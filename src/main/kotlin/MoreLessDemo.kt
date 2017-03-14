import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

var count = 0

val mutex = Mutex()

fun main(vararg args: String) = runBlocking {
    val jobs = mutableListOf<Job>()
    repeat(10) {
        jobs += async(CommonPool) {
            delay(ThreadLocalRandom.current().nextLong(1000), TimeUnit.MILLISECONDS)
            mutex.lock()
            try {
                count++
            } finally {
                mutex.unlock()
            }
        }
    }

    repeat(10) {
        jobs += async(CommonPool) {
            delay(ThreadLocalRandom.current().nextLong(1000), TimeUnit.MILLISECONDS)
            mutex.lock()
            try {
                count--
            } finally {
                mutex.unlock()
            }
        }
    }

    jobs.forEach { it.join() }
    println("Result $count")
}

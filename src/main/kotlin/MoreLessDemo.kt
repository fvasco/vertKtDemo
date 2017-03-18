import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withMutex
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

var count = 0

val mutex = Mutex()

fun main(vararg args: String) = runBlocking {
    val jobs = mutableListOf<Job>()
    repeat(10) {
        jobs += async(CommonPool) {
            delay(ThreadLocalRandom.current().nextLong(1000), TimeUnit.MILLISECONDS)
            mutex.withMutex {
                count++
            }
        }
    }

    repeat(10) {
        jobs += async(CommonPool) {
            delay(ThreadLocalRandom.current().nextLong(1000), TimeUnit.MILLISECONDS)
            mutex.withMutex {
                count--
            }
        }
    }

    jobs.forEach { it.join() }
    println("Result $count")
}

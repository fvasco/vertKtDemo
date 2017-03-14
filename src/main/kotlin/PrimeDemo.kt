import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.runBlocking

val printer = actor<String>(CommonPool) {
    for (text in channel)
        println(text)
}

fun numbers(from: Int) = produce<Int>(CommonPool) {
    for (n in from..Integer.MAX_VALUE) {
        send(n)
    }
}

fun filterNotDivisibleBy(divisor: Int, numbers: ReceiveChannel<Int>) = produce<Int>(CommonPool) {
    for (n in numbers) {
        if (n % divisor != 0) send(n)
    }
}

fun primes() = produce<Int>(CommonPool) {
    var primes = numbers(2)
    while (true) {
        val prime = primes.receive()
        send(prime)
        primes = filterNotDivisibleBy(prime, primes)
    }
}

fun main(vararg args: String) = runBlocking {
    for (n in primes()) {
        printer.send("Prime $n")
    }
}

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class MainVerticle : AbstractVerticle() {

    override fun start() {
        val eventBus = vertx.eventBus()

        // register actor
        eventBus.launchConsumer<String>("messageBus") { message ->
            println("Received ${message.body()}, reply ok")
            message.reply("ok")
        }

        // play a demo code
        launchFuture() {
            println("Do job...")
            val timerResult = handle <Long> { handler -> vertx.setTimer(2000, handler) }
            println("result is $timerResult, send finish")
            val response = handleResult <Message<String>> { eventBus.send("messageBus", "finish", it) }
            println("reply is ${response.body()}")

            println("Register demo")
            listOf(Verticle1(), Verticle2(), Verticle3(), Verticle4(), Verticle5()).forEach { verticle ->
                println("Register ${verticle.javaClass}...")
                val result = handleResult<String> { vertx.deployVerticle(verticle, it) }
                println("Register ${verticle.javaClass}: $result")
            }
            println("Start demo")
            eventBus.send(WorkStep.STEP1.busName, "work description 1")
            eventBus.send(WorkStep.STEP1.busName, "work description 2")
            eventBus.send(WorkStep.STEP1.busName, "work description 3")
        }
    }
}

/**
 * Await for [AsyncResult] and return result
 */
suspend fun <T> handleResult(block: (handler: Handler<AsyncResult<T>>) -> Unit): T =
        suspendCoroutine <T> { cont: Continuation<T> ->
            // handler returns result or exception
            val handler = Handler<AsyncResult<T>> {
                if (it.succeeded()) cont.resume(it.result()) else cont.resumeWithException(it.cause())
            }
            block(handler)
        }

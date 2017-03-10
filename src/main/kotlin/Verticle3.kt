import WorkStep.STEP3
import WorkStep.STEP4
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import kotlin.coroutines.experimental.*

class Verticle3 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        val messageConsumer = eventBus.consumer<String>(STEP3.busName)
        messageConsumer.handler(this::handleWorkRequest)
    }

    private fun handleWorkRequest(requestMessage: Message<String>) {
        val eventBus = vertx.eventBus()
        launch {
            try {
                val jobDescription = requestMessage.body()

                val workMessage = handle<String> { handler ->
                    doWork(STEP3, jobDescription, vertx, handler)
                }

                val result = handleResult<Message<String>> { handler ->
                    eventBus.send(STEP4.busName, workMessage, handler)
                }

                requestMessage.reply(result.body())
            } catch(e: Exception) {
                requestMessage.fail(0, e.message)
            }
        }
    }
}

/**
 * Vert.x Future adapter for Kotlin coroutine continuation
 */
private class FutureContinuation<T>(override val context: CoroutineContext,
                                    private val future: Future<T> = Future.future()) :
        Continuation<T>, Future<T> by future {

    override fun resume(value: T) = future.complete(value)

    override fun resumeWithException(exception: Throwable) = future.fail(exception)
}

/**
 * Create a coroutine
 */
fun <T> launch(context: CoroutineContext = EmptyCoroutineContext,
               block: suspend () -> T): Future<T> =
        FutureContinuation<T>(context).also { futureContinuation ->
            block.startCoroutine(completion = futureContinuation)
        }

/**
 * Await for resume and return result
 */
suspend fun <T> handle(block: (Handler<T>) -> Unit): T =
        suspendCoroutine { cont: Continuation<T> ->
            // handler calls `resume`
            val handler = Handler<T> { cont.resume(it) }
            block(handler)
        }

/**
 * Await for [AsyncResult] and return result
 */
suspend fun <T> handleResult(block: (Handler<AsyncResult<T>>) -> Unit): T =
        suspendCoroutine { cont: Continuation<T> ->
            // handler returns result or exception
            val handler = Handler<AsyncResult<T>> { asyncResult ->
                if (asyncResult.succeeded())
                    cont.resume(asyncResult.result())
                else
                    cont.resumeWithException(asyncResult.cause())
            }
            block(handler)
        }


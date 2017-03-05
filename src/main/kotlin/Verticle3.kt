import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import kotlin.coroutines.experimental.*

class Verticle3 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        val messageConsumer = eventBus.consumer<String>(WorkStep.STEP3.busName)
        messageConsumer.handler(this::handleWorkRequest)
    }

    private fun handleWorkRequest(requestMessage: Message<String>) {
        val eventBus = vertx.eventBus()
        launchFuture {
            try {
                val jobDescription = requestMessage.body()
                val workMessage = handle<String> { doWork(WorkStep.STEP3, jobDescription, vertx, it) }
                val result = handleResult<Message<String>> { eventBus.send(WorkStep.STEP4.busName, workMessage, it) }
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
private class VertxFutureCoroutine<T>(override val context: CoroutineContext, private val delegate: Future<T> = Future.future()) :
        Future<T> by delegate, Continuation<T> {

    override fun resume(value: T) = delegate.complete(value)

    override fun resumeWithException(exception: Throwable) = delegate.fail(exception)
}

/**
 * Create a coroutine
 */
fun <T> launchFuture(context: CoroutineContext = EmptyCoroutineContext,
                     block: suspend () -> T): Future<T> =
        VertxFutureCoroutine<T>(context).also { futureContinuation ->
            block.startCoroutine(completion = futureContinuation)
        }

/**
 * Await for resume and return result
 */
suspend fun <T> handle(block: (handler: Handler<T>) -> Unit): T =
        suspendCoroutine <T> { cont: Continuation<T> ->
            // handler calls `resume`
            val handler = Handler<T> { cont.resume(it) }
            block(handler)
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

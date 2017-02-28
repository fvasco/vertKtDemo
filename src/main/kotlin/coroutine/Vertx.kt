package coroutine

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import kotlin.coroutines.experimental.*

/**
 * Create a coroutine
 */
fun <T> asyncFuture(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): Future<T> =
        VertxFutureCoroutine<T>(context).also { block.startCoroutine(completion = it) }

class VertxFutureCoroutine<T>(override val context: CoroutineContext, private val delegate: Future<T> = Future.future()) :
        Future<T> by delegate, Continuation<T> {

    override fun resume(value: T) = delegate.complete(value)

    override fun resumeWithException(exception: Throwable) = delegate.fail(exception)
}

/**
 * Await for resume and return result
 */
suspend fun <T> handle(block: (handler: Handler<T>) -> T): T =
        suspendCoroutine <T> { cont: Continuation<T> ->
            // handler calls `resume`
            val handler = Handler<T> { cont.resume(it) }
            block(handler)
        }

/**
 * Await for [AsyncResult] and return result
 */
suspend fun <T> handleResult(block: (handler: Handler<AsyncResult<T>>) -> T): T =
        suspendCoroutine <T> { cont: Continuation<T> ->
            // handler returns result or exception
            val handler = Handler<AsyncResult<T>> {
                if (it.succeeded()) cont.resume(it.result()) else cont.resumeWithException(it.cause())
            }
            block(handler)
        }

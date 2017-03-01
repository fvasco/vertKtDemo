package coroutine

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.streams.ReadStream
import kotlin.coroutines.experimental.*

typealias MessageHandler<T> = Handler<Message<T>>
typealias AsyncResultHandler<T> = Handler<AsyncResult<T>>

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
fun <T> launchFuture(handler: AsyncResultHandler<T>? = null,
                     context: CoroutineContext = EmptyCoroutineContext,
                     block: suspend () -> T): Future<T> =
        VertxFutureCoroutine<T>(context).also { future ->
            if (handler != null) future.setHandler(handler)
            block.startCoroutine(completion = future)
        }

/**
 * Run a message consumer for a given address
 */
fun <T> EventBus.launchConsumer(address: String, block: (Message<T>) -> Unit): MessageConsumer<T> =
        consumer<T>(address).apply {
            launchFuture {
                forEach(block)
            }
        }

fun <T> EventBus.launchConsumer(address: String, handler: MessageHandler<T>): MessageConsumer<T> =
        launchConsumer(address) { handler.handle(it) }

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
suspend fun <T> handleResult(block: (handler: AsyncResultHandler<T>) -> Unit): T =
        suspendCoroutine <T> { cont: Continuation<T> ->
            // handler returns result or exception
            val handler = AsyncResultHandler<T> {
                if (it.succeeded()) cont.resume(it.result()) else cont.resumeWithException(it.cause())
            }
            block(handler)
        }

suspend fun <T> ReadStream<T>.forEach(block: (T) -> Unit): Unit {
    suspendCoroutine <Unit> { cont: Continuation<Unit> ->
        handler { block(it) }
        endHandler { cont.resume(Unit) }
    }
}

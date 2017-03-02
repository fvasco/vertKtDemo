import io.vertx.core.AbstractVerticle
import io.vertx.core.streams.ReadStream
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class Verticle4 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        launchFuture {
            val messageConsumer = eventBus.consumer<String>(WorkStep.STEP4.busName)
            messageConsumer.forEach { requestMessage ->
                val description = requestMessage.body()
                val workMessage = handle <String> { doWork(WorkStep.STEP4, description, vertx, it) }
                eventBus.send(WorkStep.STEP5.busName, workMessage)
            }
        }
    }
}

suspend fun <T> ReadStream<T>.forEach(block: suspend (T) -> Unit) {
    suspendCoroutine <Unit> { cont: Continuation<Unit> ->
        handler { handler ->
            pause()
            launchFuture {
                block(handler)
            }.setHandler { asyncResult ->
                if (asyncResult.succeeded()) {
                    resume()
                } else {
                    // remove handler
                    handler(null)
                    cont.resumeWithException(asyncResult.cause())
                }
            }
        }
        exceptionHandler { cont.resumeWithException(it) }
        endHandler { cont.resume(Unit) }
    }
}

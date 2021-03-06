import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message
import io.vertx.core.streams.ReadStream
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class Verticle4 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        launch {
            val messageConsumer = eventBus.consumer<String>(WorkStep.STEP4.busName)
            messageConsumer.forEach { requestMessage ->
                try {
                    val description = requestMessage.body()

                    val workMessage = handle <String> { handler ->
                        doWork(WorkStep.STEP4, description, vertx, handler)
                    }

                    val result = handleResult<Message<String>> { handler ->
                        eventBus.send(WorkStep.STEP5.busName, workMessage, handler)
                    }

                    requestMessage.reply(result.body())
                } catch(e: Exception) {
                    requestMessage.fail(0, e.message)
                }
            }
        }
    }
}

suspend fun <T> ReadStream<T>.forEach(block: suspend (T) -> Unit) {
    val stream = this
    suspendCoroutine { cont: Continuation<Unit> ->

        stream.endHandler { cont.resume(Unit) }
        stream.exceptionHandler { cont.resumeWithException(it) }

        stream.handler { handler ->
            pause()
            val future = launch { block(handler) }
            future.setHandler { asyncResult ->
                resume()

                if (!asyncResult.succeeded()) {
                    // on error remove handlers
                    stream.handler(null)
                    stream.exceptionHandler(null)
                    stream.endHandler(null)
                    cont.resumeWithException(asyncResult.cause())
                }
            }
        }
    }
}

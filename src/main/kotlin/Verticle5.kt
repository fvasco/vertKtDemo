import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer

class Verticle5 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        eventBus.launchConsumer<String>(WorkStep.STEP5.busName) { requestMessage ->
            val description = requestMessage.body()
            val workMessage = handle <String> { doWork(WorkStep.STEP5, description, vertx, it) }
            eventBus.send(WorkStep.STEP6.busName, workMessage)
        }
    }
}

/**
 * Run a message consumer for a given address
 */
fun <T> EventBus.launchConsumer(address: String, block: suspend (Message<T>) -> Unit): MessageConsumer<T> =
        consumer<T>(address).apply {
            launchFuture {
                forEach { message ->
                    try {
                        block(message)
                    } catch(e: Exception) {
                        message.fail(0, e.message)
                    }
                }
            }
        }

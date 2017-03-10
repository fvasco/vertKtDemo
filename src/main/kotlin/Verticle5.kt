import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer

class Verticle5 : AbstractVerticle() {
    override fun start() {
        val eventBus = vertx.eventBus()
        eventBus.launchConsumer<String, String>(WorkStep.STEP5.busName) { requestMessage ->
            val description = requestMessage.body()
            val result: String = handle { handler ->
                doWork(WorkStep.STEP5, description, vertx, handler)
            }
            return@launchConsumer result
        }
    }
}

/**
 * Run a message consumer for a given address
 */
fun <Req, Res> EventBus.launchConsumer(address: String,
                                       block: suspend (Message<Req>) -> Res): MessageConsumer<Req> {
    val consumer = consumer<Req>(address)
    launch {
        consumer.forEach { message ->
            try {
                val res = block(message)
                message.reply(res)
            } catch(e: Exception) {
                message.fail(0, e.message)
            }
        }
    }
    return consumer
}

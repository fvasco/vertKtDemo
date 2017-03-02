import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message

class Verticle2 : AbstractVerticle() {

    override fun start() {
        val eventBus = vertx.eventBus()
        eventBus
                .consumer<String>(WorkStep.STEP2.busName)
                .handler { this.handleWorkRequest(it) }
    }

    private fun handleWorkRequest(requestMessage: Message<String>) {
        doWork(WorkStep.STEP2, requestMessage.body(), vertx, Handler<String> { this.handleWorkResult(it) })
    }

    private fun handleWorkResult(result: String) {
        val eventBus = vertx.eventBus()
        eventBus.publish(WorkStep.STEP3.busName, result)
    }
}

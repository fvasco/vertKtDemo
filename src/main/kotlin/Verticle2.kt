import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

class Verticle2 : AbstractVerticle() {

    override fun start() {
        vertx.eventBus()
                .consumer<String>(WorkStep.STEP2.busName)
                .handler(WorkRequestHandler(vertx))
    }

    private class WorkRequestHandler(val vertx: Vertx) : Handler<Message<String>> {
        override fun handle(requestMessage: Message<String>) {
            doWork(WorkStep.STEP2, requestMessage.body(), vertx, WorkResultHandler(vertx, requestMessage))
        }
    }

    private class WorkResultHandler(val vertx: Vertx,
                                    val requestMessage: Message<String>) : Handler<String> {
        override fun handle(result: String) {
            vertx.eventBus().send(WorkStep.STEP3.busName, result, NextStepResultHandler(requestMessage))
        }
    }

    private class NextStepResultHandler(val requestMessage: Message<String>) : Handler<AsyncResult<Message<String>>> {
        override fun handle(event: AsyncResult<Message<String>>) {
            val result = event.result().body()
            requestMessage.reply(result)
        }
    }
}

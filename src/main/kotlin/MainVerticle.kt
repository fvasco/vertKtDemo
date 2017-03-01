import coroutine.handle
import coroutine.handleResult
import coroutine.launchConsumer
import coroutine.launchFuture
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message

class MainVerticle : AbstractVerticle() {

    override fun start(startFuture: Future<Void>) {
        val eventBus = vertx.eventBus()

        eventBus.launchConsumer<String>("messageBus") { message ->
            println("Received ${message.body()}, reply ok")
            message.reply("ok")
        }

        launchFuture(handler = (startFuture as Future<Unit>).completer()) {
            println("Do job...")
            val result = handle <Long> { handler -> vertx.setTimer(2000, handler) }
            println("result is $result, send finish")
            val response = handleResult <Message<String>> { eventBus.send("messageBus", "finish", it) }
            println("reply is ${response.body()}")
        }
    }
}

import io.vertx.core.*
import io.vertx.core.eventbus.Message

class InitVerticle : AbstractVerticle() {

    override fun start() {
        val eventBus = vertx.eventBus()
        vertx.executeBlocking {
            launch {
                while (true) {
                    handle<Long> { handler ->
                        vertx.setTimer(1000, handler)
                    }
                    print("Enter request: ")
                    val line = System.console().readLine()
                    if (line.isBlank()) vertx.close()
                    val response = handleResult<Message<String>> { handler ->
                        eventBus.send(WorkStep.STEP1.busName, line, handler)
                    }
                    println("Response: ${response.body()}")
                }
            }
        }
    }
}

fun <T> Vertx.executeBlocking(resultHandler: Handler<AsyncResult<T>>? = null, block: () -> T) {
    val blockingCodeHandler = Handler<Future<T>> { event ->
        try {
            event.complete(block())
        } catch (e: Exception) {
            event.fail(e)
        }
    }
    executeBlocking<T>(blockingCodeHandler, resultHandler)
}

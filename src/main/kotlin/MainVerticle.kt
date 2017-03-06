import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.Message

class MainVerticle : AbstractVerticle() {

    override fun start() {
        val eventBus = vertx.eventBus()

        // register actor
        eventBus.launchConsumer<String, String>("messageBus") { message ->
            println("Received ${message.body()}, reply ok")
            return@launchConsumer "ok"
        }

        // play a demo code
        launchFuture {
            println("Do job...")
            val timerResult = handle <Long> { handler -> vertx.setTimer(2000, handler) }
            println("result is $timerResult, send finish")
            val response = handleResult <Message<String>> { eventBus.send("messageBus", "finish", it) }
            println("reply is ${response.body()}")

            println("Register demo")
            listOf(Verticle1(), Verticle2(), Verticle3(), Verticle4(), Verticle5())
                    .forEach { verticle ->
                        println("Register ${verticle.javaClass}...")
                        val result = handleResult<String> { vertx.deployVerticle(verticle, it) }
                        println("Register ${verticle.javaClass}: $result")
                    }
            println("Start demo")
            eventBus.send<String>(WorkStep.STEP1.busName, "work description 1", { println("Result 1: ${it.result().body()}") })
            eventBus.send<String>(WorkStep.STEP1.busName, "work description 2", { println("Result 2: ${it.result().body()}") })
            eventBus.send<String>(WorkStep.STEP1.busName, "work description 3", { println("Result 3: ${it.result().body()}") })
        }
    }
}

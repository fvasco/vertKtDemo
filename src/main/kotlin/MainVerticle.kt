import coroutine.asyncTimer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class MainVerticle : AbstractVerticle() {

    override fun start(future: Future<Void>) {
        vertx.setTimer(1000) { event ->
            println("timer result $event")
        }

        coroutine.future {
            val result = vertx.asyncTimer(1000)
            println("coroutine result $result")
        }

        future.complete()
    }
}
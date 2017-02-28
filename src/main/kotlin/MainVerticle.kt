import coroutine.asyncFuture
import coroutine.handle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class MainVerticle : AbstractVerticle() {

    override fun start(future: Future<Void>) {
        asyncFuture {
            println("Do job...")
            val result = handle <Long> { handler -> vertx.setTimer(2000, handler) }
            println("result is $result")
            future.complete()
        }
    }
}

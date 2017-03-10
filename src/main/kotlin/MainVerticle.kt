import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions

class MainVerticle : AbstractVerticle() {
    override fun start() {
        vertx.deployVerticle(InitVerticle(), DeploymentOptions().setWorker(true))
        listOf(Verticle1(), Verticle2(), Verticle3(), Verticle4(), Verticle5())
                .forEach { verticle ->
                    vertx.deployVerticle(verticle)
                }
    }
}

import io.vertx.core.Handler
import io.vertx.core.Vertx
import java.util.concurrent.ThreadLocalRandom

enum class WorkStep {
    STEP1, STEP2, STEP3, STEP4, STEP5, STEP6
}

val WorkStep.busName get() = name

    fun doWork(workStep: WorkStep,
               description: String,
               vertx: Vertx,
               handler: Handler<String>) {
        val millis = ThreadLocalRandom.current().nextLong(2000)
        vertx.setTimer(millis) {
            val result = "$description ($workStep $millis ms)"
            handler.handle(result)
        }
    }

import io.vertx.core.Handler
import io.vertx.core.Vertx

enum class WorkStep {
    STEP1, STEP2, STEP3, STEP4, STEP5, STEP6
}

val WorkStep.busName get() = name

fun doWork(workStep: WorkStep, description: String, vertx: Vertx, handler: Handler<String>) {
    println("$workStep: $description")
    vertx.setTimer(1000) {
        handler.handle(description)
    }
}

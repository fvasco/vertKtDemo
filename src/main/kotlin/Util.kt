import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec

enum class WorkStep {
    STEP1, STEP2, STEP3, STEP4, STEP5, STEP6;

    val busName get() = name
}

fun doWork(workStep: WorkStep, description: String, vertx: Vertx, handler: Handler<String>) {
    println("$workStep: $description")
    vertx.setTimer(1000) {
        handler.handle(description)
    }
}

object UnitMessageCodec : MessageCodec<Unit, Any> {
    override fun systemCodecID(): Byte = -1

    override fun name(): String = Unit::class.java.canonicalName

    override fun encodeToWire(buffer: Buffer?, s: Unit?) = Unit

    override fun decodeFromWire(pos: Int, buffer: Buffer?): Any = Unit

    override fun transform(s: Unit?): Any = Unit
}

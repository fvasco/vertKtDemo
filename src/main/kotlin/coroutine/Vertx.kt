package coroutine

import io.vertx.core.Vertx
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

suspend fun Vertx.asyncTimer(delay: Long): Long =
        suspendCoroutine<Long> { cont: Continuation<Long> ->
            setTimer(delay) { event ->
                cont.resume(event)
            }
        }

package coroutine

import java.util.concurrent.CompletableFuture
import kotlin.coroutines.experimental.*

fun <T> future(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): CompletableFuture<T> =
        CompletableFutureCoroutine<T>(context).also { block.startCoroutine(completion = it) }

class CompletableFutureCoroutine<T>(override val context: CoroutineContext) : CompletableFuture<T>(), Continuation<T> {
    override fun resume(value: T) { complete(value) }
    override fun resumeWithException(exception: Throwable) { completeExceptionally(exception) }
}
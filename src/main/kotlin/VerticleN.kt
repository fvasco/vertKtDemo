import io.vertx.core.Vertx
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.toCoroutineDispatcher
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

sealed class Workflow
class WorkflowStep(val description: String,
                   val response: CompletableFuture<String> = CompletableFuture()) : Workflow()

fun launchActor(step: WorkStep,
                channel: Channel<Workflow>,
                next: Channel<Workflow>,
                vertx: Vertx): Job =
        kotlinx.coroutines.experimental.launch(vertx.toCoroutineDispatcher()) {
            for (workflow in channel) {
                when (workflow) {
                    is WorkflowStep -> {
                        val description = workflow.description

                        val result: String = handle { handler ->
                            doWork(step, description, vertx, handler)
                        }

                        val nextWorkflowStep = WorkflowStep(result)
                        next.send(nextWorkflowStep)
                        nextWorkflowStep.response.get().let { workflow.response.complete(it) }
                    }
                }
            }
        }


private fun Vertx.toCoroutineDispatcher() = VertxExecutor(this).toCoroutineDispatcher()

private class VertxExecutor(val vertx: Vertx) : Executor {
    override fun execute(command: Runnable) = vertx.runOnContext { command.run() }
}

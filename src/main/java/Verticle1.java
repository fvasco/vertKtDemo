import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class Verticle1 extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus
                .<String>consumer(WorkStep.STEP1.getBusName())
                .handler(workRequestMessage -> UtilKt.doWork(WorkStep.STEP1, workRequestMessage.body(), vertx,
                        workResult -> eventBus.send(WorkStep.STEP2.getBusName(), workResult,
                                asyncMessageResult -> workRequestMessage.reply(asyncMessageResult.result().body())
                        )));
    }

    private void handleWorkRequest(Message<String> workRequestMessage) {
        UtilKt.doWork(WorkStep.STEP1, workRequestMessage.body(), vertx, this::handleWorkResult);
    }

    private void handleWorkResult(String workResult) {
        EventBus eventBus = vertx.eventBus();
        eventBus.send(WorkStep.STEP2.getBusName(), workResult, this::handleResult);
    }

    private void handleResult(AsyncResult<Message<String>> asyncMessageResult) {

    }
}

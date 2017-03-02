import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class Verticle1 extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus
                .<String>consumer(WorkStep.STEP1.getBusName())
                .handler(this::handleWorkRequest);
    }

    private void handleWorkRequest(Message<String> requestMessage) {
        UtilKt.doWork(WorkStep.STEP1, requestMessage.body(), vertx, this::handleWorkResult);
    }

    private void handleWorkResult(String result) {
        EventBus eventBus = vertx.eventBus();
        eventBus.publish(WorkStep.STEP2.getBusName(), result);
    }
}

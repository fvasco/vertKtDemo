import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class Verticle1 extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus
                .<String>consumer(UtilKt.getBusName(WorkStep.STEP1))
                .handler(workRequestMessage -> UtilKt.doWork(WorkStep.STEP1, workRequestMessage.body(), vertx,
                        workResult -> eventBus.send(UtilKt.getBusName(WorkStep.STEP2), workResult,
                                asyncMessageResult -> workRequestMessage.reply(asyncMessageResult.result().body())
                        )));
    }
}

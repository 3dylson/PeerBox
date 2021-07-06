package pt.ipb.dsys.peerbox;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class PeerBoxApp extends Application {
    private ConfigurableApplicationContext appContext;


    @Override
    public void init() {
        appContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage stage) {
        appContext.publishEvent(new StageReadyEvent(stage));

    }

    @Override
    public void stop() {
        appContext.close();
        Platform.exit();
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }

        public Stage getStage() {
            return (Stage) getSource();
        }
    }
}


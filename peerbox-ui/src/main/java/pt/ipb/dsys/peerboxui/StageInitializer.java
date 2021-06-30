package pt.ipb.dsys.peerboxui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerboxui.PeerBoxApp.StageReadyEvent;

import java.awt.*;
import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {
    @Value("classpath:/peerbox.fxml")
    private Resource peerboxResource;
    private String appTitle;
    private ApplicationContext appContext;

    public StageInitializer(@Value("${spring.application.ui.title}") String appTitle, ApplicationContext appContext) {
        this.appTitle = appTitle;
        this.appContext = appContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(peerboxResource.getURL());
            fxmlLoader.setControllerFactory(param -> appContext.getBean(param));
            Parent parent = fxmlLoader.load();

            Stage stage = event.getStage();
            stage.setScene(new Scene(parent,600,400));
            stage.setTitle(appTitle);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

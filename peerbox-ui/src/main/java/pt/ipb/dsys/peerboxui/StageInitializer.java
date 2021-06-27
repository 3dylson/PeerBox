package pt.ipb.dsys.peerboxui;

import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pt.ipb.dsys.peerboxui.PeerBoxApp.StageReadyEvent;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
    }
}

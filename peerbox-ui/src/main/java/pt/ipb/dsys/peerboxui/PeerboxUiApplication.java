package pt.ipb.dsys.peerboxui;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeerboxUiApplication {

    public static void main(String[] args) {
        Application.launch(PeerBoxApp.class, args);
    }

}

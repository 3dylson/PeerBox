package pt.ipb.dsys.peerbox;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.System.exit;

@SpringBootApplication
public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";
    public static final String gossipHostname = "gossip-router";
    public static final String peerBox = ("boxFiles/");


    public static void main(String[] args) {
        Application.launch(PeerBoxApp.class, args);
        exit(0);
    }

}

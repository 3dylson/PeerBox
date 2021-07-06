package pt.ipb.dsys.peerbox;

import javafx.application.Application;
import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.PeerUtil;
import pt.ipb.dsys.peerbox.util.Sleeper;
import pt.ipb.dsys.peerbox.util.WatchCallable;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

@SpringBootApplication
public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";
    public static final String gossipHostname = "gossip-router";
    public static final String peerBox = ("boxFiles/");


    public static void main(String[] args) {
        PeerUtil.localhostFix(gossipHostname);
        boolean isNode = args.length > 0 && args[0].equals("node");
        if (isNode) {
            new Main().initializeCluster();
        }
        Application.launch(PeerBoxApp.class, args);
        exit(0);
    }

    private void initializeCluster() {

        // Cluster initialization and connection

        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter(gossipHostname, 12001))) {

            logger.info("Starting a background thread for watching folders.");
            //new Thread(new WatchFolder()).start();
            ExecutorService executor = Executors.newCachedThreadPool();
            LoggingReceiver receiver = new LoggingReceiver(channel);
            channel.setReceiver(receiver);
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            channel.connect(CLUSTER_NAME);
            // optional: let cluster stabilize to reduce ambiguity, although it works all the same
            Sleeper.sleep(5000);

            PeerFile peerFile = new PeerFile(channel, receiver);
            executor.submit(new WatchCallable(peerFile));
            String hostname = DnsHelper.getHostName();

            File dir = new File(peerBox);
            if (!dir.exists()) {
                dir.mkdir();
            }

            while (true) {
                if (!receiver.getChunks().isEmpty()) {
                    logger.info("-- ({}) have this files chunks: ", hostname);
                    receiver.listChunks();
                    Sleeper.sleep(50000);
                }
                Sleeper.sleep(20000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

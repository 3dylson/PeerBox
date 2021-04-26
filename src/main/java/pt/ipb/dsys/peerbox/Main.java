package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";


    public static void main(String[] args) {

        // Cluster initialization and connection
        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter())) {

            channel.connect(CLUSTER_NAME);
            channel.setReceiver(new LoggingReceiver());
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded

            String hostname = DnsHelper.getHostName();


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


    }

}

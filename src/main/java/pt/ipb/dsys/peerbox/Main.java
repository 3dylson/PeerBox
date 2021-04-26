package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";

    private JChannel channel;
    PeerFile dir;


    public static void main(String[] args) {

        new Main().initializeCluster();

    }

    private void initializeCluster() {

        // Cluster initialization and connection
        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter())) {

            channel.connect(CLUSTER_NAME);
            channel.setReceiver(new LoggingReceiver());
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            channel.getState(null, 10000);

            String hostname = DnsHelper.getHostName();


            while (true){
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    //File[] f = dir.listFiles();
                    System.out.print("> "); System.out.flush();
                    String line=in.readLine().toLowerCase();
                    if(line.startsWith("quit") || line.startsWith("exit"))
                        break;
                    line="[" + hostname + "] " + line;
                    ObjectMessage message = new ObjectMessage(null,line);
                    channel.send(message);
                    Sleeper.sleep(2000);
                } catch (PeerBoxException e) {
                    logger.info("The node: {} is disconnecting",hostname);
                    channel.close();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    // Returns the number of connected nodes in the cluster
    private int clusterSize() {
        return channel.getView().getMembers().size();
    }


}

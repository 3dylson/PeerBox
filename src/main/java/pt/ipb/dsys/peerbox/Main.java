package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.PeerUtil;
import pt.ipb.dsys.peerbox.util.Sleeper;
import pt.ipb.dsys.peerbox.util.WatchCallable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";
    public static final String gossipHostname = "gossip-router";
    public static final String peerBox = ("peerBox/");


    public static void main(String[] args) {

        PeerUtil.localhostFix(gossipHostname);
        boolean isNode = args.length > 0 && args[0].equals("node");
        new Main().initializeCluster(isNode);

    }

    private void initializeCluster(boolean isNode) {

        // Cluster initialization and connection

        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter(gossipHostname,12001))) {

            logger.info("Starting a background thread for watching folders.");
            //new Thread(new WatchFolder()).start();
            ExecutorService executor = Executors.newCachedThreadPool();
            LoggingReceiver receiver = new LoggingReceiver(channel);
            channel.setReceiver(receiver);
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            channel.connect(CLUSTER_NAME);
            // optional: let cluster stabilize to reduce ambiguity, although it works all the same
            Sleeper.sleep(5000);

            PeerFile peerFile = new PeerFile(channel,receiver);
            executor.submit(new WatchCallable(peerFile));
            String hostname = DnsHelper.getHostName();

            File dir = new File(peerBox);
            if (!dir.exists()){
                dir.mkdir();
            }
            if(!isNode) {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        //File[] f = dir.listFiles();
                        System.out.print("""
                                > Welcome to the PeerBox! :)
                                [1] List all files
                                [2] Fetch a file
                                [3] Delete a file
                                Or "exit" to leave.
                                """);
                        System.out.flush();

                        String line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit"))
                            break;
                        else if (line.startsWith("1")) {
                            logger.info("Listing files on peerBox: ");
                            receiver.listFiles();
                        }
                        else if (line.startsWith("2")){
                            System.out.print("> Enter the filename to fetch\n");
                            String filename = in.readLine();
                            PeerFileID id = new PeerFileID(null,filename,null,0);
                            peerFile.fetch(id);
                        }
                        else if (line.startsWith("3")) {
                            System.out.print("> Enter the filename to delete\n");
                            String filename = in.readLine();
                            PeerFileID id = new PeerFileID(null,filename,null,0);
                            peerFile.delete(id);
                        }
                    } catch (PeerBoxException e) {
                        logger.info("The node: {} is disconnecting", hostname);
                        channel.close();
                        break;
                    }
                }
            } else {
                while (true) {
                    try{
                        if(!peerFile.getPeerFiles().isEmpty()){
                            logger.info("-- ({}) have this files chunks: ",hostname);
                            peerFile.listFiles();
                            Sleeper.sleep(50000);
                        }
                        Sleeper.sleep(20000);
                    } catch (Exception e) {
                        logger.warn("I have disconnected! {}",hostname);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

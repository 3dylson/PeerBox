package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBox;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";

    private JChannel channel;
    PeerFile dir;
    PeerBox peerBox;


    public static void main(String[] args) {

        new Main().initializeCluster();

    }

    private void initializeCluster() {

        // Cluster initialization and connection
        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter())) {

            channel.connect(CLUSTER_NAME);
            channel.setReceiver(new LoggingReceiver());
            //channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            //channel.getState(null, 10000);

            String hostname = DnsHelper.getHostName();


            while (true){
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    //File[] f = dir.listFiles();
                    System.out.print("""
                            > Welcome to our PeerBox! :)
                            1. Create a file
                            2. List all files
                            Or "exit" to leave.
                            """); System.out.flush();
                    Sleeper.sleep(10000);
                    String line=in.readLine().toLowerCase();
                    if(line.startsWith("quit") || line.startsWith("exit"))
                        break;
                    else if(line.startsWith("1")){
                        System.out.print("> Name your file\n");
                        String fileName = in.readLine();
                        //FileInputStream fileIn = new FileInputStream(fileName);
                        PeerFile fileIn = new PeerFile(fileName);
                        System.out.print("> Write something in your file.\n*write s to save and exit*:\n");
                        String data;
                        do {
                            /*byte[] buf = new byte[8096];
                            int bytes=fileIn.*/
                            byte[] buf = new byte[8096];
                            data = in.readLine();
                            buf = data.getBytes(StandardCharsets.UTF_8);
                            fileIn.setData(buf);
                            ObjectMessage message = new ObjectMessage(null,data);
                            channel.send(message);
                        }while (!data.equals("s"));
                        System.out.print("> Enter the path of the file\n");
                        String path = in.readLine();
                        System.out.print("> Enter the number of the replicas(per chunks)\n");
                        int replicas = in.read();
                        fileIn.save(path,replicas);
                        logger.info("The file was saved in the path {} with {} replicas", path,replicas);

                        /*line=" Executed " + line;
                        System.out.print("> Enter the name of the file you which to save\n");
                        String fileName = in.readLine();
                        FileInputStream fileIn = new FileInputStream(fileName);

                        System.out.print("> Enter the path of the file\n");
                        String path = in.readLine();
                        System.out.print("> Enter the number of the replicas(per chunks)\n");
                        int replicas = in.read();
                        //peerBox.save(path,replicas);
                        logger.info("The file was saved in the path {} with {} replicas", path,replicas);*/
                    }
                    else if(line.startsWith("fetch")){
                        line=" Executed " + line;
                        System.out.print("> Enter the PeerFileID to retrieve the file\n");
                        PeerFile id = null;
                        int id2 = in.read();
                        /*assert false;
                        id.setFileId(id2);
                        peerBox.fetch(id);*/
                    }
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

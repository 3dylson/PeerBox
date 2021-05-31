package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.PeerUtil;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.*;
import java.util.Arrays;

public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";
    public static final String gossipHostname = "gossip-router";
    private long timestamp = 0;


    public static void main(String[] args) {

        PeerUtil.localhostFix(gossipHostname);
        boolean isNode = args.length > 0 && args[0].equals("node");
        new Main().initializeCluster(isNode);

    }

    private void initializeCluster(boolean isNode) {

        // Cluster initialization and connection

        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter(gossipHostname,12001))) {

            LoggingReceiver receiver = new LoggingReceiver(channel);
            channel.setReceiver(receiver);
            channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            channel.connect(CLUSTER_NAME);
            // optional: let cluster stabilize to reduce ambiguity, although it works all the same
            Sleeper.sleep(5000);




            String hostname = DnsHelper.getHostName();
            String path = ("\\tmp\\");
            File dir = new File(path);
            if (!dir.exists()){
                dir.mkdir();
            }

            if(!isNode) {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        //File[] f = dir.listFiles();
                        System.out.print("""
                                > Welcome to our PeerBox! :)
                                [1] Create a file
                                [2] List all files
                                [3] Fetch a file
                                [4] Delete a file
                                Or "exit" to leave.
                                """);
                        System.out.flush();

                        String line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit"))
                            break;
                        else if (line.startsWith("1")) {
                            String GUID = channel.getAddressAsUUID();
                            System.out.print("> Enter the filename\n");
                            String filename = in.readLine();
                            System.out.print("> The file path is "+path+" \n");
                            PeerFileID ID = new PeerFileID(GUID,filename,path,timestamp);
                            PeerFile file = new PeerFile(ID);
                            file.setChannel(channel);

                            File sysFile = new File(path+"\\"+filename);
                            System.out.print("> Enter the file content [type exit to quit writing!]\n");
                            if (!sysFile.exists()){
                                sysFile.createNewFile();
                            }
                            FileOutputStream inF = new FileOutputStream(sysFile);
                            try{
                                String content;
                                do {
                                    content = in.readLine();
                                    inF.write(content.getBytes());
                                } while (!content.endsWith("exit"));
                                inF.close();
                                FileInputStream out = new FileInputStream(sysFile);
                                byte[] fileContent = out.readAllBytes();
                                file.setData(fileContent);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                System.out.print("> Enter the number of the replicas(per chunks)\n");
                                int replicas = in.read();
                                file.save(path, replicas);
                                channel.send(new ObjectMessage(null,file.getFileId()));
                                logger.info("The file was saved in the path {}.", path);
                                //file.setData(buf);
                            }
                            /*while(bu)
                            byte[] buf = new byte[8096];
                            int bytes = inF.read(buf);

                            file.setData(buf);*/

                        }
                        else if (line.startsWith("2")) {
                            //String text = "Executed Listing!";
                            ObjectMessage message = new ObjectMessage(null, dir);
                            channel.send(message);
                        }
                        else if (line.startsWith("3")){
                            String GUID = channel.getAddressAsUUID();
                            System.out.print("> The file ID available is: "+GUID+"\n");
                            System.out.print("> Enter the file ID to fetch)\n");
                            String fileID = in.readLine();
                            PeerFileID ID = new PeerFileID(fileID);
                            PeerFile file = new PeerFile(ID);
                            file.setChannel(channel);
                            file.fetch(ID);
                        }
                        else if (line.startsWith("4")) {
                            String GUID = channel.getAddressAsUUID();
                            System.out.print("> The file ID available is: "+GUID+"\n");
                            System.out.print("> Enter the file ID to delete)\n");
                            String fileID = in.readLine();
                            PeerFileID ID = new PeerFileID(fileID);
                            PeerFile file = new PeerFile(ID);
                            file.setChannel(channel);
                            file.delete(ID);
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
                        //logger.info("I am a node! {}",hostname);
                        /*String text = String.format("Hello from %s!", hostname);
                        ObjectMessage message = new ObjectMessage(null, text);
                        channel.send(message);*/

                        File[] f = dir.listFiles();
                        Arrays.stream(f).forEach(System.out::println);
                        Sleeper.sleep(300000);
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

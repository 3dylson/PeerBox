package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.Chunk;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final String CLUSTER_NAME = "PeerBox";
    private long timestamp = 0;

    final Map<PeerFileID, PeerFile> files = new ConcurrentHashMap<>();



    public static void main(String[] args) {
        boolean isNode = args.length > 0 && args[0].equals("node");
        new Main().initializeCluster(isNode);

    }

    private void initializeCluster(boolean isNode) {

        // Cluster initialization and connection
        try (JChannel channel = new JChannel(DefaultProtocols.gossipRouter())) {

            channel.connect(CLUSTER_NAME);
            channel.setReceiver(new LoggingReceiver());
            //channel.setDiscardOwnMessages(true);  // <-- Own messages will be discarded
            //channel.getState(null, 10000);

            String hostname = DnsHelper.getHostName();

            if(!isNode) {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                        //File[] f = dir.listFiles();
                        System.out.print("""
                                > Welcome to our PeerBox! :)
                                [1] Create a file
                                [2] List all files
                                Or "exit" to leave.
                                """);
                        System.out.flush();

                        String line = in.readLine().toLowerCase();
                        if (line.startsWith("quit") || line.startsWith("exit"))
                            break;
                        else if (line.startsWith("1")) {
                            timestamp++;
                            String text = "Executed Create/Save!";
                            ObjectMessage message = new ObjectMessage(null, text);
                            channel.send(message);
                            String GUID = channel.getAddressAsUUID();
                            System.out.print("> Enter the filename\n");
                            String filename = in.readLine();
                            System.out.print("> Enter the file path\n");
                            String path = in.readLine();
                            PeerFileID ID = new PeerFileID(GUID,filename,path,timestamp);
                            PeerFile file = new PeerFile(ID);

                            File dir = new File(path);
                            File sysFile = new File(path+"\\"+filename);
                            System.out.print("> Enter the file content\n");
                            if (!sysFile.exists()){
                                dir.mkdir();
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
                                files.put(file.getFileId(),file);
                                file.save(path, replicas);
                                logger.info("The file was saved in the path {} with {} replicas", path, replicas);
                                //file.setData(buf);
                            }
                            /*while(bu)
                            byte[] buf = new byte[8096];
                            int bytes = inF.read(buf);

                            file.setData(buf);*/

                            //System.out.print("> Write something in your file.\n*write s to save and exit*:\n");
                        } else if (line.startsWith("2")) {
                            String text = "Executed Listing!";
                            ObjectMessage message = new ObjectMessage(null, text);
                            channel.send(message);
                            synchronized (files){
                                for(Map.Entry<PeerFileID,PeerFile> entry: files.entrySet()){
                                    System.out.println(entry.getKey() + ": " + entry.getValue());
                                }
                            }
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
                        //TODO update view!
                        //logger.info("I am a node! {}",hostname);
                        String text = String.format("Hello from %s!", hostname);
                        ObjectMessage message = new ObjectMessage(null, text);
                        channel.send(message);
                        Sleeper.sleep(300000);
                    } catch (Exception e) {
                        logger.warn("I have disconected! {}",hostname);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }



}

package pt.ipb.dsys.peerbox.jgroups;

import com.google.common.primitives.Bytes;
import org.jgroups.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;
import pt.ipb.dsys.peerbox.util.PeerUtil;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pt.ipb.dsys.peerbox.Main.peerBox;

public class LoggingReceiver implements Receiver, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    private JChannel channel;

    final List<Address> members = new LinkedList<>();
    Map<String, Integer> totalChunkfile = new ConcurrentHashMap<>();
    Map<UUID, byte[]> chunks = new ConcurrentHashMap<>();

    List<byte[]> tmpchunks = new ArrayList<>();

    public enum STATES {
        DEFAULT, SAVE, FETCH, DELETE, FILE
    }

    private STATES state = STATES.DEFAULT;

    public LoggingReceiver(JChannel channel) {
        this.channel = channel;
    }

    public void setChannel(JChannel channel) {
        this.channel = channel;
    }

    public Map<UUID, byte[]> getChunks() {
        return chunks;
    }

    public void setState(STATES state) {
        this.state = state;
    }

    /**
     * Called when a change in membership has occurred. No long running actions, sending of messages
     * or anything that could block should be done in this callback. If some long running action
     * needs to be performed, it should be done in a separate thread.
     * <br/>
     * Note that on reception of the first view (a new member just joined), the channel will not yet
     * be in the connected state. This only happens when {@link JChannel#connect(String)} returns.
     *
     * @param new_view
     */
    @Override
    public void viewAccepted(View new_view) {
        logger.info("# New View: {} ",new_view);
        synchronized (members) {
            members.clear();
            members.addAll(new_view.getMembers());
        }
    }

    @Override
    public void receive(Message msg) {
       Object message = msg.getObject();
       Address source = msg.src();
       /*String line = "Message received from: "
                + msg.getSrc()
                + " to: " + msg.getDest()
                + " -> " + message;
        logger.info(line);*/
       if(message instanceof PeerFileID) {

           if (state == STATES.SAVE){
               logger.info("Received chunk number {}, of the file: {}.",((PeerFileID) message).getChunkNumber(), ((PeerFileID) message).getFileName() );
               chunks.put(((PeerFileID) message).getId(),((PeerFileID) message).getChunk());

           }

           else if (state == STATES.FETCH){
               byte[] chunkBytes = chunks.get(((PeerFileID) message).getId());
               if (chunkBytes != null) {
                   logger.info("-- sending chunks of the file: {} ... ", ((PeerFileID) message).getFileName());
                   ((PeerFileID) message).setChunk(chunkBytes);
                   try {
                       logger.info("(Debugging) FILE FETCH");
                       //channel.send(new ObjectMessage(source,"File"));
                       //this.setState(STATES.FILE);
                       PeerFile newFile = new PeerFile(channel,this);
                       newFile.setFileId((PeerFileID) message);
                       PeerFileID peerFileID = (PeerFileID) message;
                       newFile.getChannel().send(new ObjectMessage(source,peerFileID));
                       Sleeper.sleep(300);
                       //channel.send(new ObjectMessage(null,peerFileID));
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
           }

           else if (state == STATES.FILE) {

               int fetchTotal = totalChunkfile.get(((PeerFileID) message).getFileName());
               tmpchunks.add(((PeerFileID) message).getChunkNumber(),((PeerFileID) message).getChunk());
               //logger.info("(Debugging) FILE REBUILD");
               if (((PeerFileID) message).getChunkNumber() == fetchTotal) {

                   byte[] fileBytes = new byte[0];
                   File temp = new File(peerBox+((PeerFileID) message).getFileName());
                   for (int i = 1; i <= fetchTotal; i++) {
                       int index = i-1;
                       if(i == 1){
                           fileBytes=tmpchunks.get(index);
                       }else{
                           fileBytes = Bytes.concat(fileBytes, tmpchunks.get(index));
                       }
                   }
                   fileBytes= PeerUtil.trimBytes(fileBytes);
                   try {
                       FileWriter writer = new FileWriter(temp);
                       InputStream in = new ByteArrayInputStream(fileBytes);
                       writer.flush();
                       int next = in.read();
                       while (next != -1) {

                           writer.write(next);
                           next = in.read();
                       }
                       writer.close();
                   } catch (IOException ie) {
                       ie.printStackTrace();
                   } finally {
                       logger.info("File successfully fetched: {}",peerBox+((PeerFileID) message).getFileName());
                       logger.info("Fetch {} again to show metadata...",peerBox+((PeerFileID) message).getFileName());
                       this.setState(STATES.DEFAULT);
                   }
               }
           }

           else if (state == STATES.DELETE) {
               byte[] chunkBytes = chunks.get(((PeerFileID) message).getId());
               if (chunkBytes != null) {
                   logger.info("-- deleting all chunks of the file: {}",((PeerFileID) message).getFileName());
                   chunks.remove(((PeerFileID) message).getId());
                   totalChunkfile.remove(((PeerFileID) message).getFileName());

               }
               else {
                   logger.info("-- I dont have the chunks of the file: {} ...",((PeerFileID) message).getFileName());
               }
           }
       }

       else if(message instanceof PeerFile) {

           if (state == STATES.SAVE){
               totalChunkfile.put(((PeerFile) message).getFileId().getFileName(),((PeerFile) message).getTotalChunks());
           }

       }

       else if (message instanceof String) {

           if ("Save".equals(message)) {
               this.setState(STATES.SAVE);
           }
           else if("Fetch".equals(message)) {
               this.setState(STATES.FETCH);
           }
           else if("Delete".equals(message)) {
               this.setState(STATES.DELETE);
           }
           else if("File".equals(message)) {
               this.setState(STATES.FILE);
           }
           else if("Default".equals(message)) {
               this.setState(STATES.DEFAULT);
           }
       }


    }

    public void listChunks() {
        chunks.forEach((key, value) -> logger.info("{} : {}",key,value));
    }

    // Returns the number of connected nodes in the cluster
    public int clusterSize() {
        return members.size();
    }

}

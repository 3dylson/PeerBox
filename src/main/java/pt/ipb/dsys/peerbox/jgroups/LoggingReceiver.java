package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pt.ipb.dsys.peerbox.Main.peerBox;

public class LoggingReceiver implements Receiver, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    private JChannel channel;
    private View new_view;

    List<Address> members = new LinkedList<>();
    Map<String, List<UUID>> files = new ConcurrentHashMap<>();
    Map<UUID, List<byte[]>> chunks = new ConcurrentHashMap<>();

    List<List<byte[]>> fetchByteList = new ArrayList<>();

    public enum STATES {
        DEFAULT, SAVE, FETCH, DELETE
    }

    private STATES state = STATES.DEFAULT;

    public LoggingReceiver(JChannel channel) {
        this.channel = channel;
    }

    public JChannel getChannel() {
        return channel;
    }

    public void setChannel(JChannel channel) {
        this.channel = channel;
    }

    public View getNew_view() {
        return new_view;
    }

    public void setNew_view(View new_view) {
        this.new_view = new_view;
    }

    public List<Address> getMembers() {
        return members;
    }

    public void setMembers(List<Address> members) {
        this.members = members;
    }

    public Map<String, List<UUID>> getFiles() {
        return files;
    }

    public void setFiles(Map<String, List<UUID>> files) {
        this.files = files;
    }

    public Map<UUID, List<byte[]>> getChunks() {
        return chunks;
    }

    public void setChunks(Map<UUID, List<byte[]>> chunks) {
        this.chunks = chunks;
    }

    public STATES getState() {
        return state;
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
       String line = "Message received from: "
                + msg.getSrc()
                + " to: " + msg.getDest()
                + " -> " + message;
        logger.info(line);
       if(message instanceof PeerFileID) {

           if (state == STATES.SAVE){
               logger.info("Received chunk number {}, of the file: {}.",((PeerFileID) message).getChunkNumber(), ((PeerFileID) message).getFileName() );
               chunks.put(((PeerFileID) message).getId(),((PeerFileID) message).getChunk());
           }

           else if (state == STATES.FETCH){
               List<byte[]> chunkBytes = chunks.get(((PeerFileID) message).getId());
               if (!chunkBytes.isEmpty()) {
                   logger.info("-- sending chunks of the file: {} ... ", ((PeerFileID) message).getFileName());
                   ((PeerFileID) message).setChunk(chunkBytes);

                   List<UUID> fileChunks = getFiles().get(((PeerFileID) message).getFileName());
                   //fetchByteList.add(((PeerFileID) message).getChunkNumber(),((PeerFileID) message).getChunk());

                   if (((PeerFileID) message).getChunkNumber() == fileChunks.size() - 1) {

                       try {
                           PeerFile file = new PeerFile(null,null);
                           file.setFileId((PeerFileID) message);
                           channel.send(new ObjectMessage(msg.src(),file));
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   }
               }
           }

           else if (state == STATES.DELETE) {
               List<byte[]> chunkBytes = chunks.get(((PeerFileID) message).getId());
               if (!chunkBytes.isEmpty()) {
                   logger.info("-- deleting all chunks of the file: {}",((PeerFileID) message).getFileName());
                   chunks.remove(((PeerFileID) message).getId());
                   files.remove(((PeerFileID) message).getFileName());
               }
               else {
                   logger.info("-- I dont have the chunks of the file: {} ...",((PeerFileID) message).getFileName());
               }
           }
       }

       else if(message instanceof PeerFile) {

           //?((PeerFile) message).save()

           String outputFile = new File(((PeerFile) message).getFileId().getFileName()).getName();
           outputFile = peerBox+outputFile;
           try {
               OutputStream out = new FileOutputStream(outputFile);
               out.write(((PeerFile) message).getData());
               ((PeerFile) message).getPeerFiles().put(((PeerFile) message).getFileId().getFileName(),out);

           } catch (IOException e) {
               e.printStackTrace();
           }

           OutputStream thisFile = ((PeerFile) message).getPeerFiles().get(((PeerFile) message).getFileId().getFileName());
           //peerFiles.put(path,out);
           logger.info("Fetched file infos: {}",thisFile.toString());
           /*String info = ((PeerFile) message).getFileId().getChunk().toString();
           logger.info("{}",info);*/
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
           else if("Default".equals(message)) {
               this.setState(STATES.DEFAULT);
           }
       }
    }

    public void listFiles() {
        if (!files.isEmpty()) {

            files.forEach((key, value) -> logger.info("{} : {}",key, value));
        }
        else {
            logger.warn("No files to list.");
        }
    }

    // Returns the number of connected nodes in the cluster
    public int clusterSize() {
        return members.size();
    }

}

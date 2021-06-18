package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.*;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingReceiver implements Receiver, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    private JChannel channel;
    private View new_view;

    List<Address> members = new LinkedList<>();
    Map<String, List<UUID>> files = new ConcurrentHashMap<>();
    Map<UUID, byte[]> chunks = new ConcurrentHashMap<>();

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

    public Map<UUID, byte[]> getChunks() {
        return chunks;
    }

    public void setChunks(Map<UUID, byte[]> chunks) {
        this.chunks = chunks;
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
        System.out.println(line);
       if(message instanceof PeerFileID) {
           // Increments the logical clock timestamp whenever a request is received
           timestamp++;

           if (state == STATES.NULL){
               OutputStream out = files.get(((PeerFileID) message).getFilename());
               try{
                   if (out == null) {
                       String output_filename = new File(((PeerFileID) message).getFilename()).getName();
                       output_filename = "\\tmp\\"+output_filename;
                       out = new FileOutputStream(output_filename);
                       System.out.println("-- creating file "+((PeerFileID) message).getFilename()+"\n");
                       files.put(((PeerFileID) message).getFilename(),out);
                   }
               } catch (FileNotFoundException e) {
                   e.printStackTrace();
               }
           }

           else if (state == STATES.DELETE){
               OutputStream out = files.get(((PeerFileID) message).getFilename());
               if (out == null){
                   System.out.println("-- file doesn't exists! \n");
               }
               synchronized (files){
                   files.remove(((PeerFileID) message).getFilename());
                   System.out.println("-- deleting file "+out+"\n");
               }
           }

       }

       else if(message instanceof PeerFile) {
           if(state == STATES.WAITING) {
               sendChunk((PeerFile) message);
               System.out.println("-- sending chunks... ");
           }

       }

       else if (message instanceof Chunk) {
           chunks.add((Chunk) message);
           /*logger.info("Received chunk number {}, of the file {}, from {} ", ((Chunk) message).getChunkNo(),
                   ((Chunk) message).getFileID().getFileId().getFilename(), msg.getSrc() );*/
           System.out.println("Received chunk number "+((Chunk) message).getChunkNo()+" of the file "
                   +((Chunk) message).getFileID().getFileId().getFilename());

       }

       else if (message instanceof File) {
           if (files.isEmpty()){
               System.out.println("-- no files! \n");
           }
           synchronized (files){
               for(Map.Entry<String,OutputStream> entry: files.entrySet()){
                   System.out.println(entry.getKey() + ": " + entry.getValue());
               }
           }
       }

    }

    private void sendFile(PeerFile request) {
        UUID destination = null;

        // Gets the destination by comparing the request's GUID with every GUID in the cluster
        for (Address address : channel.getView().getMembers()) {
            UUID addressUUID = (UUID) address;
            if (addressUUID.toStringLong().equals(request.getFileId().getGUID())) {
                destination = addressUUID;
            }
        }
        if (destination !=null){
            try{
                PeerFile file = new PeerFile(new PeerFileID(channel.getAddressAsString()));
                channel.send(destination,file);
                logger.info("Sent a File to {}",destination.toStringLong());
            }catch (Exception e) {
                logger.error("File not sent!");
            }
        }
    }

    private void sendChunk(PeerFile request) {
        UUID destination = null;
        System.out.println("Received a request");
        // Gets the destination by comparing the request's GUID with every GUID in the cluster
        for (Address address : channel.getView().getMembers()) {
            UUID addressUUID = (UUID) address;
            if (addressUUID.toStringLong().equals(request.getFileId().getGUID())) {
                destination = addressUUID;
            }
        }
        if (destination !=null){
            try{
                PeerFile file = new PeerFile(new PeerFileID(channel.getAddressAsString()));
                ArrayList<Chunk> incomingChunks = new ArrayList<>(request.getChunks());
                ArrayList<Chunk> localChunk = new ArrayList<>(chunks);

                for (int i = 0; i < incomingChunks.size(); i++){
                    for (int j = 0; j < localChunk.size(); j++){
                        if (localChunk.equals(incomingChunks)){
                            channel.send(new ObjectMessage(destination, localChunk.get(j)));
                            System.out.println("Sending chunk "+localChunk.get(j).getChunkNo()
                                    +" to "+destination.toStringLong());
                        }
                    }
                }
            }catch (Exception e) {
                logger.error("Chunk not sent!");
            }
        }
    }

    /**
     * Processes pending (delayed) requests
     * */
    private void processPendingRequests(){
        List<PeerFile> requestQ;
        do {
            requestQ = new ArrayList<PeerFile>(requestQueue);
            //Collections.sort(requestQ);
            for (PeerFile request : requestQ) {
                sendFile(request);
            }
        }
        while (!requestQ.equals(requestQueue));

        requestQueue.clear();
    }

    // Returns the number of connected nodes in the cluster
    private int clusterSize() {
        return channel.getView().getMembers().size();
    }


}

package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PeerFile implements PeerBox, Comparable<PeerFileID>, Serializable {

    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;
    private Collection<Chunk> chunks = new ArrayList<>();

    //private final Map<PeerFileID, PeerFile> files = new ConcurrentHashMap<>();
    JChannel channel;
    LoggingReceiver receiver = new LoggingReceiver();


    public PeerFile() {

    }

    public PeerFile(PeerFileID fileId, Collection<Chunk> chunks) {
        this.fileId = fileId;
        this.chunks = chunks;
    }

    public PeerFile(PeerFileID fileId, byte[] data) {
        this.fileId = fileId;
        this.data = data;
    }

    public PeerFile(PeerFileID fileId, List<List<byte[]>> splitedData) {
        this.fileId = fileId;
        chunks.add((Chunk) splitedData);
    }

    public PeerFile(PeerFileID fileId, byte[] data, Collection<Chunk> chunks) {
        this.fileId = fileId;
        this.data = data;
        this.chunks = chunks;
    }

    public PeerFile(PeerFileID peerFileID) {
        fileId = peerFileID;
        //channel.connect(CLUSTER_NAME);
        //channel.setReceiver(receiver);
    }

    public PeerFileID getFileId() {
        return fileId;
    }

    public void setFileId(PeerFileID fileId) {
        this.fileId = fileId;
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Collection<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(Collection<Chunk> chunks) {
        this.chunks = chunks;
    }

    public JChannel getChannel() {
        return channel;
    }

    public void setChannel(JChannel channel) {
        this.channel = channel;
    }

    /**
     * Operations:
     * - Splits `data` in BLOCK_SIZE chunks
     * - Propagates chunks to registered peers
     * - File exists -> use your imagination :)
     *
     * @param path     The local path of the file to store in peer box
     * @param replicas The number of replicas per chunk (peers per chunk?)
     * @return The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public PeerFileID save(String path, int replicas) throws PeerBoxException {

        List<byte[]> data = Collections.singletonList(this.getData());
        List<List<byte[]>> splitedData = Lists.partition(data, BLOCK_SIZE);

        int size = splitedData.lastIndexOf(data);
        for (int i = 0; i <= size; i++){
            //new Chunk(this,i, Collections.singletonList(splitedData.get(i)));
            chunks.add(new Chunk(this,i, Collections.singletonList(splitedData.get(i))));
        }

        for (Chunk chunk : chunks) {
            try{
                ArrayList<Address> receivers = new ArrayList<>(channel.getView().getMembers());
                if(receivers.isEmpty()){
                    System.out.println("There's no receivers!");
                    break;
                }
                //receivers.forEach(System.out::println);
                int v = receivers.size();
                System.out.println("There's "+v+" receivers!");
                int r=0;
                //Collections.shuffle(receivers);
                for (Address address : receivers){
                    while (r++<=replicas) {
                        channel.send(new ObjectMessage(address, this.getChunks().contains(chunk)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fileId;
    }

    /**
     * Retrieves the file designated by the specified id.
     * Expected operations are:
     * - Figure out where the file chunks are
     * - Gather and assemble all the chunks from the registered peers
     *
     * @param id The ID of the file in the PeerBox
     * @return the resulting file stored in the PeerBox system and associated metadata
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public PeerFile fetch(PeerFileID id) throws PeerBoxException {

        receiver.setState(LoggingReceiver.STATES.WAITING);

        PeerFile request = new PeerFile(id);

        try {
           /* int n = request.chunks.size();
            List<Chunk> r_chunk = (List<Chunk>) request.getChunks();
            int i =0;
            for (i=0; i <= n; i++) {
                channel.send(null, r_chunk.get(i));
            }*/
            channel.send(null,request);

        } catch (Exception e) {
            e.printStackTrace();
            receiver.setState(LoggingReceiver.STATES.READY);
            System.out.println("Waiting for chunks...");
            Sleeper.sleep(10000);
            receiver.setState(LoggingReceiver.STATES.NULL);
        }

        return request;
       // return files.get(id);
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws PeerBoxException {

        /*synchronized (files){
            files.remove(id);
        }*/

    }

    /**
     * Shows all the files stored in peer box
     *
     */
    @Override
    public void listFiles() {

        /*synchronized (files){
            for(Map.Entry<PeerFileID,PeerFile> entry: files.entrySet()){
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }*/
    }

    @Override
    public PeerFileID data(String path) throws PeerBoxException {
        return null;
    }

    /**
     * Compares requests to see which one has the earliest timestamp
     * If the timestamps can't be compared, compares the GUIDs*/
    @Override
    public int compareTo(PeerFileID other) {
        if (other.getTimestamp() < this.getFileId().getTimestamp()) {
            return -1;
        }
        else if (other.getTimestamp() > this.getFileId().getTimestamp()) {
            return 1;
        }
        else {
            return other.getGUID().compareTo(this.getFileId().getGUID());
        }
    }
}

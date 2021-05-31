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

public class PeerFile implements PeerBox, Serializable {

    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;
    private Collection<Chunk> chunks = new ArrayList<>();

    //private final Map<PeerFileID, PeerFile> files = new ConcurrentHashMap<>();
    JChannel channel;
    LoggingReceiver receiver = new LoggingReceiver();


    public PeerFile() {
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
                        channel.send(new ObjectMessage(address, this.chunks.contains(chunk)));
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
        //ArrayList<Chunk> chunkArrayList = new ArrayList<>(request.getChunks());

        try {

            ObjectMessage message = new ObjectMessage(null, request);
            channel.send(message);
            System.out.println("Waiting for chunks...");
            Sleeper.sleep(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        receiver.setState(LoggingReceiver.STATES.NULL);

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

        receiver.setState(LoggingReceiver.STATES.DELETE);
        PeerFile request = new PeerFile(id);

        try{
            ObjectMessage message = new ObjectMessage(null, request);
            channel.send(message);
            System.out.println("Sending delete msg...");
            Sleeper.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        receiver.setState(LoggingReceiver.STATES.NULL);

    }

    @Override
    public PeerFileID data(String path) throws PeerBoxException {
        return null;
    }

}

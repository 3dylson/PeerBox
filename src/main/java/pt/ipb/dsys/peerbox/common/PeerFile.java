package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Receiver;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PeerFile implements PeerBox, Comparable<PeerFileID>  {


    private PeerFileID fileId;
    private byte[] data;
    private Collection<Chunk> chunks;

    //private final Map<PeerFileID, PeerFile> files = new ConcurrentHashMap<>();
    JChannel channel;
    LoggingReceiver receiver; //= new LoggingReceiver();


    public PeerFile() {

    }

    public PeerFile(PeerFileID fileId) {
        this.fileId = fileId;
    }

    public PeerFile(PeerFileID fileId, byte[] data) {
        this.fileId = fileId;
        this.data = data;
    }

    public PeerFile(PeerFileID fileId, List<List<byte[]>> splitedData) {
        this.fileId = fileId;
        chunks.add((Chunk) splitedData);
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

        int size = splitedData.size();
        for (int i = 0; i <= size; i++){
            chunks.add((new Chunk(this,i, Collections.singletonList(splitedData.get(i)))) );
        }

        //chunks.add((Chunk) splitedData);
        //Chunk chunk = new Chunk(this.fileId,i)
        //this.getChunks().add((Chunk) splitedData);

        /*int n=0;
        while (n++<replicas){
            Chunk chunk = new Chunk(this,n, splitedData);
            chunks.add(chunk);
        }*/

        for (Chunk chunk : chunks) {
            try{
                List<Address> receivers = receiver.getMembers();
                //receivers.add((Address) receiver.getMembers());
                int r=0;
                while (r++<replicas){
                    Collections.shuffle(receivers);
//                    channel.setReceiver((Receiver) receivers.get(0));
                    channel.send(receivers.get(0),chunk);
                    receivers.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*try {
            for (Address address : receiver.getMembers()) {
                int i=0;
                while (i++<replicas) {
                    *//*Collections.shuffle(splitedData);
                    chunks.add(new Chunk(this,i,splitedData));*//*

                    PeerFile msg = new PeerFile(getFileId(), Collections.singletonList(splitedData.get(0)));
                    msg.getFileId().setPath(path);
                    channel.send(address, msg);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }*/


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
            int n = request.chunks.size();
            List<Chunk> r_chunk = (List<Chunk>) request.getChunks();
            int i =0;
            for (i=0; i <= n; i++) {
                channel.send(null, r_chunk.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
            receiver.setState(LoggingReceiver.STATES.READY);
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

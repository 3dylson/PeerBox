package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.Address;
import org.jgroups.JChannel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerFile implements PeerBox  {


    private PeerFileID fileId;
    private byte[] data;
    private Collection<Chunk> chunks;
    private final Map<PeerFileID, PeerFile> files = new ConcurrentHashMap<>();
    JChannel channel;


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

        chunks.add((Chunk) splitedData);

        /*int i=0;
        while(i++<replicas) {
            ObjectMessage msg = new ObjectMessage(null,splitedData);
            channel.send(msg);
            }*/

        try {
            for (Address address : channel.getView().getMembers()) {
                int i=0;
                while (i++<replicas) {
                    PeerFile msg = new PeerFile(getFileId(), splitedData);
                    channel.send(address, msg);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
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

        try {
            PeerFile request = new PeerFile(id);
            channel.send(null, request);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return files.get(id);
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws PeerBoxException {

        synchronized (files){
            files.remove(id);
        }

    }

    /**
     * Shows all the files stored in peer box
     *
     */
    @Override
    public void listFiles() {

        synchronized (files){
            for(Map.Entry<PeerFileID,PeerFile> entry: files.entrySet()){
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    @Override
    public PeerFileID data(String path) throws PeerBoxException {
        return null;
    }
}

package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.JChannel;
import org.jgroups.Message;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.System.exit;

public class PeerFile implements PeerBox {

    private static final String CLUSTER_NAME = "PeerBox";

    JChannel channel;

    private PeerFileID fileId;
    private byte[] data;

    private Collection<Chunk> chunks;
    private int filehash;


    public PeerFile(String pathName) throws Exception {
        File dir = new File(pathName);
        int hash =  dir.hashCode();
        this.setFilehash(hash);
        channel = new JChannel(DefaultProtocols.gossipRouter());
        channel.connect(CLUSTER_NAME);
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

    public int getFilehash() {
        return filehash;
    }

    public void setFilehash(int filehash) {
        this.filehash = filehash;
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
    public PeerFileID save(String path, int replicas) throws Exception {

        boolean bool = false;
        File file = new File(path);

        bool = file.exists();

        if(!bool){

            List<byte[]> data = Collections.singletonList(this.data);
            List<List<byte[]>> splittedData = Lists.partition(data, BLOCK_SIZE);

            int i=0;
            while(i++<replicas) {
                //Message msg = new Message(null, splittedData);
                channel.send((Message) splittedData);
            }

        } else{
            System.out.print("This file already exists!");
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
        return null;
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws PeerBoxException {

    }

    /**
     * Shows all the files stored in peer box
     *
     * @throws PeerBoxException in case the list is empty
     * @return
     */
    @Override
    public File[] listFiles() throws PeerBoxException {
        File dir = new File("dropdown");
        return null;
    }

    @Override
    public PeerFileID data(String path) throws PeerBoxException {
        return null;
    }
}

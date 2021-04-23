package pt.ipb.dsys.peerbox.common;

import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class PeerFile extends UnicastRemoteObject implements PeerBox {

    private PeerFileID fileId;

    private byte[] data;

    /**
     * Creates and exports a new UnicastRemoteObject object using an
     * anonymous port.
     *
     * <p>The object is exported with a server socket
     * created using the {@link RMISocketFactory} class.
     *
     * @throws RemoteException if failed to export object
     * @since 1.1
     */
    protected PeerFile() throws RemoteException {
        super();
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

    /**
     * Operations:
     * - Splits `path` in BLOCK_SIZE chunks
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
        if(this.data.length == 0){
            return null;
        }

        int BLOCKSIZE = this.BLOCK_SIZE;
        int numOfChunks = (int)Math.ceil((double)this.data.length / BLOCKSIZE);
        byte[][] partitions = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; i++) {
            int start = i + BLOCKSIZE;
            int length = Math.min(this.data.length - start, BLOCKSIZE);

            byte[] temp = new byte[length];
            System.arraycopy(this.data, start, temp, 0, length);
            partitions[i] = temp;
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
     */
    @Override
    public List<PeerFile> listFiles() throws PeerBoxException {
        return null;
    }
}

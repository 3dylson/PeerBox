package pt.ipb.dsys.peerbox.common;

/**
 * The main interface that specifies the operation of the PeerBox system.
 * It contains the basic functionality.
 */
public interface PeerBox {

    // Block size in bytes (default: 64k)
    int BLOCK_SIZE = 64 * 1024;

    /**
     * Operations:
     *  - Splits `path` in BLOCK_SIZE chunks
     *  - Propagates chunks to registered peers
     *  - File exists -> use your imagination :)
     * @param path The local path of the file to store in peer box
     * @param replicas The number of replicas per chunk (peers per chunk?)
     * @return The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    PeerFileID save(String path, int replicas) throws PeerBoxException;

    /**
     * Retrieves the file designated by the specified id.
     * Expected operations are:
     *  - Figure out where the file chunks are
     *  - Gather and assemble all the chunks from the registered peers
     * @param id The ID of the file in the PeerBox
     * @return the resulting file stored in the PeerBox system and associated metadata
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    PeerFile fetch(PeerFileID id) throws PeerBoxException;

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    void delete(PeerFileID id) throws PeerBoxException;


}

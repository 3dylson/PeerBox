package pt.ipb.dsys.peerbox.common;

public class Chunk {

    private PeerFile fileID;
    private int chunkNo;
    int MAX_SIZE = 64;

    public Chunk(PeerFile fileID, int chunkNo) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    public PeerFile getFileID() {
        return fileID;
    }

    public void setFileID(PeerFile fileID) {
        this.fileID = fileID;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
    }
}

package pt.ipb.dsys.peerbox.common;

import java.util.List;

public class Chunk {

    private PeerFile fileID;
    private int chunkNo;
    int MAX_SIZE = 64;
    private List<List<byte[]>> splitedData;

    public Chunk(PeerFile fileID) {
        this.fileID = fileID;
    }

    public Chunk(PeerFile fileID, int chunkNo, List<List<byte[]>> splitedData ) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.splitedData = splitedData;
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

package pt.ipb.dsys.peerbox.common;

import java.io.Serializable;
import java.util.List;

public class Chunk implements Serializable {

    public static final long serialVersionUID = 1L;

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

    public List<List<byte[]>> getSplitedData() {
        return splitedData;
    }

    public void setSplitedData(List<List<byte[]>> splitedData) {
        this.splitedData = splitedData;
    }
}

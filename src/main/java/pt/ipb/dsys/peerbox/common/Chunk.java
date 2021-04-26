package pt.ipb.dsys.peerbox.common;

public class Chunk {

    private PeerFile fileID;
    private int chunkNo;
    int MAX_SIZE = 64;

    public Chunk(PeerFile fileID, int chunkNo) {
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    public Chunk replicate(int num){
        //Chunk chunk = fileID.getData();
    }


}

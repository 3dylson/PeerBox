package pt.ipb.dsys.peerbox.common;


import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class PeerFileID implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    UUID id;
    String fileName;
    byte[] chunk;
    int chunkNumber;

    public PeerFileID(UUID id, String fileName, byte[] chunk, int chunkNumber) {
        this.id = id;
        this.fileName = fileName;
        this.chunk = chunk;
        this.chunkNumber = chunkNumber;
    }


    public UUID getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

}

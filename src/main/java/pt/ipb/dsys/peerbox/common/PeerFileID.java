package pt.ipb.dsys.peerbox.common;

import org.jgroups.util.UUID;

import java.io.Serializable;
import java.util.List;

public class PeerFileID implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    UUID id;
    String fileName;
    List<byte[]> chunk;
    int chunkNumber;

    public PeerFileID(UUID id, String fileName, List<byte[]> chunk, int chunkNumber) {
        this.id = id;
        this.fileName = fileName;
        this.chunk = chunk;
        this.chunkNumber = chunkNumber;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<byte[]> getChunk() {
        return chunk;
    }

    public void setChunk(List<byte[]> chunk) {
        this.chunk = chunk;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

}

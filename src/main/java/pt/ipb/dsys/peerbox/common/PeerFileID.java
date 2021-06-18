package pt.ipb.dsys.peerbox.common;

import org.jgroups.util.UUID;

import java.io.Serializable;

public class PeerFileID implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    UUID id;
    int chunkNumber;

    public PeerFileID(UUID id, int chunkNumber) {
        this.id = id;
        this.chunkNumber = chunkNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

}

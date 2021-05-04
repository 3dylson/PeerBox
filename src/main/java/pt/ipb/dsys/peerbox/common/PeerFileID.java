package pt.ipb.dsys.peerbox.common;

import java.io.Serializable;

public class PeerFileID implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    String GUID;
    String filename;
    String path;
    long timestamp;

    public PeerFileID() {

    }

    public PeerFileID(String GUID, String filename, String path, long timestamp) {
        this.GUID = GUID;
        this.filename = filename;
        this.path = path;
        this.timestamp = timestamp;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

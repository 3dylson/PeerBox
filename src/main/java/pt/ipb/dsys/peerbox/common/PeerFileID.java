package pt.ipb.dsys.peerbox.common;

import java.io.Serializable;

public class PeerFileID implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    protected String filename;
    protected String path;

    public PeerFileID(String filename, String path) {
        this.filename = filename;
        this.path = path;
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
}

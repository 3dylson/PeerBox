package pt.ipb.dsys.peerbox.common;

import org.jgroups.util.UUID;

import java.io.Serializable;

public class PeerFileID implements Serializable, Comparable<PeerFileID> {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    UUID ID;
    long timestamp;
    String path;

    public PeerFileID(){
        this.ID = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getID() {
        return ID;
    }

    public void setPath(String path){
        this.path = path;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /*
    * Compares filesID to see which one has the earliest timestamp
    * If the timestamps can't be compared, compares the IDs
    *
    * */
    @Override
    public int compareTo(PeerFileID other) {
        if (other.getTimestamp() < this.getTimestamp()) {
            return -1;
        }
        else if (other.getTimestamp() > this.getTimestamp()) {
            return 1;
        }
        else {
            return other.getID().compareTo(this.getID());
        }

    }



}

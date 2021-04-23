package pt.ipb.dsys.peerbox.common;

import java.io.Serializable;
import java.util.Date;

public class PeerFileID implements Serializable, Comparable<PeerFileID> {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    String ID;
    long timestamp;

    public PeerFileID( String ID, long timestamp){
        this.ID = ID;
        this.timestamp = timestamp;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
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

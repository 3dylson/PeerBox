package pt.ipb.dsys.peerbox.common;

import org.jgroups.util.UUID;

import java.io.File;
import java.io.Serializable;

public class PeerFileID implements Serializable, Comparable<PeerFileID> {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    File file;
    UUID ID;
    String name;
    String path;
    long timestamp;
    String filehash;

    public File getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = new File(file);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilehash() {
        return filehash;
    }

    public void setFilehash(String filehash) {
        this.filehash = filehash;
    }

    public UUID getID() {
        return ID;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getPath(){
        return path;
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

    public PeerFileID(String path, String name) {
        if (name != null) {
            System.out.println(path + "\\" + name);
            this.ID = UUID.randomUUID();
            this.timestamp = System.currentTimeMillis();
            this.name = name;
            this.path = path;
            this.setFilehash(Integer.toString(this.getName().hashCode() + this.getPath().hashCode()));
        }
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

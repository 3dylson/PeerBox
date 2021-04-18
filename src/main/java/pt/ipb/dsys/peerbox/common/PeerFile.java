package pt.ipb.dsys.peerbox.common;

public class PeerFile {

    private PeerFileID fileId;

    private byte[] data;

    public PeerFileID getFileId() {
        return fileId;
    }

    public void setFileId(PeerFileID fileId) {
        this.fileId = fileId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}

package pt.ipb.dsys.peerbox.provider;

import pt.ipb.dsys.peerbox.common.PeerBox;
import pt.ipb.dsys.peerbox.common.PeerBoxException;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

import java.util.List;

public class PeerProvider implements PeerBox {
    @Override
    public PeerFileID save(String path, int replicas) throws PeerBoxException {
        return null;
    }

    @Override
    public PeerFile fetch(PeerFileID id) throws PeerBoxException {
        return null;
    }

    @Override
    public void delete(PeerFileID id) throws PeerBoxException {

    }

    @Override
    public List<PeerFile> listFiles() throws PeerBoxException {
        return null;
    }
}

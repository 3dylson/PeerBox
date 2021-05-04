package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.*;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

import java.util.LinkedList;
import java.util.List;

public class LoggingReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    JChannel channel;
    final List<Address> members =new LinkedList<Address>();

    public enum STATES {
        READY, WAITING, CRITICAL
    }

    private STATES state = STATES.READY;
    private long timestamp = 0;

    public List<Address> getMembers() {
        return members;
    }

    /**
     * Called when a change in membership has occurred. No long running actions, sending of messages
     * or anything that could block should be done in this callback. If some long running action
     * needs to be performed, it should be done in a separate thread.
     * <br/>
     * Note that on reception of the first view (a new member just joined), the channel will not yet
     * be in the connected state. This only happens when {@link JChannel#connect(String)} returns.
     *
     * @param new_view
     */
    @Override
    public void viewAccepted(View new_view) {
        System.out.println("New View: " + new_view);
        Receiver.super.viewAccepted(new_view);
        synchronized (members) {
            members.clear();
            members.addAll(new_view.getMembers());
        }
    }

    @Override
    public void receive(Message msg) {
       Object message = msg.getObject();
       if(message instanceof PeerFile) {
           // Increments the logical clock timestamp whenever a request is received
           timestamp++;

           if(state == STATES.READY) {
               sendFile((PeerFile) message);
           }
       }

    }

    private void sendFile(PeerFile request) {
        UUID destination = null;

        // Gets the destination by comparing the request's GUID with every GUID in the cluster
        for (Address address : channel.getView().getMembers()) {
            UUID addressUUID = (UUID) address;
            if (addressUUID.toStringLong().equals(request.getFileId().getGUID())) {
                destination = addressUUID;
            }
        }
        if (destination !=null){
            try{
                PeerFile file = new PeerFile(new PeerFileID(channel.getAddressAsString()));
                channel.send(destination,file);
                logger.info("Sent a File to {}",destination.toStringLong());
            }catch (Exception e) {
                logger.error("File not sent!");
            }
        }
    }


}

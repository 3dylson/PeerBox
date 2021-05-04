package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.common.PeerFileID;

public class LoggingReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    JChannel channel;

    public enum STATES {
        READY, WAITING, CRITICAL
    }

    private STATES state = STATES.READY;
    private long timestamp = 0;


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

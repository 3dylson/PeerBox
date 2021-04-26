package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.common.PeerBox;
import pt.ipb.dsys.peerbox.common.PeerFile;

import java.util.List;

public class LoggingReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    public enum STATES {
        READY, WAITING, CRITICAL
    }

    private STATES state = STATES.READY;
    private long timestamp = 0;

    @Override
    public void receive(Message msg) {
        Object message = msg.getObject();
        logger.info("Message from {} to {}: {}", msg.src(), msg.dest(), msg.getObject());
    }

    @Override
    public void viewAccepted(View view) {
        logger.info("New View : {}", view);
    }



}

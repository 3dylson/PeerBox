package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    @Override
    public void receive(Message msg) {
        logger.info("Message from {} to {}: {}", msg.src(), msg.dest(), msg.getObject());
    }

    @Override
    public void viewAccepted(View new_view) {
        Receiver.super.viewAccepted(new_view);
    }

}

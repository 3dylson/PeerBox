package pt.ipb.dsys.peerbox.jgroups;

import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class LoggingReceiver implements Receiver {

    private static final Logger logger = LoggerFactory.getLogger(LoggingReceiver.class);

    /*public enum STATES {
        READY, WAITING, CRITICAL
    }

    private STATES state = STATES.READY;
    private long timestamp = 0;*/

    final List<String> state = new LinkedList<String>();

    @Override
    public void receive(Message msg) {
        logger.info("Message from {} to {}: {}", msg.src(), msg.dest(), msg.getObject());

        /*String line = msg.getSrc() + ": " + msg.getObject();
        logger.info(line);
        synchronized (state){
            state.add(line);
        }*/

    }

    @Override
    public void viewAccepted(View view) {
        logger.info("New View : {}", view);
    }

    /**
     * Allows an application to write the state to an OutputStream. After the state has
     * been written, the OutputStream doesn't need to be closed as stream closing is automatically
     * done when a calling thread returns from this callback.
     *
     * @param output The OutputStream
     * @throws Exception If the streaming fails, any exceptions should be thrown so that the state requester
     *                   can re-throw them and let the caller know what happened
     */
    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state){
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    /**
     * Allows an application to read the state from an InputStream. After the state has been
     * read, the InputStream doesn't need to be closed as stream closing is automatically done when a
     * calling thread returns from this callback.
     *
     * @param input The InputStream
     * @throws Exception If the streaming fails, any exceptions should be thrown so that the state requester
     *                   can catch them and thus know what happened
     */
    @Override
    public void setState(InputStream input) throws Exception {
        List<String> list;
        list=(List<String>)Util.objectFromStream(new DataInputStream(input));
        synchronized(state) {
            state.clear();
            state.addAll(list);
        }
        logger.info(list.size() + " messages in file history):");
        for(String str: list) {
            logger.info(str);
        }
    }
}

package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static pt.ipb.dsys.peerbox.Main.CLUSTER_NAME;

public class PeerFile implements PeerBox, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PeerFile.class);
    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;
    private int totalChunks = 0;

    JChannel channel;
    LoggingReceiver receiver;

    public PeerFile(JChannel channel, LoggingReceiver receiver) throws Exception {
        this.channel = channel;
        this.receiver = receiver;

        this.receiver.setChannel(channel);
        this.channel.setReceiver(this.receiver);
        this.channel.connect(CLUSTER_NAME);

    }


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

    public JChannel getChannel() {
        return channel;
    }

    public void setChannel(JChannel channel) {
        this.channel = channel;
    }

    public LoggingReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(LoggingReceiver receiver) {
        this.receiver = receiver;
    }

    /**
     * Operations:
     * - Splits `data` in BLOCK_SIZE chunks
     * - Propagates chunks to registered peers
     * - File exists -> use your imagination :)
     *
     * @param path     The local path of the file to store in peer box
     * @param replicas The number of replicas per chunk (peers per chunk?)
     * @return The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public PeerFileID save(String path, int replicas) throws PeerBoxException {

        Random random = new Random();

        List<byte[]> data = Collections.singletonList(this.getData());
        List<List<byte[]>> chunks = Lists.partition(data,BLOCK_SIZE);


        ArrayList<Address> receivers = new ArrayList<>(channel.getView().getMembers());
        List<UUID> chunksList = new ArrayList<>();
        if(receivers.isEmpty()){
            logger.warn("There's no receivers!");
        }

        chunks.iterator().forEachRemaining(chunk ->
        {
            UUID chunkId = UUID.randomUUID();
            chunksList.add(chunkId);
            this.totalChunks++;

            try {

                receiver.setState(LoggingReceiver.STATES.SAVE);

                for (int i = 0; i < replicas; i++) {

                    int receive = random.nextInt(receivers.size());
                    Address destination = channel.getView().getMembers().get(receive);

                    PeerFileID fileID = new PeerFileID(chunkId,path ,chunk,i);
                    channel.send(new ObjectMessage(destination,fileID));
                    //setFileId(fileID);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Sleeper.sleep(5000);
        receiver.setState(LoggingReceiver.STATES.DEFAULT);

        //setFileId(file);
        PeerFileID filePathId = new PeerFileID(null,path,null,0);
        setFileId(filePathId);

        synchronized (receiver.getFiles()){

            receiver.getFiles().put(path,chunksList);
            logger.info("File: {}, saved with {} replicas.",path,replicas);
        }
        return filePathId;
    }

    /**
     * Retrieves the file designated by the specified id.
     * Expected operations are:
     * - Figure out where the file chunks are
     * - Gather and assemble all the chunks from the registered peers
     *
     * @param id The ID of the file in the PeerBox
     * @return the resulting file stored in the PeerBox system and associated metadata
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public PeerFile fetch(PeerFileID id) throws PeerBoxException {

        String path = id.fileName;
        List<UUID> fileChunks = receiver.getFiles().get(path);

        receiver.setState(LoggingReceiver.STATES.FETCH);
        syncChunks(path, fileChunks);


        Sleeper.sleep(5000);
        receiver.setState(LoggingReceiver.STATES.DEFAULT);

        return (PeerFile) receiver.getFiles().get(path);
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws PeerBoxException {

        String path = id.fileName;
        List<UUID> fileChunks = receiver.getFiles().remove(path);

        receiver.setState(LoggingReceiver.STATES.DELETE);
        syncChunks(path, fileChunks);

        Sleeper.sleep(5000);
        receiver.setState(LoggingReceiver.STATES.DEFAULT);
    }

    private void syncChunks(String path, List<UUID> fileChunks) {
        if(fileChunks.isEmpty()) {
            logger.warn("Chunks not found");
        }
        for (int i = 0; i < fileChunks.size(); i++) {
            PeerFileID fileID = new PeerFileID(fileChunks.get(i),path,null,i);
            try {
                channel.send(new ObjectMessage(null,fileID));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

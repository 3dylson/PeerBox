package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.util.UUID;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.Serializable;
import java.util.*;

public class PeerFile implements PeerBox, Serializable {

    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;
    private int totalChunks = 0;

    JChannel channel;
    LoggingReceiver receiver;

    public PeerFile(PeerFileID fileId, byte[] data, JChannel channel, LoggingReceiver receiver) {
        this.fileId = fileId;
        this.data = data;
        this.channel = channel;
        this.receiver = receiver;
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
            System.out.println("There's no receivers!");
        }

        chunks.iterator().forEachRemaining(chunk ->
        {
            UUID chunkId = UUID.randomUUID();
            chunksList.add(chunkId);
            totalChunks++;

            for (int i = 0; i < replicas; i++) {

                try {

                    int receive = random.nextInt(receivers.size());
                    Address destination = channel.getView().getMembers().get(receive);

                    PeerFileID fileID = new PeerFileID(chunkId,chunk,i);
                    channel.send(new ObjectMessage(destination,fileID));
                    //setFileId(fileID);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        //setFileId(file);

        receiver.getFiles().put(path,chunksList);

        return fileId;
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

        return null;
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws PeerBoxException {



    }



}

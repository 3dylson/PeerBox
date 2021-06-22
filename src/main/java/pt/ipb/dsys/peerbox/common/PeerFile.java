package pt.ipb.dsys.peerbox.common;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pt.ipb.dsys.peerbox.Main.CLUSTER_NAME;
import static pt.ipb.dsys.peerbox.Main.peerBox;

public class PeerFile implements PeerBox, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PeerFile.class);
    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;
    private int totalChunks = 0;

    Map<String, OutputStream> peerFiles = new ConcurrentHashMap<>();

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

    public Map<String, OutputStream> getPeerFiles() {
        return peerFiles;
    }

    public void setPeerFiles(Map<String, OutputStream> peerFiles) {
        this.peerFiles = peerFiles;
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

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int counter) {
        totalChunks+=counter;
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
    public PeerFileID save(String path, int replicas) throws Exception {

        Random random = new Random();
        ArrayList<Address> receivers = new ArrayList<>(channel.getView().getMembers());
        List<UUID> chunksList = new ArrayList<>();

        if(receivers.size()<1){
            logger.warn("There's no receivers!");
        }

        int blockSize = BLOCK_SIZE;
        int blockCount = (data.length + blockSize - 1) / blockSize;

        byte[] chunk;

        try {

            channel.send(new ObjectMessage(null,"Save"));
            Sleeper.sleep(3000);

            for (int i = 1; i < blockCount; i++) {
                int idx = (i - 1) * blockSize;

                chunk = Arrays.copyOfRange(data, idx, idx + blockSize);

                UUID chunkId = UUID.randomUUID();
                chunksList.add(chunkId);

                for (int j = 0; j < replicas; j++) {

                    int receive = random.nextInt(receivers.size());
                    Address destination = channel.getView().getMembers().get(receive);

                    PeerFileID fileID = new PeerFileID(chunkId,path , Collections.singletonList(chunk),j);
                    ObjectMessage msg = new ObjectMessage(destination,fileID);
                    channel.send(msg);
                    //setFileId(fileID);
                    }
            }

        } finally {

            // Last chunk
            int end = -1;
            if (data.length % blockSize == 0) {
                end = data.length;
            } else {
                end = data.length % blockSize + blockSize * (blockCount - 1);
            }
            chunk = Arrays.copyOfRange(data, (blockCount - 1) * blockSize, end);

            UUID chunkId = UUID.randomUUID();
            chunksList.add(chunkId);


            receiver.receive(new ObjectMessage(null,new PeerFileID(chunkId,path , Collections.singletonList(chunk),0)));

            for (int j = 1; j <= replicas; j++) {

                int receive = random.nextInt(receivers.size());
                Address destination = channel.getView().getMembers().get(receive);

                PeerFileID fileID = new PeerFileID(chunkId,path , Collections.singletonList(chunk),j);
                ObjectMessage msg = new ObjectMessage(destination,fileID);
                channel.send(msg);
                //setFileId(fileID);
            }
        }

        setTotalChunks(chunksList.size());

        Sleeper.sleep(3000);
        channel.send(new ObjectMessage(null,"Default"));

        //setFileId(file);
        PeerFileID filePathId = new PeerFileID(null,path,null,0);
        setFileId(filePathId);



        receiver.getFiles().put(path,chunksList);
        logger.info("File: {}, saved with {} replicas of {} chunk(s).",path,replicas,getTotalChunks());


        String outputFile = new File(path).getName();
        outputFile = peerBox+outputFile;
        OutputStream out = new FileOutputStream(outputFile);
        out.write(data);
        peerFiles.put(path,out);

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
    public PeerFile fetch(PeerFileID id) throws Exception {

        String path = id.fileName;
        OutputStream out = peerFiles.get(path);

        if (out == null){
            logger.info("File not found! Requesting chunks to other peers ...");
            List<UUID> fileChunks = receiver.getFiles().get(path);

            channel.send(new ObjectMessage(null,"Fetch"));
            Sleeper.sleep(3000);
            syncChunks(path, fileChunks);
            Sleeper.sleep(3000);
            channel.send(new ObjectMessage(null,"Default"));

        }
        else{

            logger.info("File fetched: {}", out);

        }

        return null;
    }

    /**
     * Deletes all replicas of the designated PeerBox file in all the peers.
     *
     * @param id The ID of the file in the PeerBox
     * @throws PeerBoxException in case some unexpected (which?) condition happens
     */
    @Override
    public void delete(PeerFileID id) throws Exception {

        String path = id.fileName;
        List<UUID> fileChunks = receiver.getFiles().remove(path);

        channel.send(new ObjectMessage(null,"Delete"));
        Sleeper.sleep(3000);
        syncChunks(path, fileChunks);

        Sleeper.sleep(3000);
        File delFile = new File(peerBox+path);
        if (delFile.exists()){
            delFile.deleteOnExit();
        }
        peerFiles.remove(path);
        channel.send(new ObjectMessage(null,"Default"));
    }

    private void syncChunks(String path, List<UUID> fileChunks) {
        if(fileChunks == null) {
            logger.warn("Chunks not found");
            return;
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


    public void listFiles() {
        if (!peerFiles.isEmpty()) {
            peerFiles.forEach((key, value) -> logger.info("{} : {}",key,value));
        }
        else{
            logger.warn("No files to list.");
        }

    }
}

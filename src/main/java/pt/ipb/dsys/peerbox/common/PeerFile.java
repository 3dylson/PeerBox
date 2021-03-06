package pt.ipb.dsys.peerbox.common;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.Sleeper;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static pt.ipb.dsys.peerbox.Main.CLUSTER_NAME;
import static pt.ipb.dsys.peerbox.Main.peerBox;

public class PeerFile implements PeerBox, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PeerFile.class);
    public static final long serialVersionUID = 1L;


    private PeerFileID fileId;
    private byte[] data;

    private int totalChunks = 0;

    Map<String, File> peerFiles = new ConcurrentHashMap<>();
    Map<File, Integer> chunksTotal = new ConcurrentHashMap<>();
    Map<String, List<UUID>> fileChunks = new ConcurrentHashMap<>();

    JChannel channel;
    LoggingReceiver receiver;


    public PeerFile(JChannel channel, LoggingReceiver receiver) throws Exception {
        this.channel = channel;
        this.receiver = receiver;
        Sleeper.sleep(300);
        this.receiver.setChannel(channel);
        this.channel.setReceiver(this.receiver);
        this.channel.connect(CLUSTER_NAME);
    }

    public PeerFile(PeerFileID fileId, int totalChunks) {
        this.fileId = fileId;
        this.totalChunks = totalChunks;
    }

    public PeerFileID getFileId() {
        return fileId;
    }

    public JChannel getChannel() {
        return channel;
    }

    public void setFileId(PeerFileID fileId) {
        this.fileId = fileId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, File> getPeerFiles() {
        return peerFiles;
    }

    public LoggingReceiver getReceiver() {
        return receiver;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
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
        File newFile = new File(peerBox + path);

        if (receivers.size() < 2) {
            logger.warn("There's no receivers! Saving File without replicas...");
            createFileWithData(path, newFile);
            logger.info("File: {}, saved with 0 replicas of 0 chunk(s).", path);
            return null;
        } else {

            List<UUID> chunksList = new ArrayList<UUID>();

            int blockSize = BLOCK_SIZE;
            int blockCount = (data.length + blockSize - 1) / blockSize;

            byte[] chunk;

            int totalChunk = 0;

            try {

                for (int i = 1; i < blockCount; i++) {
                    int idx = (i - 1) * blockSize;

                    totalChunk++;

                    chunk = Arrays.copyOfRange(data, idx, idx + blockSize);

                    UUID chunkId = UUID.randomUUID();
                    chunksList.add(chunkId);


                    for (int j = 1; j <= replicas; j++) {

                        int receive = random.nextInt(receivers.size());
                        Address destination = channel.getView().getMembers().get(receive);
                        channel.send(new ObjectMessage(destination, "Save"));
                        Sleeper.sleep(3000);
                        channel.send(new ObjectMessage(destination, new PeerFileID(chunkId, path, chunk, j)));

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

                totalChunk++;

                UUID chunkId = UUID.randomUUID();
                chunksList.add(chunkId);

                for (int j = 1; j <= replicas; j++) {

                    int receive = random.nextInt(receivers.size());
                    Address destination = channel.getView().getMembers().get(receive);
                    channel.send(new ObjectMessage(destination, "Save"));
                    Sleeper.sleep(3000);
                    channel.send(new ObjectMessage(destination, new PeerFileID(chunkId, path, chunk, j)));
                    //setFileId(fileID);
                }
            }

            fileChunks.put(path, chunksList);
            setTotalChunks(totalChunk);
            PeerFileID filePathId = new PeerFileID(null, path, null, 0);
            setFileId(filePathId);

            ObjectMessage message = new ObjectMessage(null,new PeerFile(filePathId, totalChunk));
            //channel.send(new ObjectMessage(null, new PeerFile(filePathId, totalChunk)));
            channel.send(message);
            receiver.setState(LoggingReceiver.STATES.SAVE);
            receiver.receive(message);

            Sleeper.sleep(3000);
            receiver.setState(LoggingReceiver.STATES.DEFAULT);
            channel.send(new ObjectMessage(null, "Default"));

            createFileWithData(path, newFile);
            logger.info("File: {}, saved with {} replicas of {} chunk(s).", path, replicas, getTotalChunks());
        }


        return getFileId();
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
        File peerFile = peerFiles.get(path);

        if (peerFile == null) {
            logger.info("File not found! Requesting chunks to other peers ...");
            List<UUID> chunks = fileChunks.get(path);

            channel.send(new ObjectMessage(null, "Fetch"));
            receiver.setState(LoggingReceiver.STATES.FILE);
            Sleeper.sleep(3000);

            if (chunks == null) {
                logger.warn("Chunks not found");
                //return;
            } else {
                try {

                    for (int i = 1; i <= chunks.size(); i++) {
                        int index = i - 1;
                        PeerFileID fileID = new PeerFileID(chunks.get(index), path, null, i);
                        channel.send(new ObjectMessage(null, fileID));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {

            logger.info("Opening the file {} :", path);
            String line;

            try {

                if (!Desktop.isDesktopSupported()) {
                    logger.warn("Desktop is not supported, showing file content on terminal:");
                    // FileReader reads text files in the default encoding.
                    FileReader fileReader = new FileReader(peerFile);
                    // Always wrap FileReader in BufferedReader.
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                    }
                    bufferedReader.close();
                    return null;
                }
                Desktop desktop = Desktop.getDesktop();

                if (peerFile.exists()) {
                    desktop.open(peerFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        List<UUID> removedChunks = fileChunks.remove(path);
        File peerFile = peerFiles.get(path);
        int fileChunks = chunksTotal.get(peerFile);
        channel.send(new ObjectMessage(null, "Delete"));
        Sleeper.sleep(3000);
        if (removedChunks == null) {
            logger.warn("Chunks not found to be deleted");
            //return;
        } else {
            for (int i = 1; i <= removedChunks.size(); i++) {
                int index = i - 1;
                PeerFileID fileID = new PeerFileID(removedChunks.get(index), path, null, i);
                channel.send(new ObjectMessage(null, fileID));
            }

        }
        Sleeper.sleep(3000);
        File delFile = new File(peerBox + path);
        if (delFile.exists()) {
            delFile.delete();
            logger.info("File: {}, has been successfully deleted.", path);
        }
        peerFiles.remove(path);
        channel.send(new ObjectMessage(null, "Default"));
    }


    private void createFileWithData(String path, File newFile) {
        if (!newFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(newFile);
                outputStream.write(data);
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        peerFiles.put(path, newFile);
        chunksTotal.put(newFile, totalChunks);
    }

    public File[] readDirectory() {
        File file;
        File[] fileList = null;

        try {
            file = new File(peerBox);
            fileList = file.listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }


}

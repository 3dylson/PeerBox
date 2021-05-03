package pt.ipb.dsys.peerbox.common;

import com.google.common.collect.Lists;
import org.jgroups.JChannel;
import org.jgroups.ObjectMessage;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerFile implements PeerBox  {


    JChannel channel;
    Map<String, OutputStream> files = new ConcurrentHashMap<>();

    private Collection<Chunk> chunks;

    private PeerFileID fileId;
    private byte[] data;


    public PeerFile(PeerFileID fileId) {
        this.fileId = fileId;

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

        //getFileId().setPath(path);

        String filename = this.getFileId().getFilename();

        String filePath = this.getFileId().getPath();

       // File file = new File(filePath);
        FileOutputStream out = new FileOutputStream(filePath);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        /*file.setWritable(true);
        file.setExecutable(true);
        file.setReadable(true);*/




        System.out.print("> Write something in your file.\n*write s to save and exit*:\n");
        String fdata;
        for(;;) {
            /*byte[] buf = new byte[8096];
            out.write(buf);
            int bytes = out.write(buf);*/
            fdata = in.readLine();
            out.write(fdata.getBytes());
            if(fdata.equals("s"))
                break;
            this.setData(fdata.getBytes());
        }

        boolean bool = false;
        //PeerFile file = new PeerFile(path,null);

        //bool = file.exists();

        //if(!bool){

            List<byte[]> data = Collections.singletonList(this.getData());
            List<List<byte[]>> splitedData = Lists.partition(data, BLOCK_SIZE);
            //chunks.add((Chunk) splitedData);

            int i=0;
            while(i++<replicas) {
                //Message msg = new Message(null, splitedData);
                //channel.send((Message) splitedData);
                ObjectMessage msg = new ObjectMessage(null,splitedData);
                channel.send(msg);
            }

        /*} else{
            System.out.print("This file already exists!");
        }*/

        //Example:
        /*Person p=new Person("Bela Ban", 322649, array);
        Message msg=new ObjectMessage(dest, p);
        channel.send(msg)

        // or

        msg=new ObjectMessage(null, "hello world");
        channel.send(msg);*/

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
    public PeerFile fetch(PeerFileID id) throws Exception {

        /*Address address = id;
        channel.getAddress();
        PeerFile file = new PeerFile(ID);
        channel.send(null, file);*/

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

    /**
     * Shows all the files stored in peer box
     *
     * @return
     */
    @Override
    public File[] listFiles() {
        //File dir = new File("dropdown");
        return null;
    }

    @Override
    public PeerFileID data(String path) throws PeerBoxException {
        return null;
    }
}

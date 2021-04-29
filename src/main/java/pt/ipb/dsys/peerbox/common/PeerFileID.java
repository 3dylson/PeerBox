package pt.ipb.dsys.peerbox.common;

import org.jgroups.Global;
import org.jgroups.Header;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

public class PeerFileID extends Header implements Serializable {

    // Members will depend on the metadata specific to your implementation

    public static final long serialVersionUID = 1L;

    protected static final short ID=3500;


    protected String  filename;
    protected boolean eof;

    public PeerFileID() {} // for de-serialization

    public PeerFileID(String filename, boolean eof) {
        this.filename=filename;
        this.eof=eof;
    }


    /**
     * Returns the magic-ID. If defined in jg-magic-map.xml, the IDs need to be the same
     */
    @Override
    public short getMagicId() {
        return ID;
    }

    /**
     * Creates an instance of the class implementing this interface
     */
    @Override
    public Supplier<? extends Header> create() {
        return PeerFileID::new;
    }

    /**
     * Returns the size (in bytes) of the marshalled object
     */
    @Override
    public int serializedSize() {
        return Util.size(filename) + Global.BYTE_SIZE;
    }

    /**
     * Write the entire state of the current object (including superclasses) to outstream.
     * Note that the output stream <em>must not</em> be closed
     *
     * @param out
     */
    @Override
    public void writeTo(DataOutput out) throws IOException {
       // Util.write( filename,out);
    }

    /**
     * Read the state of the current object (including superclasses) from instream
     * Note that the input stream <em>must not</em> be closed
     *
     * @param in
     */
    @Override
    public void readFrom(DataInput in) throws IOException, ClassNotFoundException {
        //filename=(String)Util.read(in);
        eof=in.readBoolean();


    }
}

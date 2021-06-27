package pt.ipb.dsys.peerbox.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PeerUtil {

    /**
     * By checking if the @gossipHostname hostname is set (which happens only inside docker)
     * we are able to check if this is a peer inside docker
     * @param gossipHostname The name of the gossip router inside docker
     * @return Whether this is a peer running inside docker
     */
    public static boolean isPeer(String gossipHostname) {
        try {
            InetAddress.getByName(gossipHostname);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * If not running inside docker, bind to address 127.0.0.1.
     * This prevents network problems the happen (mostly) in windows
     */
    public static void localhostFix(String gossipHostname) {
        if (!isPeer(gossipHostname))
            System.setProperty("jgroups.bind_addr", "127.0.0.1");
    }


    public static byte[] trimBytes(byte[] bytes){
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }

}


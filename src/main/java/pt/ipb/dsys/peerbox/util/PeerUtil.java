package pt.ipb.dsys.peerbox.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerUtil {

    public static boolean isPeer() {
        try {
            InetAddress.getByName("gossip-router");
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static void localhostFix() {
        if (!isPeer())
            System.setProperty("jgroups.bind_addr", "127.0.0.1");
    }

}

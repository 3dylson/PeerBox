package pt.ipb.dsys.peerbox;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsHelper {

    public static String getHostName() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

}

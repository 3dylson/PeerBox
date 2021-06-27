package pt.ipb.dsys.peerbox.util;

public class Sleeper {

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignore) {
            // don't care
        }
    }

}

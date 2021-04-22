package pt.ipb.dsys.peerbox.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PeerProviderMain {

    private static final Logger logger = LoggerFactory.getLogger(PeerProviderMain.class);

    public static void main(String[] args) {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try{
            PeerProvider node = new PeerProvider();
            Registry registry = LocateRegistry.createRegistry(1099); // Should be same as gossip-Router's (12001) ??
            registry.rebind(PeerProvider.STUB_NAME, node);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        System.out.println("Provider running..."); // Will it run if catch is executed ??
    }
}

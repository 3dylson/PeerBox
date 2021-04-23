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
            System.out.println("Creating " + PeerProvider.STUB_NAME + " object...");

            PeerProvider node = new PeerProvider();
            Registry registry = LocateRegistry.createRegistry(1099); // Should be same as gossip-Router's (12001) ??

            System.out.println("Binding the node object to the registry...");
            registry.rebind(PeerProvider.STUB_NAME, node);

            System.out.println("Provider node running...");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

package pt.ipb.dsys.peerbox.jgroups;

import com.google.common.collect.Lists;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.TUNNEL;
import org.jgroups.stack.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class DefaultProtocols {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProtocols.class);

    public static List<Protocol> gossipRouter() throws UnknownHostException {
        List<Protocol> protocols = Lists.newArrayList();

        TUNNEL tunnel = new TUNNEL();
        try {
            InetAddress grAddress = InetAddress.getByName("gossip-router");
            logger.info("Found gossip router at {} (using it)", grAddress);
            tunnel.setGossipRouterHosts("gossip-router[12001]");
        } catch (UnknownHostException e) {
            tunnel.setGossipRouterHosts("127.0.0.1[12001]");
        }

        protocols.add(tunnel);
        protocols.add(new PING());

        return protocols;

    }


}

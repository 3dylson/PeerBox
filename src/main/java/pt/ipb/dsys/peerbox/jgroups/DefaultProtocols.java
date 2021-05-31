package pt.ipb.dsys.peerbox.jgroups;

import com.google.common.collect.Lists;
import org.jgroups.protocols.BARRIER;
import org.jgroups.protocols.FD_ALL3;
import org.jgroups.protocols.FD_SOCK;
import org.jgroups.protocols.FRAG2;
import org.jgroups.protocols.MERGE3;
import org.jgroups.protocols.MFC;
import org.jgroups.protocols.PING;
import org.jgroups.protocols.TUNNEL;
import org.jgroups.protocols.UFC;
import org.jgroups.protocols.UNICAST3;
import org.jgroups.protocols.VERIFY_SUSPECT;
import org.jgroups.protocols.pbcast.GMS;
import org.jgroups.protocols.pbcast.NAKACK2;
import org.jgroups.protocols.pbcast.STABLE;
import org.jgroups.protocols.pbcast.STATE_TRANSFER;
import org.jgroups.stack.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class DefaultProtocols {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProtocols.class);

    public static List<Protocol> gossipRouter(String gossipHostname, int gossipPort) throws UnknownHostException {
        List<Protocol> protocols = Lists.newArrayList();

        TUNNEL tunnel = new TUNNEL();
        try {
            InetAddress grAddress = InetAddress.getByName(gossipHostname);
            logger.info("Found gossip router at {} (using it)", grAddress);
            tunnel.setGossipRouterHosts(String.format("%s[%d]", gossipHostname, gossipPort));
        } catch (UnknownHostException e) {
            tunnel.setGossipRouterHosts(String.format("127.0.0.1[%d]", gossipPort));
        }

        protocols.add(tunnel);
        protocols.add(new PING());
        protocols.add(new NAKACK2());
        protocols.add(new GMS());

//        protocols.add(new MERGE3());
//        protocols.add(new FD_SOCK());
//        protocols.add(new FD_ALL3());
//        protocols.add(new VERIFY_SUSPECT());
//        protocols.add(new UNICAST3());
//        protocols.add(new STABLE());
//        protocols.add(new MFC());
//        protocols.add(new FRAG2());
//        protocols.add(new BARRIER());
//        protocols.add(new STATE_TRANSFER());

        return protocols;
    }

}


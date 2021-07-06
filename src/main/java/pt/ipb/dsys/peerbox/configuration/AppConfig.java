package pt.ipb.dsys.peerbox.configuration;

import org.jgroups.JChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;
import pt.ipb.dsys.peerbox.util.PeerUtil;

import static pt.ipb.dsys.peerbox.Main.CLUSTER_NAME;
import static pt.ipb.dsys.peerbox.Main.gossipHostname;

@Configuration
public class AppConfig {

    @Bean
    @ConditionalOnMissingBean
    public JChannel channel() throws Exception {
        PeerUtil.localhostFix(gossipHostname);
        return new JChannel(DefaultProtocols.gossipRouter(gossipHostname,12001));
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingReceiver receiver(JChannel channel) {
        return new LoggingReceiver(channel);
    }

    @Bean
    public PeerFile peerFile() throws Exception {
        JChannel channel1 = channel();
        LoggingReceiver receiver1 = receiver(channel1);
        channel1.setReceiver(receiver1);
        channel1.setDiscardOwnMessages(true);
        channel1.connect(CLUSTER_NAME);
        return new PeerFile(channel1,receiver1);
    }

}

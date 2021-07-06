package pt.ipb.dsys.peerbox.configuration;

import org.jgroups.JChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.DefaultProtocols;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;

import static pt.ipb.dsys.peerbox.Main.gossipHostname;

@Configuration
public class AppConfig {

    @Bean
    @ConditionalOnMissingBean
    public JChannel channel() throws Exception {
        return new JChannel(DefaultProtocols.gossipRouter(gossipHostname,12001));
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingReceiver receiver(JChannel channel) {
        return new LoggingReceiver(channel);
    }

    @Bean
    public PeerFile peerFile(JChannel channel, LoggingReceiver receiver) throws Exception {
        return  new PeerFile(channel,receiver);
    }

}

package pt.ipb.dsys.peerbox;

import org.jgroups.JChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.ipb.dsys.peerbox.common.PeerFile;
import pt.ipb.dsys.peerbox.jgroups.LoggingReceiver;

@Configuration
public class PeerboxConfiguration {
    @Bean
    public PeerFile peerFile(JChannel channel, LoggingReceiver receiver) throws Exception {
        return  new PeerFile(channel,receiver);
    }

    @Bean
    @ConditionalOnMissingBean
    public JChannel channel() throws Exception {
        return new JChannel();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingReceiver receiver(JChannel channel) {
        return new LoggingReceiver(channel);
    }
}

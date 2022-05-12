package io.nuvalence.platform.audit.service.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
@SpringBootTest(properties = {"spring.cloud.gcp.pubsub.enable=true"})
@ActiveProfiles("test")
class PubSubConfigTest {
    @Autowired
    @Qualifier("pubSubInputChannel")
    MessageChannel inputChannel;

    @Autowired private PubSubInboundChannelAdapter pubSubInboundChannelAdapter;

    @Test
    void messageChannelAdapter_shouldConfigureOutputChannel() {
        Assertions.assertEquals(inputChannel, pubSubInboundChannelAdapter.getOutputChannel());
    }

    @Test
    void messageChannelAdapter_ShouldSetAutoAck() {
        Assertions.assertEquals(AckMode.AUTO_ACK, pubSubInboundChannelAdapter.getAckMode());
    }
}

package io.nuvalence.platform.audit.service.config;

import io.nuvalence.platform.audit.service.service.PubSubService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configures PubSub IO.
 */
@Configuration
@RequiredArgsConstructor
public class PubSubConfig {
    private final PubSubService pubSubService;

    @Value("${spring.cloud.gcp.pubsub.topic}")
    private String topic;

    @Value("${spring.cloud.gcp.pubsub.subscription}")
    private String subscription;

    /**
     * Creates messaging channel for messages retrieved from PubSub.
     *
     * @return Channel
     */
    @Bean
    public MessageChannel pubSubInputChannel() {
        return new DirectChannel();
    }

    /**
     * Creates an adapter that listens for PubSub messages and sends them to a spring channel.
     *
     * @param inputChannel   Channel for listener to write to
     * @param pubSubTemplate PubSub Message Template
     * @return Message Adapter
     */
    @Bean
    @ConditionalOnProperty(
            value = "spring.cloud.gcp.pubsub.enable",
            havingValue = "true",
            matchIfMissing = true)
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("pubSubInputChannel") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {

        PubSubInboundChannelAdapter adapter =
                new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(inputChannel);
        adapter.setAckMode(AckMode.AUTO_ACK);
        return adapter;
    }

    /**
     * Handler for inbound PubSub Messages.
     *
     * @return Message Handler
     */
    @Bean
    @ServiceActivator(inputChannel = "pubSubInputChannel")
    public MessageHandler messageReceiver() {
        return pubSubService::process;
    }

    /**
     * Creates an outbound adapter to push messages to PubSub.
     *
     * @param pubSubTemplate PubSub Message Template.
     * @return Message Handler
     */
    @Bean
    @ConditionalOnProperty(
            value = "spring.cloud.gcp.pubsub.enable",
            havingValue = "true",
            matchIfMissing = true)
    @ServiceActivator(inputChannel = "pubSubOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubSubTemplate) {
        return new PubSubMessageHandler(pubSubTemplate, topic);
    }

    /**
     * *FOR LOCAL DEVELOPMENT* Creates an outbound adapter to push messages to the pubSubInputChannel.
     * Bypasses GCP Pub/SUB.
     *
     * @param inputChannel Channel to publish to
     * @return Message Handler
     */
    @Bean
    @ConditionalOnProperty(value = "spring.cloud.gcp.pubsub.enable", havingValue = "false")
    @ServiceActivator(inputChannel = "pubSubOutputChannel")
    public MessageHandler localMessageSender(
            @Qualifier("pubSubInputChannel") MessageChannel inputChannel) {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String payload = (String) message.getPayload();
                Message<byte[]> serializedMessage =
                        MessageBuilder.createMessage(
                                payload.getBytes(StandardCharsets.UTF_8), message.getHeaders());
                inputChannel.send(serializedMessage);
            }
        };
    }

    /**
     * Default Interface for outbound messages.
     */
    @MessagingGateway(defaultRequestChannel = "pubSubOutputChannel")
    public interface PubSubOutboundGateway {
        void sendToPubSub(String data) throws IOException;
    }
}

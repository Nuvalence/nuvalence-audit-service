package io.nuvalence.platform.audit.service.config;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;

import io.nuvalence.platform.audit.service.service.PubSubService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
@SpringBootTest(properties = {"spring.cloud.gcp.pubsub.enable=false"})
@ActiveProfiles("test")
public class PubSubConfigLocalTest {
    @Autowired
    @Qualifier("pubSubInputChannel")
    MessageChannel inputChannel;

    @MockBean PubSubService mockPubSubService;

    @Autowired private MessageHandler localMessageSender;

    @Test
    void localMessageSender_shouldPublishToInputChannel() {
        Message<String> testMessage = MessageBuilder.withPayload("test").build();

        localMessageSender.handleMessage(testMessage);

        ArgumentCaptor<Message<byte[]>> messageCapture = ArgumentCaptor.forClass(Message.class);
        verify(mockPubSubService).process(messageCapture.capture());
        assertArrayEquals(
                "test".getBytes(StandardCharsets.UTF_8), messageCapture.getValue().getPayload());
    }
}

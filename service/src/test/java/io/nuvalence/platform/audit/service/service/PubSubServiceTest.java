package io.nuvalence.platform.audit.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.platform.audit.service.config.PubSubConfig;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class PubSubServiceTest {
    private static final String serializedString = "SERIALIZED DATA";

    @Mock private AuditEventRepository mockRepository;
    @Mock private PubSubConfig.PubSubOutboundGateway mockGateway;
    @Mock private ObjectMapper mockMapper;

    private PubSubService service;

    @BeforeEach
    public void beforeEach() {
        service = new PubSubService(mockGateway, mockRepository, mockMapper);
    }

    @Test
    public void publish_shouldWriteToGateway() throws IOException {
        AuditEventEntity event = new AuditEventEntity();

        Mockito.when(mockMapper.writeValueAsString(event)).thenReturn(serializedString);
        service.publish(event);

        Mockito.verify(mockGateway).sendToPubSub(serializedString);
    }

    @Test
    public void publish_shouldPassGeneratedEventIdToGateway() throws IOException {
        var initial = new UUID(0, 0);

        AuditEventEntity event = new AuditEventEntity();
        event.setEventId(initial);

        Mockito.when(mockMapper.writeValueAsString(event))
                .thenAnswer(arg -> ((AuditEventEntity) arg.getArgument(0)).getEventId().toString());

        var result = service.publish(event).getEventId();

        // Ensure the original value was overwritten, and make sure the overwritten value was passed
        // to the gateway
        Assertions.assertNotEquals(initial, result);
        Mockito.verify(mockGateway).sendToPubSub(result.toString());
    }

    @Test
    public void publish_shouldFallbackToRepository_whenMappingExceptionIsThrown()
            throws JsonProcessingException {
        AuditEventEntity event = new AuditEventEntity();

        Mockito.when(mockMapper.writeValueAsString(event)).thenThrow(JsonProcessingException.class);

        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> service.publish(event)),
                () -> Mockito.verify(mockRepository).save(event),
                () -> Mockito.verifyNoInteractions(mockGateway));
    }

    @Test
    public void publish_shouldFallbackToRepository_whenMessagingExceptionIsThrown()
            throws IOException {
        AuditEventEntity event = new AuditEventEntity();

        Mockito.when(mockMapper.writeValueAsString(event)).thenReturn(serializedString);

        Mockito.doThrow(new MessagingException("TEST EXCEPTION"))
                .when(mockGateway)
                .sendToPubSub(serializedString);

        Assertions.assertAll(
                () -> Assertions.assertDoesNotThrow(() -> service.publish(event)),
                () -> Mockito.verify(mockRepository).save(event));
    }

    @Test
    public void process_shouldPersistMessages() throws IOException {
        var message = new GenericMessage<>(serializedString.getBytes(StandardCharsets.UTF_8));
        var event = new AuditEventEntity();

        Mockito.when(mockMapper.readValue(message.getPayload(), AuditEventEntity.class))
                .thenReturn(event);

        service.process(message);
        Mockito.verify(mockRepository).save(event);
    }

    @Test
    public void process_shouldIgnoreDuplicateKeys() throws IOException {
        var message = new GenericMessage<>(serializedString.getBytes(StandardCharsets.UTF_8));
        var event = new AuditEventEntity();

        Mockito.when(mockMapper.readValue(message.getPayload(), AuditEventEntity.class))
                .thenReturn(event);
        Mockito.when(mockRepository.save(event))
                .thenThrow(new DuplicateKeyException("TEST EXCEPTION"));

        Assertions.assertDoesNotThrow(() -> service.process(message));
    }

    @Test
    public void process_shouldThrowIfDeserializationFails() throws IOException {
        var message = new GenericMessage<>(serializedString.getBytes(StandardCharsets.UTF_8));

        Mockito.when(mockMapper.readValue(message.getPayload(), AuditEventEntity.class))
                .thenThrow(new IOException());

        Assertions.assertThrows(RuntimeException.class, () -> service.process(message));
    }
}

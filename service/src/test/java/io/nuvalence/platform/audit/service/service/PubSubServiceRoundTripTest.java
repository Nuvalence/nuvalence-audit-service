package io.nuvalence.platform.audit.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.platform.audit.service.config.PubSubConfig;
import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.StateChangeEventEntity;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Tests PubSubService with a real mapper to ensure round trip serialization works correctly.
 */
@SpringBootTest(properties = {"spring.cloud.gcp.pubsub.enable=true"})
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class PubSubServiceRoundTripTest {
    @Mock private AuditEventRepository mockRepository;
    @Mock private PubSubConfig.PubSubOutboundGateway mockGateway;
    @Autowired private ObjectMapper mapper;

    private PubSubService service;

    @BeforeEach
    public void beforeEach() {
        service = new PubSubService(mockGateway, mockRepository, mapper);
    }

    @Test
    public void service_shouldRoundTrip_AuditEventEntity() throws IOException {
        var event = new AuditEventEntity();
        event.setBusinessObjectId(UUID.randomUUID());
        event.setTimestamp(OffsetDateTime.now());
        event.setSummary("Test Summary");
        event.setSchema("https://www.example.com/test/uri");

        var result = roundTripAuditEvent(event);

        assertThat(result).usingRecursiveComparison().isEqualTo(event);
    }

    @Test
    public void service_shouldRoundTrip_ActivityEventEntity() throws IOException {
        var event = new ActivityEventEntity();
        event.setActivityType("Test Type");

        var result = roundTripAuditEvent(event);

        Assertions.assertEquals(event.getActivityType(), result.getActivityType());
    }

    @Test
    public void service_shouldRoundTrip_StateChangeEventEntity() throws IOException {
        var event = new StateChangeEventEntity();
        event.setNewState("New State");
        event.setOldState("Old State");

        var result = roundTripAuditEvent(event);

        Assertions.assertEquals(event.getNewState(), result.getNewState());
        Assertions.assertEquals(event.getOldState(), result.getOldState());
    }

    private <T extends AuditEventEntity> T roundTripAuditEvent(T entity) throws IOException {
        service.publish(entity);
        var sent = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mockGateway).sendToPubSub(sent.capture());

        // Simulate PubSub by invoking process with the same string sent to the gateway
        service.process(new GenericMessage<>(sent.getValue().getBytes(StandardCharsets.UTF_8)));

        var received = ArgumentCaptor.forClass(AuditEventEntity.class);
        Mockito.verify(mockRepository).save(received.capture());

        return (T) received.getValue();
    }
}

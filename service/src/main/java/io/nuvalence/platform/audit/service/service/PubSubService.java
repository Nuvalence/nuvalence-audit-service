package io.nuvalence.platform.audit.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.platform.audit.service.config.PubSubConfig;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

/**
 * Handle PubSub writes & callbacks.
 */
@Service
@RequiredArgsConstructor
public class PubSubService {
    private static final Log LOG = LogFactory.getLog(PubSubService.class);
    private final PubSubConfig.PubSubOutboundGateway messagingGateway;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper mapper;

    /**
     * Serialize and write entity to PubSub.
     *
     * @param entity Audit entity to write to PubSub
     * @return original entity
     */
    public AuditEventEntity publish(AuditEventEntity entity) {
        entity.setEventId(UUID.randomUUID());

        try {
            String str = mapper.writeValueAsString(entity);
            messagingGateway.sendToPubSub(str);
        } catch (IOException | MessagingException ex) {
            LOG.warn(
                    "PubSub message could not be written, writing message directly to database",
                    ex);
            auditEventRepository.save(entity);
        }

        return entity;
    }

    /**
     * Handle incoming PubSub message.
     *
     * @param message Incoming PubSub Message to process & persist.
     */
    public void process(Message<?> message) {
        AuditEventEntity entity = unsafeParseEntity(message);

        try {
            auditEventRepository.save(entity);
            LOG.info(String.format("PubSub message processed/persisted - %s", entity.getEventId()));
        } catch (DuplicateKeyException ex) {
            LOG.info("Duplicate PubSub message ignored", ex);
        }
    }

    /**
     * Parses an entity from a pub/sub message;
     * throws a runtime exception if the value cannot be parsed.
     *
     * @param message pub/sub message
     * @return parsed entity
     */
    private AuditEventEntity unsafeParseEntity(Message<?> message) {
        try {
            var payload = (byte[]) message.getPayload();
            return mapper.readValue(payload, AuditEventEntity.class);
        } catch (IOException ex) {
            LOG.error("Error parsing message from PubSub", ex);
            throw new RuntimeException(ex);
        }
    }
}

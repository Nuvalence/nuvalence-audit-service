package io.nuvalence.platform.audit.service.controllers;

import io.nuvalence.platform.audit.service.generated.controllers.V1ApiDelegate;
import io.nuvalence.platform.audit.service.generated.models.AuditEventId;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.AuditEventsPage;
import io.nuvalence.platform.audit.service.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Controller layer for audit service.
 */
@Service
@RequiredArgsConstructor
public class V1ApiDelegateImpl implements V1ApiDelegate {
    private final AuditEventService auditEventService;

    @Override
    public ResponseEntity<AuditEventsPage> getEvents(
            String businessObjectType,
            UUID businessObjectId,
            String sortOrder,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer pageNumber,
            Integer pageSize) {
        return ResponseEntity.ok(
                auditEventService.findAuditEvents(
                        businessObjectType,
                        businessObjectId,
                        startTime,
                        endTime,
                        pageNumber,
                        pageSize,
                        sortOrder));
    }

    @Override
    public ResponseEntity<AuditEventId> postEvent(
            String businessObjectType, UUID businessObjectId, AuditEventRequest body) {
        AuditEventId eventId =
                auditEventService.addAuditEvent(businessObjectType, businessObjectId, body);
        return ResponseEntity.status(201).body(eventId);
    }
}

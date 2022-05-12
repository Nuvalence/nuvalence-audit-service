package io.nuvalence.platform.audit.service.service;

import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.generated.models.AuditEventId;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.AuditEventsPage;
import io.nuvalence.platform.audit.service.mapper.AuditEventMapper;
import io.nuvalence.platform.audit.service.mapper.PagingMetadataMapper;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service layer to manage audit events.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final AuditEventMapper auditEventMapper;
    private final PagingMetadataMapper pagingMetadataMapper;
    private final PubSubService pubSubService;

    private static void checkTimeRange(OffsetDateTime startTime, OffsetDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw ApiException.Builder.badRequest(
                    "The startTime cannot be greater than the endTime.");
        }
    }

    static PageRequest createPageable(Integer pageNumber, Integer pageSize, String sortOrder) {
        try {
            return PageRequest.of(
                    pageNumber, pageSize, Sort.Direction.fromString(sortOrder), "timestamp");
        } catch (IllegalArgumentException e) {
            throw ApiException.Builder.badRequest(e.getMessage());
        }
    }

    /**
     * Queries audit events from db.
     *
     * @param businessObjectType Type of business object.
     * @param businessObjectId   Unique identifier for a business object of the specified type.
     * @param startTime          Specifies a start time (inclusive) for filtering results to events which occurred at
     *                           or after the specified time.
     * @param endTime            Specifies an end time (exclusive)for filtering results to events which occurred before
     *                           the specified time.
     * @param pageNumber         Results page number.
     * @param pageSize           Results page size.
     * @param sortOrder          Controls whether results are returned in chronologically ascending or descending order.
     * @return page object containing db query results and pagination metadata
     */
    @Transactional(readOnly = true)
    public AuditEventsPage findAuditEvents(
            String businessObjectType,
            UUID businessObjectId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Integer pageNumber,
            Integer pageSize,
            String sortOrder) {
        checkTimeRange(startTime, endTime);
        Page<AuditEventEntity> page =
                auditEventRepository.findAll(
                        businessObjectType,
                        businessObjectId,
                        startTime,
                        endTime,
                        createPageable(pageNumber, pageSize, sortOrder));

        return createAuditEventsPage(page);
    }

    private AuditEventsPage createAuditEventsPage(Page<AuditEventEntity> page) {
        return new AuditEventsPage()
                .events(auditEventMapper.fromEntities(page.getContent()))
                .pagingMetadata(pagingMetadataMapper.toPagingMetadata(page));
    }

    /**
     * Creates an audit event for a specific entity.
     *
     * @param businessObjectType business object type
     * @param businessObjectId   business object id (unique within the specified business object type)
     * @param auditEventRequest  audit event data
     * @return event identifier for the created event
     */
    public AuditEventId addAuditEvent(
            String businessObjectType, UUID businessObjectId, AuditEventRequest auditEventRequest) {

        AuditEventEntity entity =
                auditEventMapper.toEntity(businessObjectId, businessObjectType, auditEventRequest);

        var result = pubSubService.publish(entity);
        return new AuditEventId().eventId(result.getEventId());
    }
}

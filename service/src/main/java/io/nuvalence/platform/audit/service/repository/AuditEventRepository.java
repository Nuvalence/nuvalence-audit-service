package io.nuvalence.platform.audit.service.repository;

import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity_;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.criteria.Predicate;

/**
 * Repository for audit events.
 */
public interface AuditEventRepository
        extends CrudRepository<AuditEventEntity, UUID>, JpaSpecificationExecutor<AuditEventEntity> {

    /**
     * JPA Specification for querying audit events in <code>findAll()</code> method.
     *
     * @param entityId   Unique identifier for an entity of the requested type.
     * @param entityType Type of entity.
     * @param startTime  Specifies a start time (inclusive) for filtering results to events which occurred at or after
     *                   the specified time.
     * @param endTime    Specifies an end time (exclusive)for filtering results to events which occurred before the
     *                   specified time.
     * @return Specification to be used to query audit events.
     */
    default Specification<AuditEventEntity> findAllSpec(
            UUID entityId, String entityType, OffsetDateTime startTime, OffsetDateTime endTime) {
        return (root, query, builder) -> {
            Predicate predicate =
                    builder.and(
                            builder.equal(root.get(AuditEventEntity_.businessObjectId), entityId),
                            builder.equal(
                                    root.get(AuditEventEntity_.businessObjectType), entityType));

            if (startTime != null) {
                predicate =
                        builder.and(
                                predicate,
                                builder.greaterThanOrEqualTo(
                                        root.get(AuditEventEntity_.timestamp), startTime));
            }
            if (endTime != null) {
                predicate =
                        builder.and(
                                predicate,
                                builder.lessThan(root.get(AuditEventEntity_.timestamp), endTime));
            }
            return predicate;
        };
    }

    default Page<AuditEventEntity> findAll(
            String entityType,
            UUID entityId,
            OffsetDateTime start,
            OffsetDateTime end,
            Pageable pageable) {
        return findAll(findAllSpec(entityId, entityType, start, end), pageable);
    }

    Page<AuditEventEntity> findAll(Specification<AuditEventEntity> spec, Pageable pageable);
}

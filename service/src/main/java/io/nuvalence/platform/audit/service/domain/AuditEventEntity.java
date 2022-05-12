package io.nuvalence.platform.audit.service.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.nuvalence.platform.audit.service.generated.models.AuditEventDataBase;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * Audit event entity.
 */
@Getter
@Setter
@Entity
@Table(name = "audit_events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class AuditEventEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID eventId;

    @Column(length = 1024)
    private String schema;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private AuditEventDataBase.TypeEnum type;

    @Column(length = 32)
    private UUID businessObjectId;

    @Column(length = 64)
    private String businessObjectType;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    private String summary;

    @Column(length = 1024)
    private String systemOfRecord;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(
            name = "audit_events_related_business_objects",
            joinColumns = @JoinColumn(name = "audit_event_id"))
    @Column(name = "related_business_object", length = 1024, nullable = false)
    private Set<String> relatedBusinessObjects;

    @Embedded private RequestContext requestContext;
}

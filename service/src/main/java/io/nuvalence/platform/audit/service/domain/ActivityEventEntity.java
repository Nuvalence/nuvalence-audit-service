package io.nuvalence.platform.audit.service.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * An audit event which indicates some activity ocurred on some entity.
 */
@Getter
@Setter
@Entity
public class ActivityEventEntity extends AuditEventEntity {
    private String activityType;
    @Lob private String data;
}

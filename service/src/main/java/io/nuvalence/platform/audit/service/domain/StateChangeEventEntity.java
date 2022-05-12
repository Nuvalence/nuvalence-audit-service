package io.nuvalence.platform.audit.service.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * An audit event which indicates an entity's state has changed.
 */
@Getter
@Setter
@Entity
public class StateChangeEventEntity extends AuditEventEntity {
    @Lob private String newState;
    @Lob private String oldState;
}

package io.nuvalence.platform.audit.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Request context for audit event.
 */
@Embeddable
@Getter
@Setter
public class RequestContext {
    @Column(length = 36)
    private UUID userId;

    @Column(length = 36)
    private UUID tenantId;

    @Column(length = 36)
    private UUID originatorId;

    @Column(length = 36)
    private UUID requestId;

    @Column(length = 36)
    private UUID traceId;

    @Column(length = 36)
    private UUID spanId;
}

package io.nuvalence.platform.audit.service.mapper;

import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mappings that can be reused for all <code>AuditEventEntity</code> subclasses.
 */
@Retention(RetentionPolicy.CLASS)
@Mappings({
    @Mapping(target = "businessObjectId", source = "businessObjectId"),
    @Mapping(target = "businessObjectType", source = "businessObjectType"),
    @Mapping(target = "schema", source = "request.eventData.schema"),
    @Mapping(target = "type", source = "request.eventData.type"),
    @Mapping(target = "timestamp", source = "request.timestamp"),
    @Mapping(target = "summary", source = "request.summary"),
    @Mapping(target = "systemOfRecord", source = "request.links.systemOfRecord"),
    @Mapping(target = "relatedBusinessObjects", source = "request.links.relatedBusinessObjects"),
    @Mapping(target = "requestContext", source = "request.requestContext")
})
public @interface ToAuditEventEntity {}

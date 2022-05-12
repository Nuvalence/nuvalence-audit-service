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
    @Mapping(target = "eventId", source = "entity.eventId"),
    @Mapping(target = "businessObject.id", source = "entity.businessObjectId"),
    @Mapping(target = "businessObject.type", source = "entity.businessObjectType"),
    @Mapping(target = "eventData.schema", source = "entity.schema"),
    @Mapping(target = "eventData.type", source = "entity.type"),
    @Mapping(target = "timestamp", source = "entity.timestamp"),
    @Mapping(target = "summary", source = "entity.summary"),
    @Mapping(target = "links.systemOfRecord", source = "entity.systemOfRecord"),
    @Mapping(target = "links.relatedBusinessObjects", source = "entity.relatedBusinessObjects"),
    @Mapping(target = "requestContext", source = "entity.requestContext")
})
public @interface FromAuditEventEntity {}

package io.nuvalence.platform.audit.service.mapper;

import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.StateChangeEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.generated.models.ActivityEventData;
import io.nuvalence.platform.audit.service.generated.models.AuditEvent;
import io.nuvalence.platform.audit.service.generated.models.AuditEventDataBase;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.StateChangeEventData;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Maps entity to Api models using MapStruct library.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditEventMapper {
    @ToAuditEventEntity
    ActivityEventEntity toActivityEventEntity(
            UUID businessObjectId, String businessObjectType, AuditEventRequest request);

    /**
     * Sets activity audit event fields.
     *
     * @param request audit event request
     * @param entity  entity
     */
    @AfterMapping
    default void toActivityEventEntity(
            AuditEventRequest request, @MappingTarget ActivityEventEntity entity) {
        if (!(request.getEventData() instanceof ActivityEventData)) {
            throw ApiException.Builder.badRequest(
                    "Invalid eventData type: " + request.getEventData().getClass().getName());
        }

        ActivityEventData activityEventData = (ActivityEventData) request.getEventData();
        entity.setActivityType(activityEventData.getActivityType());
        entity.setData(activityEventData.getData());
    }

    @ToAuditEventEntity
    StateChangeEventEntity toStateChangeEventEntity(
            UUID businessObjectId, String businessObjectType, AuditEventRequest request);

    /**
     * Sets state change audit event fields.
     *
     * @param request audit event request
     * @param entity  entity
     */
    @AfterMapping
    default void toStateChangeEventEntity(
            AuditEventRequest request, @MappingTarget StateChangeEventEntity entity) {
        if (!(request.getEventData() instanceof StateChangeEventData)) {
            throw ApiException.Builder.badRequest(
                    "Invalid eventData type: " + request.getEventData().getClass().getName());
        }

        StateChangeEventData stateChangeEventData = (StateChangeEventData) request.getEventData();
        entity.setOldState(stateChangeEventData.getOldState());
        entity.setNewState(stateChangeEventData.getNewState());
    }

    /**
     * Converts request to entity for both activity and state change types.
     *
     * @param businessObjectId   Unique identifier for a business object of the specified type.
     * @param businessObjectType Type of business object.
     * @param request    audit event request
     * @return audit event entity
     */
    default AuditEventEntity toEntity(
            UUID businessObjectId, String businessObjectType, AuditEventRequest request) {
        return Optional.ofNullable(request)
                .filter(req -> req.getEventData() != null)
                .filter(req -> req.getEventData().getType() != null)
                .map(
                        req ->
                                req.getEventData().getType()
                                                == AuditEventDataBase.TypeEnum.ACTIVITYEVENTDATA
                                        ? toActivityEventEntity(
                                                businessObjectId, businessObjectType, request)
                                        : toStateChangeEventEntity(
                                                businessObjectId, businessObjectType, request))
                .orElse(null);
    }

    @FromAuditEventEntity
    AuditEvent fromActivityEventEntity(ActivityEventEntity entity);

    /**
     * Sets activity audit event fields.
     *
     * @param entity audit event entity
     * @param model  model
     */
    @AfterMapping
    default void fromActivityEventEntity(
            ActivityEventEntity entity, @MappingTarget AuditEvent model) {
        ActivityEventData activityEventData = new ActivityEventData();
        activityEventData.setSchema(model.getEventData().getSchema());
        activityEventData.setType(model.getEventData().getType());
        activityEventData.setActivityType(entity.getActivityType());
        activityEventData.setData(entity.getData());
        model.setEventData(activityEventData);
    }

    @FromAuditEventEntity
    AuditEvent fromStateChangeEventEntity(StateChangeEventEntity entity);

    /**
     * Sets activity audit event fields.
     *
     * @param entity audit event entity
     * @param model  model
     */
    @AfterMapping
    default void fromStateChangeEventEntity(
            StateChangeEventEntity entity, @MappingTarget AuditEvent model) {
        StateChangeEventData stateChangeEventData = new StateChangeEventData();
        stateChangeEventData.setSchema(model.getEventData().getSchema());
        stateChangeEventData.setType(model.getEventData().getType());
        stateChangeEventData.setOldState(entity.getOldState());
        stateChangeEventData.setNewState(entity.getNewState());
        model.setEventData(stateChangeEventData);
    }

    /**
     * Converts entity to model for both activity and state change types.
     *
     * @param entity audit event entity
     * @return audit event model
     */
    default AuditEvent fromEntity(AuditEventEntity entity) {
        return Optional.ofNullable(entity)
                .map(
                        auditEventEntity -> {
                            if (auditEventEntity instanceof ActivityEventEntity) {
                                return fromActivityEventEntity(
                                        (ActivityEventEntity) auditEventEntity);
                            } else {
                                return fromStateChangeEventEntity(
                                        (StateChangeEventEntity) auditEventEntity);
                            }
                        })
                .orElse(null);
    }

    default List<AuditEvent> fromEntities(List<AuditEventEntity> entities) {
        return entities.stream().map(this::fromEntity).collect(Collectors.toList());
    }
}

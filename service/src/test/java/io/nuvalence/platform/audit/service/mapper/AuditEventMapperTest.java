package io.nuvalence.platform.audit.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.domain.StateChangeEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.generated.models.ActivityEventData;
import io.nuvalence.platform.audit.service.generated.models.AuditEvent;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.BusinessObjectMetadata;
import io.nuvalence.platform.audit.service.generated.models.StateChangeEventData;
import io.nuvalence.platform.audit.service.utils.SamplesUtil;
import io.nuvalence.platform.audit.service.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize"})
class AuditEventMapperTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID ENTITY_ID = UUID.fromString("1190241c-5eae-11ec-bf63-0242ac130002");
    private static final String ENTITY_TYPE = "orders";

    private AuditEventMapper auditEventMapper;

    @BeforeEach
    void setUp() {
        auditEventMapper = Mappers.getMapper(AuditEventMapper.class);
    }

    @Test
    void toEntity_Activity() throws Exception {
        AuditEventRequest request = TestUtil.Data.ACTIVITY_REQUEST.readJson();
        ActivityEventEntity entity =
                SamplesUtil.readJsonFile(TestUtil.ACTIVITY_ENTITY_JSON, ActivityEventEntity.class);

        assertThat(auditEventMapper.toEntity(ENTITY_ID, ENTITY_TYPE, request))
                .usingRecursiveComparison()
                .isEqualTo(entity);
    }

    @Test
    void toActivityEventEntity_givenStateChangeData_ShouldThrow() {
        var request = new AuditEventRequest().eventData(new StateChangeEventData());
        Assertions.assertThrows(
                ApiException.class,
                () -> auditEventMapper.toActivityEventEntity(ENTITY_ID, ENTITY_TYPE, request));
    }

    @Test
    void toEntity_StateChange() throws Exception {
        AuditEventRequest request = TestUtil.Data.STATE_CHANGE_REQUEST.readJson();
        StateChangeEventEntity entity =
                SamplesUtil.readJsonFile(
                        TestUtil.STATE_CHANGE_ENTITY_JSON, StateChangeEventEntity.class);

        assertThat(auditEventMapper.toEntity(ENTITY_ID, ENTITY_TYPE, request))
                .usingRecursiveComparison()
                .isEqualTo(entity);
    }

    @Test
    void toStateChangeEventEntity_givenActivityData_ShouldThrow() {
        var request = new AuditEventRequest().eventData(new ActivityEventData());
        Assertions.assertThrows(
                ApiException.class,
                () -> auditEventMapper.toStateChangeEventEntity(ENTITY_ID, ENTITY_TYPE, request));
    }

    @Test
    void fromEntity_Activity() throws Exception {
        AuditEvent model = TestUtil.Data.ACTIVITY_MODEL.readJson();
        model.businessObject(createEntityModel());
        model.setEventId(EVENT_ID);

        ActivityEventEntity entity = TestUtil.Data.ACTIVITY_ENTITY.readJson();
        entity.setEventId(EVENT_ID);

        assertThat(auditEventMapper.fromEntity(entity)).usingRecursiveComparison().isEqualTo(model);
    }

    private BusinessObjectMetadata createEntityModel() {
        var businessObjectMetadata = new BusinessObjectMetadata();
        businessObjectMetadata.setId(ENTITY_ID);
        businessObjectMetadata.setType(ENTITY_TYPE);
        return businessObjectMetadata;
    }

    @Test
    void fromEntity_StateChange() throws Exception {
        AuditEvent model = TestUtil.Data.STATE_CHANGE_MODEL.readJson();
        model.businessObject(createEntityModel());
        model.setEventId(EVENT_ID);

        StateChangeEventEntity entity = TestUtil.Data.STATE_CHANGE_ENTITY.readJson();
        entity.setEventId(EVENT_ID);

        assertThat(auditEventMapper.fromEntity(entity)).usingRecursiveComparison().isEqualTo(model);
    }

    @Test
    void fromEntities_GivenListOfEntities_ShouldConvertAll() throws IOException {
        List<AuditEventEntity> entities =
                List.of(
                        TestUtil.Data.STATE_CHANGE_ENTITY.readJson(),
                        TestUtil.Data.ACTIVITY_ENTITY.readJson());

        List<AuditEvent> expected =
                List.of(
                        TestUtil.Data.STATE_CHANGE_MODEL.readJson(),
                        TestUtil.Data.ACTIVITY_MODEL.readJson());

        expected.forEach(
                e ->
                        e.setBusinessObject(
                                new BusinessObjectMetadata().type(ENTITY_TYPE).id(ENTITY_ID)));

        assertThat(auditEventMapper.fromEntities(entities))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}

package io.nuvalence.platform.audit.service.service;

import static io.nuvalence.platform.audit.service.utils.TestUtil.Data.ACTIVITY_ENTITY;
import static io.nuvalence.platform.audit.service.utils.TestUtil.Data.STATE_CHANGE_ENTITY;

import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.error.ApiException;
import io.nuvalence.platform.audit.service.generated.models.AuditEvent;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.service.generated.models.AuditEventsPage;
import io.nuvalence.platform.audit.service.generated.models.PagingMetadata;
import io.nuvalence.platform.audit.service.mapper.AuditEventMapper;
import io.nuvalence.platform.audit.service.mapper.PagingMetadataMapper;
import io.nuvalence.platform.audit.service.repository.AuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.BeanMembersShouldSerialize"})
@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {
    private static final Random random = new Random();
    private static final String DESC = "DESC";
    private final UUID businessObjectId = UUID.randomUUID();
    private final String businessObjectType = "business-object-type-" + UUID.randomUUID();
    @Mock private AuditEventMapper eventMapper;
    @Mock private AuditEventRepository mockRepository;
    @Mock private PagingMetadataMapper mockPagingMetadataMapper;
    @Mock private PubSubService mockPubSubService;
    @Mock private List<AuditEvent> expectedEvents;
    @Mock private PagingMetadata expectedMetadata;

    private AuditEventService service;

    @BeforeEach
    public void beforeEach() {
        service =
                new AuditEventService(
                        mockRepository, eventMapper, mockPagingMetadataMapper, mockPubSubService);
    }

    @Test
    public void
            addAuditEvent_GivenBusinessObjectAndEventData_ShouldReturnMapToEntityPersistAndReturnId() {
        var expectedId = UUID.randomUUID();
        AuditEventRequest request = Mockito.mock(AuditEventRequest.class);
        AuditEventEntity event = Mockito.mock(AuditEventEntity.class);
        Mockito.when(event.getEventId()).thenReturn(expectedId);
        Mockito.when(eventMapper.toEntity(businessObjectId, businessObjectType, request))
                .thenReturn(event);
        Mockito.when(mockPubSubService.publish(event)).thenReturn(event);

        var result = service.addAuditEvent(businessObjectType, businessObjectId, request);

        Assertions.assertAll(
                () ->
                        Mockito.verify(eventMapper)
                                .toEntity(businessObjectId, businessObjectType, request),
                () -> Mockito.verify(mockPubSubService).publish(event),
                () -> Assertions.assertEquals(expectedId, result.getEventId()));
    }

    @Test
    public void findAuditEvents_GivenQueryParameters_ShouldReturnPagedResult() throws IOException {
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = OffsetDateTime.now();
        int pageNumber = random.nextInt(10);
        int pageSize = 1 + random.nextInt(100);
        String sortOrder = DESC;
        PageRequest pageRequest = AuditEventService.createPageable(pageNumber, pageSize, sortOrder);
        List<AuditEventEntity> eventsFromRepository =
                List.of(ACTIVITY_ENTITY.readJson(), STATE_CHANGE_ENTITY.readJson());

        PageImpl<AuditEventEntity> page = new PageImpl<>(eventsFromRepository);
        Mockito.when(
                        mockRepository.findAll(
                                businessObjectType,
                                businessObjectId,
                                startTime,
                                endTime,
                                pageRequest))
                .thenReturn(page);
        Mockito.when(eventMapper.fromEntities(eventsFromRepository)).thenReturn(expectedEvents);
        Mockito.when(mockPagingMetadataMapper.toPagingMetadata(page)).thenReturn(expectedMetadata);

        var actual =
                service.findAuditEvents(
                        businessObjectType,
                        businessObjectId,
                        startTime,
                        endTime,
                        pageNumber,
                        pageSize,
                        sortOrder);

        var expected =
                new AuditEventsPage().events(expectedEvents).pagingMetadata(expectedMetadata);
        Assertions.assertAll(
                () ->
                        Mockito.verify(mockRepository)
                                .findAll(
                                        businessObjectType,
                                        businessObjectId,
                                        startTime,
                                        endTime,
                                        pageRequest),
                () -> Assertions.assertEquals(expected, actual));
    }

    @Test
    public void findAuditEvents_GivenInvalidTimeRangeParameters_ShouldThrowError() {
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = OffsetDateTime.now().minus(1, ChronoUnit.SECONDS);
        int pageNumber = random.nextInt(10);
        int pageSize = random.nextInt(100);

        Assertions.assertThrows(
                ApiException.class,
                () ->
                        service.findAuditEvents(
                                businessObjectType,
                                businessObjectId,
                                startTime,
                                endTime,
                                pageNumber,
                                pageSize,
                                DESC));
    }

    @Test
    public void findAuditEvents_GivenInvalidSortOrderParameter_ShouldThrowError() {
        OffsetDateTime startTime = OffsetDateTime.now();
        OffsetDateTime endTime = OffsetDateTime.now();
        int pageNumber = random.nextInt(10);
        int pageSize = random.nextInt(100);

        Assertions.assertThrows(
                ApiException.class,
                () ->
                        service.findAuditEvents(
                                businessObjectType,
                                businessObjectId,
                                startTime,
                                endTime,
                                pageNumber,
                                pageSize,
                                "FOO"));
    }
}

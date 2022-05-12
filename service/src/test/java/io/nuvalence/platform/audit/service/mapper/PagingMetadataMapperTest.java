package io.nuvalence.platform.audit.service.mapper;

import io.nuvalence.platform.audit.service.domain.AuditEventEntity;
import io.nuvalence.platform.audit.service.generated.models.PagingMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
class PagingMetadataMapperTest {
    private static final Random random = new Random();
    private final MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    private final PagingMetadataMapper mapper =
            new PagingMetadataMapper(() -> mockHttpServletRequest);

    private final int pageNumber = random.nextInt(10);
    private final int pageSize = 50 + random.nextInt(50);
    private final long totalCount = 1_000L + random.nextInt(10_000);

    private final Page<AuditEventEntity> fakePage =
            new PageImpl<>(
                    Collections.emptyList(), PageRequest.of(pageNumber, pageSize), totalCount);

    private final PagingMetadata expectedMetadata =
            new PagingMetadata().pageNumber(pageNumber).pageSize(pageSize).totalCount(totalCount);

    @Test
    public void toPagingMetadata_GivenEmptyPage_ShouldReturnReturnEmptyResultWithNoNextPage() {
        Assertions.assertEquals(
                new PagingMetadata().totalCount(0L).pageSize(0).pageNumber(0).nextPage(null),
                mapper.toPagingMetadata(Page.empty()));
    }

    @Test
    public void toPagingMetadata_givenPageWithNextPage_ShouldReturnNextPageLink() {
        expectedMetadata.setNextPage("http://localhost:80?pageNumber=" + (pageNumber + 1));
        Assertions.assertEquals(expectedMetadata, mapper.toPagingMetadata(fakePage));
    }

    @Test
    public void
            build_givenRequestWithPageNumberParamAndNextPage_ShouldReturnSameUriOverriddenPageNumberParam() {
        mockHttpServletRequest.addParameter("pageNumber", String.valueOf(pageNumber - 2));
        expectedMetadata.nextPage("http://localhost:80?pageNumber=" + (pageNumber + 1));
        Assertions.assertEquals(expectedMetadata, mapper.toPagingMetadata(fakePage));
    }

    @Test
    public void
            toPagingMetadata_givenRequestWithParamsAndNextPage_ShouldNotOverwriteOtherParameters() {
        String startTime = "2021-12-19T00:57:52.033767-05:00";
        mockHttpServletRequest.addParameter("pageNumber", "3");
        mockHttpServletRequest.addParameter("startTime", startTime);

        expectedMetadata.nextPage(
                "http://localhost:80?startTime=" + startTime + "&pageNumber=" + (pageNumber + 1));

        Assertions.assertEquals(expectedMetadata, mapper.toPagingMetadata(fakePage));
    }

    @Test
    public void
            toPagingMetadata_GivenRequestWithNextPage_ShouldBuildUriFromProvidedServletRequest() {
        String server = UUID.randomUUID().toString();
        String path = "/fakes/" + UUID.randomUUID().toString();
        mockHttpServletRequest.setScheme("https");
        mockHttpServletRequest.setServerName(server);
        mockHttpServletRequest.setServerPort(12345);
        mockHttpServletRequest.setRequestURI(path);

        String nextPage =
                String.format("https://%s:12345%s?pageNumber=%d", server, path, pageNumber + 1);
        expectedMetadata.nextPage(nextPage);

        Assertions.assertEquals(expectedMetadata, mapper.toPagingMetadata(fakePage));
    }
}

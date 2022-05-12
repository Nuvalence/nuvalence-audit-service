package io.nuvalence.platform.audit.service.mapper;

import io.nuvalence.platform.audit.service.generated.models.PagingMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * Converts page objects from JPA to the paging metadata model.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Component
public class PagingMetadataMapper {
    private final Supplier<HttpServletRequest> requestSupplier;

    public PagingMetadataMapper() {
        this(PagingMetadataMapper::getCurrentHttpRequest);
    }

    private static HttpServletRequest getCurrentHttpRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElseThrow();
    }

    /**
     * Converts the page from the repository to api paging metadata.
     * @param page page object
     * @param <T> page item type
     * @return paging metadata
     */
    public <T> PagingMetadata toPagingMetadata(Page<T> page) {
        var nextPageUri = buildNextPageUri(page.nextPageable());
        var nextPage = nextPageUri == null ? null : nextPageUri.toString();
        return new PagingMetadata()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalCount(page.getTotalElements())
                .nextPage(nextPage);
    }

    /**
     * Builds the next page uri.
     *
     * @param nextPage next page object
     * @return URI for the next page.
     */
    private URI buildNextPageUri(Pageable nextPage) {
        if (nextPage.isPaged()) {
            var request = requestSupplier.get();

            var queryParams =
                    request.getParameterMap().entrySet().stream()
                            .collect(
                                    Collectors.toMap(
                                            Map.Entry::getKey, e -> Arrays.asList(e.getValue())));

            return UriComponentsBuilder.fromUriString(request.getRequestURI())
                    .host(request.getServerName())
                    .scheme(request.getScheme())
                    .port(request.getServerPort())
                    .queryParams(new MultiValueMapAdapter<>(queryParams))
                    .replaceQueryParam("pageNumber", List.of(nextPage.getPageNumber()))
                    .build()
                    .toUri();
        } else {
            return null;
        }
    }
}

package io.nuvalence.platform.audit.utils.cucumber.transformers;

import io.cucumber.datatable.DataTable;
import io.nuvalence.platform.audit.client.generated.models.AuditEventDataBase;
import io.nuvalence.platform.audit.client.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.client.generated.models.RequestContext;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transforms a data table to a list of audit event requests.
 * @param <T> event data type
 */
public abstract class AbstractAuditEventTableTransformer<T extends AuditEventDataBase> {

    public abstract T asEventData(Map<String, String> data);

    /**
     * Transforms table.
     * @param table cucumber data table
     * @return list of transformed event requests
     */
    public List<AuditEventRequest> transform(DataTable table) {
        return table.asMaps().stream()
                .map(
                        d -> {
                            var timestamp =
                                    Optional.ofNullable(d.get("timestamp"))
                                            .map(OffsetDateTime::parse)
                                            .orElse(OffsetDateTime.now());
                            var requestContext =
                                    new RequestContext().originatorId(UUID.randomUUID());
                            return new AuditEventRequest()
                                    .summary(d.get("summary"))
                                    .eventData(asEventData(d))
                                    .requestContext(requestContext)
                                    .timestamp(timestamp);
                        })
                .collect(Collectors.toList());
    }
}

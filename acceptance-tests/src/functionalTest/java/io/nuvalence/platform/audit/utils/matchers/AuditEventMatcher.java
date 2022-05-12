package io.nuvalence.platform.audit.utils.matchers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuvalence.platform.audit.client.generated.models.AuditEvent;
import io.nuvalence.platform.audit.client.generated.models.AuditEventLinks;
import io.nuvalence.platform.audit.client.generated.models.AuditEventRequest;
import io.nuvalence.platform.audit.client.generated.models.BusinessObjectMetadata;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collections;

/**
 * Matches an audit event to its request data.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification =
                "The expected metadata and request are technically mutable, "
                        + "meaning if the test modifies them after creating the matcher, but before "
                        + "evaluating the assertion, the updated values will be used. This is "
                        + "assumed to be okay for a test utility class. ")
public class AuditEventMatcher extends TypeSafeMatcher<AuditEvent> {
    private final BusinessObjectMetadata businessObjectMetadata;
    private final AuditEventRequest request;

    /**
     * Matches on business object metadata and request data.
     * @param businessObjectMetadata expected business object metadata
     * @param request request data expected in event
     */
    public AuditEventMatcher(
            BusinessObjectMetadata businessObjectMetadata, AuditEventRequest request) {
        this.businessObjectMetadata = businessObjectMetadata;
        this.request = request;
    }

    @Override
    protected boolean matchesSafely(AuditEvent item) {
        return Matchers.equalTo(request.getTimestamp().toInstant())
                        .matches(item.getTimestamp().toInstant())
                && Matchers.equalTo(businessObjectMetadata).matches(item.getBusinessObject())
                && Matchers.equalTo(request.getSummary()).matches(item.getSummary())
                && Matchers.equalTo(request.getEventData()).matches(item.getEventData())
                && areLinksEqual(item.getLinks())
                && Matchers.equalTo(request.getRequestContext()).matches(item.getRequestContext());
    }

    private boolean areLinksEqual(AuditEventLinks actual) {
        // todo: ideally the default value would be the same for the client and server
        var defaultLinks = new AuditEventLinks().relatedBusinessObjects(Collections.emptyList());
        var expected = request.getLinks() == null ? defaultLinks : request.getLinks();
        return Matchers.equalTo(expected).matches(actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an event matching the data described by the request: ");
        description.appendValue(request);
    }
}

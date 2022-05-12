package io.nuvalence.platform.audit.utils.matchers;

import io.nuvalence.platform.audit.client.generated.models.AuditEvent;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

/**
 * Matches when the list of events is ordered by timestamp.
 */
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class IsOrderedByTimestamp extends TypeSafeMatcher<List<AuditEvent>> {
    private final boolean descending;

    public IsOrderedByTimestamp() {
        this(false);
    }

    public IsOrderedByTimestamp(boolean descending) {
        this.descending = descending;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("should be ordered by timestamp");
        if (descending) {
            description.appendText("(descending)");
        }
    }

    @Override
    protected boolean matchesSafely(List<AuditEvent> item) {
        for (int i = 1; i < item.size(); i++) {
            var current = item.get(i).getTimestamp();
            var last = item.get(i - 1).getTimestamp();
            if ((descending && current.isAfter(last)) || (!descending && current.isBefore(last))) {
                return false;
            }
        }
        return true;
    }
}

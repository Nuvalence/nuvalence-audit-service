package io.nuvalence.platform.audit.utils.cucumber.transformers;

import io.nuvalence.platform.audit.client.generated.models.ActivityEventData;
import io.nuvalence.platform.audit.client.generated.models.AuditEventDataBase;

import java.util.Map;

/**
 * Transforms a data table row to an activity event request.
 */
public class ActivityEventTableTransformer
        extends AbstractAuditEventTableTransformer<ActivityEventData> {
    @Override
    public ActivityEventData asEventData(Map<String, String> item) {
        var eventData =
                new ActivityEventData().activityType(item.get("type")).data(item.get("data"));
        eventData.setType(AuditEventDataBase.TypeEnum.ACTIVITYEVENTDATA);
        return eventData;
    }
}

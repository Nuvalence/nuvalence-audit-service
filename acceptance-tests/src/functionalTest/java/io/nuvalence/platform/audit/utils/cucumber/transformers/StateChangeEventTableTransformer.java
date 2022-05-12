package io.nuvalence.platform.audit.utils.cucumber.transformers;

import io.nuvalence.platform.audit.client.generated.models.AuditEventDataBase;
import io.nuvalence.platform.audit.client.generated.models.StateChangeEventData;

import java.util.Map;

/**
 * Transforms a data table row to a state change event request.
 */
public class StateChangeEventTableTransformer
        extends AbstractAuditEventTableTransformer<StateChangeEventData> {
    @Override
    public StateChangeEventData asEventData(Map<String, String> item) {
        var eventData =
                new StateChangeEventData()
                        .newState(item.get("new_state"))
                        .newState(item.get("old_state"));
        eventData.setType(AuditEventDataBase.TypeEnum.STATECHANGEEVENTDATA);
        return eventData;
    }
}

package io.nuvalence.platform.audit.service.utils;

import io.nuvalence.platform.audit.service.domain.ActivityEventEntity;
import io.nuvalence.platform.audit.service.domain.StateChangeEventEntity;
import io.nuvalence.platform.audit.service.generated.models.AuditEvent;
import io.nuvalence.platform.audit.service.generated.models.AuditEventRequest;

import java.io.IOException;

/**
 * Static test data & configuration.
 */
public interface TestUtil {
    String ACTIVITY_REQUEST_JSON = "/samples/activity_audit_event_request.json";
    String STATE_CHANGE_ENTITY_JSON = "/samples/state_change_audit_event_entity.json";
    String ACTIVITY_ENTITY_JSON = "/samples/activity_audit_event_entity.json";
    String STATE_CHANGE_REQUEST_JSON = "/samples/state_change_audit_event_request.json";

    /**
     * Test resources.
     */
    enum Data {
        ACTIVITY_REQUEST(ACTIVITY_REQUEST_JSON, AuditEventRequest.class),
        ACTIVITY_MODEL(ACTIVITY_REQUEST_JSON, AuditEvent.class),
        ACTIVITY_ENTITY(ACTIVITY_ENTITY_JSON, ActivityEventEntity.class),
        STATE_CHANGE_REQUEST(STATE_CHANGE_REQUEST_JSON, AuditEventRequest.class),
        STATE_CHANGE_MODEL(STATE_CHANGE_REQUEST_JSON, AuditEvent.class),
        STATE_CHANGE_ENTITY(STATE_CHANGE_ENTITY_JSON, StateChangeEventEntity.class);

        private final String path;
        private final Class<?> clazz;

        Data(String path, Class<?> clazz) {
            this.path = path;
            this.clazz = clazz;
        }

        public <T> T readJson() throws IOException {
            return SamplesUtil.readJsonFile(path, (Class<T>) clazz);
        }

        public String readJsonString() throws IOException {
            return SamplesUtil.readJsonFileAsString(path);
        }
    }
}

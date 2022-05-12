package io.nuvalence.platform.audit.utils.cucumber.contexts;

import io.nuvalence.platform.audit.client.generated.models.BusinessObjectMetadata;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.util.Optional;

/**
 * Shared context/data to be retained throughout a test scenario.
 * This is injected by cucumber-picocontainer in any step definitions
 * class which takes this as a constructor argument.
 */
@Getter
@Setter
public class ScenarioContext {
    private static final String baseUri =
            Optional.ofNullable(System.getenv("SERVICE_URI")).orElse("http://localhost:8080");

    private static final String authorization = System.getenv("AUTHORIZATION");

    private BusinessObjectMetadata businessObjectMetadata;

    private InputStream loadedResource;

    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Adds authorization header to request.
     * @param request http request
     */
    public void applyAuthorization(HttpRequest.Builder request) {
        if (authorization != null) {
            request.header("authorization", "Bearer " + authorization);
        }
    }
}

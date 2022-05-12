package io.nuvalence.platform.audit;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.audit.utils.cucumber.contexts.ScenarioContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class RawHttpClientStepDefinitions {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScenarioContext scenarioContext;
    private HttpResponse<?> lastApiResponse;

    @When("a request is made to create the event")
    public void requestIsMadeToCreateTheEvent() throws IOException, InterruptedException {
        var requestBuilder =
                HttpRequest.newBuilder()
                        .uri(getBusinessObjectAuditEventsUri())
                        .header("content-type", "application/json")
                        .POST(
                                HttpRequest.BodyPublishers.ofInputStream(
                                        scenarioContext::getLoadedResource));

        scenarioContext.applyAuthorization(requestBuilder);

        var request = requestBuilder.build();

        lastApiResponse = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    @Then("the resulting status code should be {int}")
    public void theResultingStatusCodeShouldBe(int expectedStatusCode) {
        Assertions.assertEquals(expectedStatusCode, lastApiResponse.statusCode());
    }

    @SneakyThrows
    private URI getBusinessObjectAuditEventsUri() {
        return new URI(
                String.format(
                        "%s/v1/audit/business-objects/%s/%s/events",
                        scenarioContext.getBaseUri(),
                        scenarioContext.getBusinessObjectMetadata().getType(),
                        scenarioContext.getBusinessObjectMetadata().getId()));
    }
}

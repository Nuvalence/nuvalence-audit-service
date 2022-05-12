package io.nuvalence.platform.audit;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.audit.utils.cucumber.contexts.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class DeveloperExperienceStepDefinitions {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final ScenarioContext scenarioContext;
    private URI endpoint;
    private HttpResponse<String> lastApiResponse;

    @Given("^the default .* endpoint (.+)$")
    public void theEndpoint(String path) throws URISyntaxException {
        this.endpoint = new URI(scenarioContext.getBaseUri() + path);
    }

    @When("a GET request is made")
    public void getRequestIsMade() throws IOException, InterruptedException {
        var requestBuilder = HttpRequest.newBuilder().uri(endpoint).GET();
        scenarioContext.applyAuthorization(requestBuilder);
        var request = requestBuilder.build();
        lastApiResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("^it should return (\\d+) and contain the text (.+)$")
    public void itShouldReturn(int statusCode, String expectedContent) {
        Assertions.assertEquals(statusCode, lastApiResponse.statusCode());
        MatcherAssert.assertThat(lastApiResponse.body(), Matchers.containsString(expectedContent));
    }
}

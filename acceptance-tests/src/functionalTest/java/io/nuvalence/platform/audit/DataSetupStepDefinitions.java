package io.nuvalence.platform.audit;

import io.cucumber.java.en.Given;
import io.nuvalence.platform.audit.client.generated.models.BusinessObjectMetadata;
import io.nuvalence.platform.audit.utils.cucumber.contexts.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
public class DataSetupStepDefinitions {
    private final ScenarioContext scenarioContext;

    @Given("^a business object with type (.+) and a random id$")
    public void businessObjectWithTypeSampleAndARandomId(String businessObjectType) {
        var metadata = new BusinessObjectMetadata().type(businessObjectType).id(UUID.randomUUID());
        scenarioContext.setBusinessObjectMetadata(metadata);
    }

    @Given("^the invalid request body defined at (.+)$")
    public void givenResource(String name) {
        try (InputStream res = this.getClass().getResourceAsStream(name)) {
            scenarioContext.setLoadedResource(res);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }
}

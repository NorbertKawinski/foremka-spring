package com.codechievement.foremka.v1;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.config.TestConfig;
import com.codechievement.foremka.v1.fixture.ProductInput;
import com.codechievement.foremka.v1.fixture.ProductScenario;
import com.codechievement.foremka.v1.fixture.ProductScenarioProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfig.class)
class TestScenariosWithComplexInputIntegrationTest {

    @Autowired
    private ProductScenarioProvider productScenarioProvider;

    @Test
    void contextLoads() {
        assertThat(productScenarioProvider, is(notNullValue()));
    }

    @Test
    void testScenarios_acceptsComplexStructuresAsInput() {
        ProductScenario scenario = productScenarioProvider.get(new ProductInput("Winter Jacket", "SEASONAL", 5000));

        assertThat(scenario.id(), is(PRODUCT_SCENARIO.id()));
        assertThat(scenario.name(), is("Winter Jacket"));
        assertThat(scenario.category(), is("SEASONAL"));
        assertThat(scenario.basePrice(), is(5000));
        assertThat(scenario.discountedPrice(), is(3500));
    }

    @Test
    void testScenarios_cachesComplexStructures() {
        ProductInput input = new ProductInput("Laptop", "ELECTRONICS", 120000);
        ProductScenario first = productScenarioProvider.get(input);
        ProductScenario second = productScenarioProvider.get(input);

        assertThat(first, is(sameInstance(second)));
    }

    @Test
    void testScenarios_differentInputsProduceDifferentScenarios() {
        ProductScenario jacket = productScenarioProvider.get(new ProductInput("Winter Jacket", "SEASONAL", 5000));
        ProductScenario laptop = productScenarioProvider.get(new ProductInput("Laptop", "ELECTRONICS", 120000));

        assertThat(jacket, is(not(sameInstance(laptop))));
        assertThat(jacket.discountedPrice(), is(3500));
        assertThat(laptop.discountedPrice(), is(108000));
    }
}

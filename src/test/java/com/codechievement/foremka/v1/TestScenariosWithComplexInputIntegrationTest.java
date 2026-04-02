package com.codechievement.foremka.v1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.config.TestConfig;
import com.codechievement.foremka.v1.fixture.RectangleInput;
import com.codechievement.foremka.v1.fixture.RectangleScenario;
import com.codechievement.foremka.v1.fixture.RectangleScenarioProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfig.class)
class TestScenariosWithComplexInputIntegrationTest {

    @Autowired
    private RectangleScenarioProvider rectangleScenarioProvider;

    @Test
    void contextLoads() {
        assertThat(rectangleScenarioProvider, is(notNullValue()));
    }

    @Test
    void testScenarios_acceptsComplexStructuresAsInput() {
        RectangleScenario scenario = rectangleScenarioProvider.get(new RectangleInput(3, 4));

        assertThat(scenario.width(), is(3));
        assertThat(scenario.height(), is(4));
        assertThat(scenario.area(), is(12));
        assertThat(scenario.perimeter(), is(14));
    }

    @Test
    void testScenarios_cachesComplexStructures() {
        RectangleInput input = new RectangleInput(5, 6);
        RectangleScenario first = rectangleScenarioProvider.get(input);
        RectangleScenario second = rectangleScenarioProvider.get(input);

        assertThat(first, is(sameInstance(second)));
    }

    @Test
    void testScenarios_differentInputsProduceDifferentScenarios() {
        RectangleScenario small = rectangleScenarioProvider.get(new RectangleInput(2, 3));
        RectangleScenario large = rectangleScenarioProvider.get(new RectangleInput(10, 20));

        assertThat(small, is(not(sameInstance(large))));
        assertThat(small.area(), is(6));
        assertThat(small.perimeter(), is(10));
        assertThat(large.area(), is(200));
        assertThat(large.perimeter(), is(60));
    }
}

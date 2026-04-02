package com.codechievement.foremka.v1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.config.TestConfig;
import com.codechievement.foremka.v1.fixture.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestConfig.class)
class TestScenariosBasicIntegrationTest {

    @Autowired
    private UserScenarioProvider userScenarioProvider;

    @Test
    void contextLoads() {
        assertThat(userScenarioProvider, is(notNullValue()));
    }

    @Test
    void scenarioFactory_createsScenario() {
        UserScenario scenario = userScenarioProvider.get("alice");

        assertThat(scenario.username(), is("alice"));
        assertThat(scenario.email(), is("alice@test.com"));
    }

    @Test
    void testScenarios_cachesScenarioByInput() {
        UserScenario first = userScenarioProvider.get("bob");
        UserScenario second = userScenarioProvider.get("bob");

        assertThat(first, is(sameInstance(second)));
        assertThat(first.username(), is("bob"));
    }

    @Test
    void testScenarios_differentInputsProduceDifferentScenarios() {
        UserScenario alice = userScenarioProvider.get("alice");
        UserScenario bob = userScenarioProvider.get("bob");

        assertThat(alice, is(not(sameInstance(bob))));
        assertThat(alice.username(), is("alice"));
        assertThat(bob.username(), is("bob"));
    }
}

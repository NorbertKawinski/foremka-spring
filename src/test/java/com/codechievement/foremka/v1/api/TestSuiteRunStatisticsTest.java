package com.codechievement.foremka.v1.api;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.v1.components.InMemoryScenarioRepository;
import org.junit.jupiter.api.Test;

class TestSuiteRunStatisticsTest {

    @Test
    void getSummaryStatistics_tracksCacheHitMissSavedTimeAndScenarioUsageOrder() {
        var repository = new InMemoryScenarioRepository();
        var testScenarios = new TestScenarios(repository, SCENARIO_SERIALIZER);
        testScenarios.get(USER_FACTORY, ALICE_INPUT);
        testScenarios.get(USER_FACTORY, ALICE_INPUT);
        testScenarios.get(USER_FACTORY, ALICE_INPUT);
        testScenarios.get(USER_FACTORY, ALICE_INPUT);
        testScenarios.get(USER_FACTORY, BOB_INPUT);
        testScenarios.get(PRODUCT_FACTORY, PRODUCT_INPUT);
        testScenarios.get(PRODUCT_FACTORY, PRODUCT_INPUT);

        TestSuiteRunStatistics stats = testScenarios.getSummaryStatistics();

        assertThat(stats.cacheHits(), is(4L));
        assertThat(stats.cacheMisses(), is(3L));
        assertThat(stats.savedTime(), is(notNullValue()));
        assertThat(stats.numUnusedScenarios(), is(0L));

        testScenarios.destroy();

        testScenarios = new TestScenarios(repository, SCENARIO_SERIALIZER);
        testScenarios.get(USER_FACTORY, ALICE_INPUT);

        stats = testScenarios.getSummaryStatistics();

        assertThat(stats.numUnusedScenarios(), is(2L));
    }
}

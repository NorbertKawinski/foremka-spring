package com.codechievement.foremka.v1.api;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.v1.components.InMemoryScenarioRepository;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the scenario statistics feature.
 *
 * <p>These tests verify that {@link TestScenarioMeta} is correctly populated when scenarios are first
 * created and when they are subsequently retrieved from the cache.
 */
class TestScenariosMetaTest {
    private TestScenarios testScenarios;
    private Instant instantBeforeFactory;
    private TestScenarioMeta meta;
    private Instant instantAfterFactory;

    @BeforeEach
    void setup() {
        testScenarios = new TestScenarios(new InMemoryScenarioRepository(), SCENARIO_SERIALIZER);

        instantBeforeFactory = Instant.now();
        meta = testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT).meta();
        instantAfterFactory = Instant.now();
    }

    @Test
    void meta_createdAt_isSetProperly() {
        assertThat(meta.getCreatedAt().isBefore(instantBeforeFactory), is(false));
        assertThat(meta.getCreatedAt().isAfter(instantAfterFactory), is(false));
    }

    @Test
    void meta_createdByTest_isSetProperly() {
        assertThat(meta.getCreatedByTest(), equalTo("TestScenariosMetaTest.setup"));

        meta = testScenarios.getWithExtra(USER_FACTORY, BOB_INPUT).meta();
        assertThat(meta.getCreatedByTest(), equalTo("TestScenariosMetaTest.meta_createdByTest_isSetProperly"));
    }

    @Test
    void meta_creationDuration_isWithinExpectedRange() {
        Duration maxExpectedDuration = Duration.between(instantBeforeFactory, instantAfterFactory);
        Duration minExpectedDuration = maxExpectedDuration.minusMillis(50);
        assertThat(meta.getCreationDuration(), is(greaterThanOrEqualTo(minExpectedDuration)));
        assertThat(meta.getCreationDuration(), is(lessThanOrEqualTo(maxExpectedDuration)));
    }

    @Test
    void meta_lastUsedAt_isSetProperly() {
        assertThat(meta.getLastUsedAt().isBefore(instantBeforeFactory), is(false));
        assertThat(meta.getLastUsedAt().isAfter(instantAfterFactory), is(false));
    }

    @Test
    void meta_lastUsedAt_isUpdatedOnEachAccess() throws InterruptedException {
        Instant beforeNextAccess = Instant.now();
        meta = testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT).meta();
        Instant afterNextAccess = Instant.now();

        assertThat(meta.getLastUsedAt().isBefore(beforeNextAccess), is(false));
        assertThat(meta.getLastUsedAt().isAfter(afterNextAccess), is(false));
    }

    @Test
    void meta_totalUsageCount_isOneOnCreation() {
        assertThat(meta.getTotalUsageCount(), is(1L));
    }

    @Test
    void meta_totalUsageCount_incrementsOnCacheHit() {
        testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT);
        meta = testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT).meta();

        assertThat(meta.getTotalUsageCount(), is(3L));
    }

    @Test
    void meta_usedByTests_containsCurrentTestOnCreation() {
        assertThat(meta.getUsedByTests(), contains("TestScenariosMetaTest.setup"));
        assertThat(
                meta.getUsedByTests(),
                not(contains("TestScenariosMetaTest.meta_usedByTests_containsCurrentTestOnCreation")));

        meta = testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT).meta();
        assertThat(
                meta.getUsedByTests(),
                containsInAnyOrder(
                        "TestScenariosMetaTest.setup",
                        "TestScenariosMetaTest.meta_usedByTests_containsCurrentTestOnCreation"));
    }

    @Test
    void meta_usedByTests_doesNotDuplicateSameCaller() {
        testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT);
        testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT);
        meta = testScenarios.getWithExtra(USER_FACTORY, ALICE_INPUT).meta();

        long count = meta.getUsedByTests().stream()
                .filter(testName ->
                        testName.equals("TestScenariosMetaTest.meta_usedByTests_doesNotDuplicateSameCaller"))
                .count();
        assertThat(count, is(1L));
    }
}

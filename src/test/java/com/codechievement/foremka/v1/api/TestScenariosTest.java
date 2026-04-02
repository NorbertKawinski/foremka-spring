package com.codechievement.foremka.v1.api;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.codechievement.foremka.v1.components.InMemoryScenarioRepository;
import com.codechievement.foremka.v1.fixture.RectangleScenario;
import com.codechievement.foremka.v1.fixture.UserScenario;
import com.codechievement.foremka.v1.internal.ScenarioSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestScenariosTest {
    private static final ScenarioSerializer serializer = new ScenarioSerializer(new ObjectMapper());

    private InMemoryScenarioRepository repository;
    private TestScenarios testScenarios;

    @BeforeEach
    void setUp() {
        repository = new InMemoryScenarioRepository();

        testScenarios = new TestScenarios(repository, serializer);
        testScenarios.afterPropertiesSet();
    }

    @Test
    void computeIfAbsent_withSupplier_createsScenario() {
        UserScenario scenario = testScenarios.computeIfAbsent(UserScenario.class, () -> ALICE_SCENARIO);
        assertThat(scenario, is(ALICE_SCENARIO));
    }

    @Test
    void computeIfAbsent_withSupplier_cachesResult() {
        assertDoesNotThrow(() -> {
            var alice1 = testScenarios.computeIfAbsent(UserScenario.class, () -> ALICE_SCENARIO);
            var alice2 = testScenarios.computeIfAbsent(UserScenario.class, () -> {
                throw new IllegalStateException("Supplier should not be called for cached scenario");
            });
            assertThat(alice1, is(sameInstance(alice2)));
        });
    }

    @Test
    void computeIfAbsent_withFunction_createsScenarioFromInput() {
        UserScenario scenario = testScenarios.computeIfAbsent(UserScenario.class, "alice", input -> ALICE_SCENARIO);
        assertThat(scenario, is(ALICE_SCENARIO));
    }

    @Test
    void computeIfAbsent_withFunction_cachesPerInput() {
        assertDoesNotThrow(() -> {
            var alice1 = testScenarios.computeIfAbsent(UserScenario.class, "alice", input -> ALICE_SCENARIO);
            var alice2 = testScenarios.computeIfAbsent(UserScenario.class, "alice", input -> {
                throw new IllegalStateException("Factory should not be called for cached input");
            });
            assertThat(alice1, is(sameInstance(alice2)));
        });
    }

    @Test
    void computeIfAbsent_differentInputs_produceDifferentScenarios() {
        UserScenario alice = testScenarios.computeIfAbsent(UserScenario.class, "alice", input -> ALICE_SCENARIO);
        UserScenario bob = testScenarios.computeIfAbsent(UserScenario.class, "bob", input -> BOB_SCENARIO);

        assertThat(alice, is(not(sameInstance(bob))));
        assertThat(alice, is(ALICE_SCENARIO));
        assertThat(bob, is(BOB_SCENARIO));
    }

    @Test
    void get_withFactory_delegatesCorrectly() {
        UserScenario scenario = testScenarios.get(USER_FACTORY, ALICE_INPUT);
        assertThat(scenario, is(ALICE_SCENARIO));
    }

    @Test
    void get_withFactory_cachesResult() {
        UserScenario first = testScenarios.get(USER_FACTORY, ALICE_INPUT);
        UserScenario second = testScenarios.get(USER_FACTORY, ALICE_INPUT);
        assertThat(first, is(sameInstance(second)));
    }

    @Test
    void get_withComplexInputFactory() {
        RectangleScenario scenario = testScenarios.get(RECTANGLE_FACTORY, RECTANGLE_INPUT);
        assertThat(scenario, is(RECTANGLE_SCENARIO));
    }

    @Test
    void destroy_persistsDataToRepository() {
        testScenarios.get(USER_FACTORY, ALICE_INPUT);
        assertThat(repository.findAll().isPresent(), is(false));

        testScenarios.destroy();

        assertThat(repository.findAll().isPresent(), is(true));
        assertThat(repository.findAll().get(), containsString("alice"));
    }

    @Test
    void constructor_restoresCachedScenarios() {
        var alice1 = testScenarios.get(USER_FACTORY, ALICE_INPUT);

        testScenarios.destroy();
        TestScenarios newTestScenarios = new TestScenarios(repository, serializer);
        newTestScenarios.afterPropertiesSet();

        var alice2 = newTestScenarios.computeIfAbsent(UserScenario.class, "alice", input -> {
            throw new IllegalStateException(
                    "Scenario should be restored from repository, factory should not be called");
        });

        assertThat(alice1, is(not(sameInstance(alice2))));
        assertThat(alice2, is(ALICE_SCENARIO));
    }
}

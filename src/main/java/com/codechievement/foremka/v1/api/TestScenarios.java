package com.codechievement.foremka.v1.api;

import com.codechievement.foremka.v1.internal.*;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

@Component
public class TestScenarios implements DisposableBean {
    private final ScenarioRepository repository;
    private final ScenarioSerializer serializer;
    private final TestScenariosMap scenarios;
    private final Instant currentRunStartedAt;
    private final boolean cleanupEnabled;

    public TestScenarios(ScenarioRepository repository, ScenarioSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
        this.scenarios = repository.findAll().map(serializer::deserialize).orElseGet(TestScenariosMap::new);
        this.currentRunStartedAt = Instant.now();
        this.cleanupEnabled = parseBoolean(getProperty("CLEANUP_TEST_SCENARIOS"));
    }

    /**
     * Saves all in-memory scenarios to the repository when the Spring context is closed.
     * This is triggered automatically when JUnit finishes all tests and the context shuts down.
     */
    @Override
    public void destroy() {
        if (cleanupEnabled) {
            scenarios.entrySet().removeIf(entry -> {
                Instant lastUsedAt = entry.getValue().meta().getLastUsedAt();
                return lastUsedAt.isBefore(currentRunStartedAt);
            });
        }
        repository.saveAll(serializer.serialize(scenarios));
    }

    public <T extends TestScenario> T computeIfAbsent(Class<T> clazz, Supplier<T> supplier) {
        return computeIfAbsent(clazz, "default", k -> supplier.get());
    }

    public <IN, T extends TestScenario> T computeIfAbsent(Class<T> clazz, IN input, Function<IN, T> supplier) {
        return computeIfAbsentWithMeta(clazz, input, supplier).scenario();
    }

    @SuppressWarnings("unchecked")
    public <IN, T extends TestScenario> TestScenarioWithMeta<T> computeIfAbsentWithMeta(
            Class<T> clazz, IN input, Function<IN, T> supplier) {
        String testName = TestNameDetector.detectCurrentTestName();
        var key = new ScenarioInputWithMeta(clazz, input);

        TestScenarioWithMeta<T> result = scenarios.computeIfAbsent(key, k -> {
            Instant start = Instant.now();
            T scenario = supplier.apply(input);
            Duration duration = Duration.between(start, Instant.now());

            TestScenarioMeta meta = new TestScenarioMeta(start, testName, duration);
            return new TestScenarioWithMeta<>(scenario, meta);
        });

        result.meta().recordUsage(testName);

        return result;
    }

    public <IN, OUT extends TestScenario> OUT get(ScenarioFactory<IN, OUT> factory, IN input) {
        return getWithMeta(factory, input).scenario();
    }

    public <IN, OUT extends TestScenario> TestScenarioWithMeta<OUT> getWithMeta(
            ScenarioFactory<IN, OUT> factory, IN input) {
        return computeIfAbsentWithMeta(factory.getScenarioClass(), input, factory::create);
    }
}

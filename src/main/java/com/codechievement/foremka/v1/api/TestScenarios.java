package com.codechievement.foremka.v1.api;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

import com.codechievement.foremka.v1.internal.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestScenarios implements DisposableBean {
    public static boolean CLEANUP_TEST_SCENARIOS = parseBoolean(getProperty("CLEANUP_TEST_SCENARIOS"));

    private final ScenarioRepository repository;
    private final ScenarioSerializer serializer;
    private final TestScenariosMap scenarios;
    private final Instant currentRunStartedAt = Instant.now();
    private final AtomicLong cacheHitsInCurrentRun = new AtomicLong();
    private final AtomicLong cacheMissesInCurrentRun = new AtomicLong();
    private final AtomicLong savedTimeNanosInCurrentRun = new AtomicLong();

    public TestScenarios(ScenarioRepository repository, ScenarioSerializer serializer) {
        this.repository = repository;
        this.serializer = serializer;
        this.scenarios = loadScenarios();
    }

    private TestScenariosMap loadScenarios() {
        try {
            return repository.findAll().map(serializer::deserialize).orElseGet(TestScenariosMap::new);
        } catch (Exception e) {
            log.warn("Failed to load test scenarios. Using empty scenario map", e);
            return new TestScenariosMap();
        }
    }

    /**
     * Saves all in-memory scenarios to the repository when the Spring context is closed.
     * This is triggered automatically when JUnit finishes all tests and the context shuts down.
     */
    @Override
    public void destroy() {
        log.info("Test suite run statistics: {}", getSummaryStatistics());

        if (CLEANUP_TEST_SCENARIOS) {
            scenarios.removeIf(s -> s.meta().getLastUsedAt().isBefore(currentRunStartedAt));
        }
        repository.saveAll(serializer.serialize(scenarios));
    }

    public <T extends TestScenario> T computeIfAbsent(Class<T> clazz, Supplier<T> supplier) {
        return computeIfAbsent(clazz, "default", k -> supplier.get());
    }

    public <IN, T extends TestScenario> T computeIfAbsent(Class<T> clazz, IN input, Function<IN, T> supplier) {
        return computeIfAbsentWithExtra(clazz, input, supplier).scenario();
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> computeIfAbsentWithExtra(
            Class<T> clazz, IN input, Function<IN, T> supplier) {
        String testName = TestNameDetector.detectCurrentTestName();

        TestScenarioWithExtra<IN, T> result =
                scenarios.computeIfAbsent(clazz, input, () -> compute(input, supplier, testName));
        result.meta().recordUsage(testName);

        if (result.meta().getTotalUsageCount() == 1) {
            cacheMissesInCurrentRun.incrementAndGet();
        } else {
            cacheHitsInCurrentRun.incrementAndGet();
            savedTimeNanosInCurrentRun.addAndGet(
                    result.meta().getCreationDuration().toNanos());
        }

        return result;
    }

    private <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> compute(
            IN input, Function<IN, T> supplier, String testName) {
        Instant start = Instant.now();
        T scenario = supplier.apply(input);
        Duration duration = Duration.between(start, Instant.now());

        TestScenarioMeta meta = new TestScenarioMeta(start, testName, duration);
        return new TestScenarioWithExtra<>(input, scenario, meta);
    }

    public TestSuiteRunStatistics getSummaryStatistics() {
        return TestSuiteRunStatistics.extract(
                currentRunStartedAt,
                cacheHitsInCurrentRun.get(),
                cacheMissesInCurrentRun.get(),
                Duration.ofNanos(savedTimeNanosInCurrentRun.get()),
                scenarios);
    }

    public <IN, T extends TestScenario> T get(ScenarioFactory<IN, T> factory, IN input) {
        return getWithExtra(factory, input).scenario();
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> getWithExtra(
            ScenarioFactory<IN, T> factory, IN input) {
        return computeIfAbsentWithExtra(factory.getScenarioClass(), input, factory::create);
    }
}

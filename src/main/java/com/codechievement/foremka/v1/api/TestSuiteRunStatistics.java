package com.codechievement.foremka.v1.api;

import com.codechievement.foremka.v1.internal.TestScenariosMap;
import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.NonNull;

public record TestSuiteRunStatistics(
        Instant currentRunStartedAt, long cacheHits, long cacheMisses, Duration savedTime, long numUnusedScenarios) {
    public static TestSuiteRunStatistics extract(
            Instant currentRunStartedAt,
            long cacheHits,
            long cacheMisses,
            Duration savedTime,
            TestScenariosMap scenarios) {

        long numUnusedScenarios = scenarios.values().stream()
                .filter(entry -> entry.meta().getLastUsedAt().isBefore(currentRunStartedAt))
                .count();

        return new TestSuiteRunStatistics(currentRunStartedAt, cacheHits, cacheMisses, savedTime, numUnusedScenarios);
    }

    @Override
    public @NonNull String toString() {
        return "cacheHits=" + cacheHits
                + ", cacheMisses=" + cacheMisses
                + ", savedTime=" + savedTime
                + ", numUnusedScenarios=" + numUnusedScenarios;
    }
}

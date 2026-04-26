package com.codechievement.foremka.v1.api;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Jacksonized
public class TestScenarioMeta {

    @EqualsAndHashCode.Include
    private Instant createdAt;

    @EqualsAndHashCode.Include
    private String createdByTest;

    @EqualsAndHashCode.Include
    private Duration creationDuration;

    private Instant lastUsedAt;

    private long totalUsageCount;

    private Set<String> usedByTests;

    public TestScenarioMeta(Instant createdAt, String createdByTest, Duration creationDuration) {
        this.createdAt = createdAt;
        this.createdByTest = createdByTest;
        this.creationDuration = creationDuration;
        this.usedByTests = new HashSet<>();
    }

    /**
     * Records a subsequent access (cache-hit). Should be called every time the scenario is
     * retrieved from the in-memory cache (i.e. factory was NOT invoked).
     */
    public synchronized void recordUsage(String testName) {
        this.lastUsedAt = Instant.now();
        this.totalUsageCount++;
        this.usedByTests.add(testName);
    }
}

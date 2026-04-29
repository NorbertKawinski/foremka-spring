package com.codechievement.foremka.v1.api;

import com.codechievement.foremka.v1.internal.EpochMillisInstantDeserializer;
import com.codechievement.foremka.v1.internal.EpochMillisInstantSerializer;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestScenarioMeta {

    @EqualsAndHashCode.Include
    @JsonProperty("ca")
    @JsonAlias("createdAt")
    @JsonSerialize(using = EpochMillisInstantSerializer.class)
    @JsonDeserialize(using = EpochMillisInstantDeserializer.class)
    private Instant createdAt;

    @EqualsAndHashCode.Include
    @JsonProperty("cb")
    @JsonAlias("createdByTest")
    private String createdByTest;

    @EqualsAndHashCode.Include
    @JsonProperty("cd")
    @JsonAlias("creationDuration")
    @Builder.Default
    private Duration creationDuration;

    @JsonProperty("lu")
    @JsonAlias("lastUsedAt")
    @JsonSerialize(using = EpochMillisInstantSerializer.class)
    @JsonDeserialize(using = EpochMillisInstantDeserializer.class)
    private Instant lastUsedAt;

    @JsonProperty("tu")
    @JsonAlias("totalUsageCount")
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OneTotalUsageCountFilter.class)
    private long totalUsageCount;

    @JsonProperty("ub")
    @JsonAlias("usedByTests")
    private Set<String> usedByTests;

    public TestScenarioMeta(Instant createdAt, String createdByTest, Duration creationDuration) {
        this.createdAt = createdAt;
        this.createdByTest = createdByTest;
        this.creationDuration = creationDuration;
        this.lastUsedAt = createdAt;
        this.usedByTests = new HashSet<>();
        this.usedByTests.add(createdByTest);
    }

    /**
     * Records an access to this scenario. Called on every retrieval — both on initial creation
     * (cache miss) and on subsequent cache hits.
     */
    public synchronized void recordUsage(String testName) {
        this.lastUsedAt = Instant.now();
        this.totalUsageCount++;
        this.usedByTests.add(testName);
    }

    public void compress() {
        if (creationDuration.isZero()) {
            creationDuration = null;
        }
        if (lastUsedAt.equals(createdAt)) {
            lastUsedAt = null;
        }
        usedByTests.remove(createdByTest);
        if (usedByTests.isEmpty()) {
            usedByTests = null;
        }
    }

    public void decompress() {
        if (creationDuration == null) {
            creationDuration = Duration.ZERO;
        }
        if (lastUsedAt == null) {
            lastUsedAt = createdAt;
        }
        if (totalUsageCount == 0) {
            totalUsageCount = 1;
        }
        if (usedByTests == null) {
            usedByTests = new HashSet<>();
        }
        usedByTests.add(createdByTest);
    }

    /**
     * Jackson omits the value when {@code valueFilter.equals(fieldValue)} returns true.
     * This keeps JSON compact while preserving all other values.
     */
    private static final class OneTotalUsageCountFilter {
        @Override
        public boolean equals(Object other) {
            return other instanceof Number number && number.longValue() == 1L;
        }
    }
}

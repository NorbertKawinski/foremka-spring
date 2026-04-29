package com.codechievement.foremka.v1.api;

import static java.time.Duration.ZERO;
import static java.time.Duration.ofSeconds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestScenarioMetaCompressTest {

    private static final Instant INSTANT_1 = Instant.ofEpochSecond(1000);
    private static final Instant INSTANT_2 = Instant.ofEpochSecond(2000);
    private static final Duration DURATION_1 = ofSeconds(1);
    private static final String CREATED_BY_1 = "myTest_1";
    private static final String CREATED_BY_2 = "myTest_2";

    @Test
    void compress_zeroDuration_setsCreationDurationToNull() {
        var meta1 = metaBuilder().creationDuration(ZERO).build();
        var meta2 = metaBuilder().creationDuration(DURATION_1).build();

        meta1.compress();
        meta2.compress();

        assertThat(meta1.getCreationDuration(), is(nullValue()));
        assertThat(meta2.getCreationDuration(), is(DURATION_1));
    }

    @Test
    void compress_lastUsedAtEqualsCreatedAt_setsLastUsedAtToNull() {
        TestScenarioMeta meta1 =
                metaBuilder().createdAt(INSTANT_1).lastUsedAt(INSTANT_1).build();
        TestScenarioMeta meta2 =
                metaBuilder().createdAt(INSTANT_1).lastUsedAt(INSTANT_2).build();

        meta1.compress();
        meta2.compress();

        assertThat(meta1.getLastUsedAt(), is(nullValue()));
        assertThat(meta2.getLastUsedAt(), is(INSTANT_2));
    }

    @Test
    void compress_usedByTestsContainsOnlyCreatedByTest_setsUsedByTestsToNull() {
        TestScenarioMeta meta1 = metaBuilder()
                .createdByTest(CREATED_BY_1)
                .usedByTests(setOf(CREATED_BY_1))
                .build();
        TestScenarioMeta meta2 = metaBuilder()
                .createdByTest(CREATED_BY_1)
                .usedByTests(setOf(CREATED_BY_1, CREATED_BY_2))
                .build();

        meta1.compress();
        meta2.compress();

        assertThat(meta1.getUsedByTests(), is(nullValue()));
        assertThat(meta2.getUsedByTests(), not(contains(CREATED_BY_1)));
        assertThat(meta2.getUsedByTests(), contains(CREATED_BY_2));
    }

    @Test
    void decompress_nullCreationDuration_setsToZero() {
        TestScenarioMeta meta1 = metaBuilder().creationDuration(null).build();
        TestScenarioMeta meta2 = metaBuilder().creationDuration(DURATION_1).build();

        meta1.decompress();
        meta2.decompress();

        assertThat(meta1.getCreationDuration(), is(ZERO));
        assertThat(meta2.getCreationDuration(), is(DURATION_1));
    }

    @Test
    void decompress_nullLastUsedAt_setsToCreatedAt() {
        TestScenarioMeta meta1 = metaBuilder().lastUsedAt(null).build();
        TestScenarioMeta meta2 = metaBuilder().lastUsedAt(INSTANT_2).build();

        meta1.decompress();
        meta2.decompress();

        assertThat(meta1.getLastUsedAt(), is(INSTANT_1));
        assertThat(meta2.getLastUsedAt(), is(INSTANT_2));
    }

    @Test
    void decompress_nullUsedByTests_setsToSetContainingCreatedByTest() {
        TestScenarioMeta meta1 = metaBuilder().usedByTests(null).build();
        TestScenarioMeta meta2 = metaBuilder().usedByTests(setOf(CREATED_BY_2)).build();

        meta1.decompress();
        meta2.decompress();

        assertThat(meta1.getUsedByTests(), contains(CREATED_BY_1));
        assertThat(meta2.getUsedByTests(), containsInAnyOrder(CREATED_BY_1, CREATED_BY_2));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /** Returns a builder pre-populated with safe default values so each test only overrides what it needs. */
    private TestScenarioMeta.TestScenarioMetaBuilder metaBuilder() {
        return TestScenarioMeta.builder()
                .createdAt(INSTANT_1)
                .createdByTest(CREATED_BY_1)
                .creationDuration(DURATION_1)
                .lastUsedAt(INSTANT_2)
                .usedByTests(setOf(CREATED_BY_1));
    }

    private Set<String> setOf(String... strings) {
        return new HashSet<>(Set.of(strings));
    }
}

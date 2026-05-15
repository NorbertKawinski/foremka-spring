package com.codechievement.foremka.v1.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapper that keeps a scenario along with its associated input and statistics together in the cache.
 */
public record TestScenarioWithExtra<IN, T extends TestScenario>(
        @JsonProperty("i") @JsonAlias("input") IN input,
        @JsonProperty("s") @JsonAlias("scenario") T scenario,
        @JsonProperty("m") @JsonAlias("meta") TestScenarioMeta meta) {

    /**
     * We assume that the scenario is always of the same type without any inheritance.
     * So far it seems true, but we have to verify it library users need to change this assumption.
     */
    public Class<T> type() {
        //noinspection unchecked
        return (Class<T>) scenario.getClass();
    }
}

package com.codechievement.foremka.v1.api;

/**
 * Internal wrapper that keeps a scenario and its associated statistics together in the cache.
 */
public record TestScenarioWithMeta<T extends TestScenario>(T scenario, TestScenarioMeta meta) {}

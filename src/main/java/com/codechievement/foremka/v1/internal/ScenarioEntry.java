package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenario;

/**
 * Represents a serialized entry pairing an input key with its corresponding scenario.
 */
public record ScenarioEntry(Object input, TestScenario scenario) {}

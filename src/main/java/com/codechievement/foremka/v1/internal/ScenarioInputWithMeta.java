package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenario;

public record ScenarioInputWithMeta(Class<? extends TestScenario> type, Object key) {}

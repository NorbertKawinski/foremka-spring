package com.codechievement.foremka.v1.api;

public interface ScenarioFactory<IN, OUT extends TestScenario> {
    Class<OUT> getScenarioClass();

    OUT create(IN input);
}

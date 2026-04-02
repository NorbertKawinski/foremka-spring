package com.codechievement.foremka.v1.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class ScenarioProvider<IN, OUT extends TestScenario> {
    @Autowired
    private TestScenarios scenarios;

    @Autowired
    private ScenarioFactory<IN, OUT> factory;

    public final OUT get(IN input) {
        return scenarios.get(factory, input);
    }
}

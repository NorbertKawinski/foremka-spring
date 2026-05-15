package com.codechievement.foremka.v1.api;

import java.util.function.Function;

public interface ScenarioFactory<IN, OUT extends TestScenario> {
    Class<OUT> getScenarioClass();

    OUT create(IN input);

    static <IN, OUT extends TestScenario> ScenarioFactory<IN, OUT> of(Class<OUT> clazz, Function<IN, OUT> factory) {
        return new ScenarioFactory<IN, OUT>() {
            @Override
            public Class<OUT> getScenarioClass() {
                return clazz;
            }

            @Override
            public OUT create(IN input) {
                return factory.apply(input);
            }
        };
    }
}

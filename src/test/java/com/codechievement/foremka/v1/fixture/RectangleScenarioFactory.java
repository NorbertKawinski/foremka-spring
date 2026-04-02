package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.ScenarioFactory;
import org.springframework.stereotype.Component;

@Component
public class RectangleScenarioFactory implements ScenarioFactory<RectangleInput, RectangleScenario> {
    @Override
    public Class<RectangleScenario> getScenarioClass() {
        return RectangleScenario.class;
    }

    @Override
    public RectangleScenario create(RectangleInput input) {
        int area = input.width() * input.height();
        int perimeter = 2 * (input.width() + input.height());
        return new RectangleScenario(input.width(), input.height(), area, perimeter);
    }
}

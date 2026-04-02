package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.api.TestScenario;

public record RectangleScenario(int width, int height, int area, int perimeter) implements TestScenario {}

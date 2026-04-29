package com.codechievement.foremka.v1.fixture;

import static com.codechievement.foremka.config.ForemkaAutoConfiguration.createObjectMapper;

import com.codechievement.foremka.v1.internal.ScenarioSerializer;

public class TestScenarioSamples {
    public static final String ALICE_INPUT = "alice";
    public static final UserScenario ALICE_SCENARIO = new UserScenario("alice", "alice@test.com");

    public static final String BOB_INPUT = "bob";
    public static final UserScenario BOB_SCENARIO = new UserScenario("bob", "bob@test.com");

    public static final RectangleInput RECTANGLE_INPUT = new RectangleInput(3, 4);
    public static final RectangleScenario RECTANGLE_SCENARIO = new RectangleScenario(3, 4, 12, 14);

    public static final UserScenarioFactory USER_FACTORY = new UserScenarioFactory();
    public static final RectangleScenarioFactory RECTANGLE_FACTORY = new RectangleScenarioFactory();
    public static final ScenarioSerializer SCENARIO_SERIALIZER = new ScenarioSerializer(createObjectMapper());
}

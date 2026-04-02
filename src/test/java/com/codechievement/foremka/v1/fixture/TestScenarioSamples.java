package com.codechievement.foremka.v1.fixture;

import com.codechievement.foremka.v1.internal.ScenarioInputWithMeta;

public class TestScenarioSamples {
    public static final String ALICE_INPUT = "alice";
    public static final ScenarioInputWithMeta ALICE_META_INPUT = new ScenarioInputWithMeta(UserScenario.class, "alice");
    public static final UserScenario ALICE_SCENARIO = new UserScenario("alice", "alice@test.com");

    public static final String BOB_INPUT = "bob";
    public static final ScenarioInputWithMeta BOB_META_INPUT = new ScenarioInputWithMeta(UserScenario.class, "bob");
    public static final UserScenario BOB_SCENARIO = new UserScenario("bob", "bob@test.com");

    public static final RectangleInput RECTANGLE_INPUT = new RectangleInput(3, 4);
    public static final ScenarioInputWithMeta RECTANGLE_META_INPUT =
            new ScenarioInputWithMeta(RectangleScenario.class, RECTANGLE_INPUT);
    public static final RectangleScenario RECTANGLE_SCENARIO = new RectangleScenario(3, 4, 12, 14);

    public static final UserScenarioFactory USER_FACTORY = new UserScenarioFactory();
    public static final RectangleScenarioFactory RECTANGLE_FACTORY = new RectangleScenarioFactory();
}

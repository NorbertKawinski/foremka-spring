package com.codechievement.foremka.v1.fixture;

import static com.codechievement.foremka.config.ForemkaAutoConfiguration.createObjectMapper;

import com.codechievement.foremka.v1.api.TestScenarioMeta;
import com.codechievement.foremka.v1.api.TestScenarioWithMeta;
import com.codechievement.foremka.v1.internal.ScenarioInputWithMeta;
import com.codechievement.foremka.v1.internal.ScenarioSerializer;
import java.time.Duration;
import java.time.Instant;

public class TestScenarioSamples {
    public static final String ALICE_INPUT = "alice";
    public static final ScenarioInputWithMeta ALICE_META_INPUT = new ScenarioInputWithMeta(UserScenario.class, "alice");
    public static final UserScenario ALICE_SCENARIO = new UserScenario("alice", "alice@test.com");
    public static final TestScenarioMeta ALICE_META =
            new TestScenarioMeta(Instant.ofEpochSecond(1), "alice", Duration.ofSeconds(1));
    public static final TestScenarioWithMeta<UserScenario> ALICE_WITH_META =
            new TestScenarioWithMeta<>(ALICE_SCENARIO, ALICE_META);

    public static final String BOB_INPUT = "bob";
    public static final ScenarioInputWithMeta BOB_META_INPUT = new ScenarioInputWithMeta(UserScenario.class, "bob");
    public static final UserScenario BOB_SCENARIO = new UserScenario("bob", "bob@test.com");
    public static final TestScenarioMeta BOB_META =
            new TestScenarioMeta(Instant.ofEpochSecond(2), "bob", Duration.ofSeconds(2));
    public static final TestScenarioWithMeta<UserScenario> BOB_WITH_META =
            new TestScenarioWithMeta<>(BOB_SCENARIO, BOB_META);

    public static final RectangleInput RECTANGLE_INPUT = new RectangleInput(3, 4);
    public static final ScenarioInputWithMeta RECTANGLE_META_INPUT =
            new ScenarioInputWithMeta(RectangleScenario.class, RECTANGLE_INPUT);
    public static final RectangleScenario RECTANGLE_SCENARIO = new RectangleScenario(3, 4, 12, 14);
    public static final TestScenarioMeta RECTANGLE_META =
            new TestScenarioMeta(Instant.ofEpochSecond(3), "rectangle", Duration.ofSeconds(3));
    public static final TestScenarioWithMeta<RectangleScenario> RECTANGLE_WITH_META =
            new TestScenarioWithMeta<>(RECTANGLE_SCENARIO, RECTANGLE_META);

    public static final UserScenarioFactory USER_FACTORY = new UserScenarioFactory();
    public static final RectangleScenarioFactory RECTANGLE_FACTORY = new RectangleScenarioFactory();
    public static final ScenarioSerializer SCENARIO_SERIALIZER = new ScenarioSerializer(createObjectMapper());
}

package com.codechievement.foremka.v1.fixture;

import static com.codechievement.foremka.config.ForemkaAutoConfiguration.createObjectMapper;

import com.codechievement.foremka.v1.internal.ScenarioSerializer;

public class TestScenarioSamples {
    public static final UserScenarioFactory USER_FACTORY = new UserScenarioFactory();
    public static final ProductScenarioFactory PRODUCT_FACTORY = new ProductScenarioFactory();
    public static final ScenarioSerializer SCENARIO_SERIALIZER = new ScenarioSerializer(createObjectMapper());

    public static final String ALICE_INPUT = "alice";
    public static final UserScenario ALICE_SCENARIO = USER_FACTORY.create(ALICE_INPUT);

    public static final String BOB_INPUT = "bob";
    public static final UserScenario BOB_SCENARIO = USER_FACTORY.create(BOB_INPUT);

    public static final ProductInput PRODUCT_INPUT = new ProductInput("Winter Jacket", "SEASONAL", 5000);
    public static final ProductScenario PRODUCT_SCENARIO = PRODUCT_FACTORY.create(PRODUCT_INPUT);
}

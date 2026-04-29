package com.codechievement.foremka.v1.internal;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static java.time.Duration.ofSeconds;
import static java.time.Instant.ofEpochSecond;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.v1.api.TestScenarioMeta;
import com.codechievement.foremka.v1.api.TestScenarioWithExtra;
import com.codechievement.foremka.v1.fixture.RectangleInput;
import com.codechievement.foremka.v1.fixture.RectangleScenario;
import com.codechievement.foremka.v1.fixture.UserScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScenarioSerializerTest {
    private final ScenarioSerializer serializer = SCENARIO_SERIALIZER;
    private TestScenarioWithExtra<String, UserScenario> ALICE_WITH_EXTRA;
    private TestScenarioWithExtra<String, UserScenario> BOB_WITH_EXTRA;
    private TestScenarioWithExtra<RectangleInput, RectangleScenario> RECTANGLE_WITH_EXTRA;

    @BeforeEach
    public void setup() {
        TestScenarioMeta ALICE_META = new TestScenarioMeta(ofEpochSecond(1), "aliceTest", ofSeconds(1));
        ALICE_WITH_EXTRA = new TestScenarioWithExtra<>(ALICE_INPUT, ALICE_SCENARIO, ALICE_META);

        TestScenarioMeta BOB_META = new TestScenarioMeta(ofEpochSecond(2), "bobTest", ofSeconds(2));
        BOB_WITH_EXTRA = new TestScenarioWithExtra<>(BOB_INPUT, BOB_SCENARIO, BOB_META);

        TestScenarioMeta RECTANGLE_META = new TestScenarioMeta(ofEpochSecond(3), "rectangleTest", ofSeconds(3));
        RECTANGLE_WITH_EXTRA = new TestScenarioWithExtra<>(RECTANGLE_INPUT, RECTANGLE_SCENARIO, RECTANGLE_META);
    }

    @Test
    void deserialize_emptyJsonObject_returnsEmptyMap() {
        TestScenariosMap restored = serializer.deserialize("{}");

        assertThat(restored.size(), is(0));
    }

    @Test
    void serializeAndDeserialize_emptyMap_roundTrips() {
        TestScenariosMap original = new TestScenariosMap();

        String json = serializer.serialize(original);
        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(0));
    }

    @Test
    void serializeAndDeserialize_singleScenario_roundTrips() {
        ALICE_WITH_EXTRA.meta().recordUsage("otherTest");
        TestScenariosMap originalScenarios = new TestScenariosMap(ALICE_WITH_EXTRA);

        String json = serializer.serialize(originalScenarios);
        TestScenariosMap restoredScenarios = serializer.deserialize(json);

        assertThat(restoredScenarios.size(), is(1));
        assertThat(restoredScenarios.contains(UserScenario.class, ALICE_INPUT), is(true));
        var restoredScenarioWithExtra = restoredScenarios.get(UserScenario.class, ALICE_INPUT);

        String restoredInput = restoredScenarioWithExtra.input();
        assertThat(restoredInput, is("alice"));

        UserScenario restoredScenario = restoredScenarioWithExtra.scenario();
        assertThat(restoredScenario.username(), is("alice"));
        assertThat(restoredScenario.email(), is("alice@test.com"));

        // Please note that most of meta is restored based on decompression
        TestScenarioMeta restoredMeta = restoredScenarioWithExtra.meta();
        assertThat(restoredMeta.getCreatedAt(), is(ofEpochSecond(1)));
        assertThat(restoredMeta.getCreatedByTest(), is("aliceTest"));
        assertThat(restoredMeta.getCreationDuration(), is(ofSeconds(1)));
        assertThat(restoredMeta.getLastUsedAt(), is(notNullValue()));
        assertThat(restoredMeta.getTotalUsageCount(), is(1L));
        assertThat(restoredMeta.getUsedByTests(), containsInAnyOrder("aliceTest", "otherTest"));
    }

    @Test
    void serializeAndDeserialize_multipleScenarioTypes_roundTrips() {
        TestScenariosMap original = new TestScenariosMap(ALICE_WITH_EXTRA, BOB_WITH_EXTRA, RECTANGLE_WITH_EXTRA);

        String json = serializer.serialize(original);
        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(3));
        assertThat(restored.contains(UserScenario.class, ALICE_INPUT), is(true));
        assertThat(restored.contains(UserScenario.class, BOB_INPUT), is(true));
        assertThat(restored.contains(RectangleScenario.class, RECTANGLE_INPUT), is(true));
    }

    @Test
    void serialize_compressedMeta_skipsNullMetaFields() {
        TestScenariosMap scenarios = new TestScenariosMap(ALICE_WITH_EXTRA);
        var scenario = scenarios.get(UserScenario.class, ALICE_INPUT);

        String json = serializer.serialize(scenarios);

        assertThat(scenario.meta().getLastUsedAt(), is(nullValue()));
        assertThat(json, not(containsString("null")));
    }

    @Test
    void serialize_totalUsageCountEqualsOne_omitsTuField() {
        ALICE_WITH_EXTRA.meta().recordUsage("");
        TestScenariosMap scenarios = new TestScenariosMap(ALICE_WITH_EXTRA);

        String json = serializer.serialize(scenarios);

        assertThat(ALICE_WITH_EXTRA.meta().getTotalUsageCount(), is(1L));
        assertThat(json, not(containsString("\"tu\"")));
    }

    @Test
    void serialize_totalUsageCountGreaterThanOne_includesTuField() {
        ALICE_WITH_EXTRA.meta().recordUsage("otherTest");
        ALICE_WITH_EXTRA.meta().recordUsage("otherTest2");
        TestScenariosMap scenarios = new TestScenariosMap(ALICE_WITH_EXTRA);

        String json = serializer.serialize(scenarios);

        assertThat(ALICE_WITH_EXTRA.meta().getTotalUsageCount(), is(2L));
        assertThat(json, containsString("\"tu\":2"));
    }

    @Test
    void deserialize_mixedValidAndInvalidTypes_preservesValidOnes() {
        TestScenariosMap original = new TestScenariosMap(ALICE_WITH_EXTRA, RECTANGLE_WITH_EXTRA);
        String json = serializer.serialize(original).replace("RectangleScenario", "InvalidScenario");

        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(1));
        assertThat(restored.contains(UserScenario.class, ALICE_INPUT), is(true));
        assertThat(restored.contains(RectangleScenario.class, RECTANGLE_INPUT), is(false));
    }
}

package com.codechievement.foremka.v1.internal;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.v1.api.TestScenarioMeta;
import com.codechievement.foremka.v1.fixture.UserScenario;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ScenarioSerializerTest {
    private final ScenarioSerializer serializer = SCENARIO_SERIALIZER;

    @Test
    void deserialize_emptyJsonObject_returnsEmptyMap() {
        TestScenariosMap restored = serializer.deserialize("{}");

        assertThat(restored, is(anEmptyMap()));
    }

    @Test
    void serializeAndDeserialize_emptyMap_roundTrips() {
        TestScenariosMap original = new TestScenariosMap();

        String json = serializer.serialize(original);
        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored, is(anEmptyMap()));
    }

    @Test
    void serializeAndDeserialize_singleScenario_roundTrips() {
        TestScenariosMap original = new TestScenariosMap();
        original.put(ALICE_META_INPUT, ALICE_WITH_META);

        String json = serializer.serialize(original);
        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(1));
        assertThat(restored.containsKey(ALICE_META_INPUT), is(true));
        var restoredScenarioWithMeta = restored.get(ALICE_META_INPUT);
        UserScenario restoredScenario = (UserScenario) restoredScenarioWithMeta.scenario();
        assertThat(restoredScenario.username(), is("alice"));
        assertThat(restoredScenario.email(), is("alice@test.com"));
        TestScenarioMeta meta = restoredScenarioWithMeta.meta();
        assertThat(meta.getCreatedByTest(), is("alice"));
        assertThat(meta.getCreationDuration(), is(Duration.ofSeconds(1)));
    }

    @Test
    void serializeAndDeserialize_multipleScenarioTypes_roundTrips() {
        TestScenariosMap original = new TestScenariosMap();
        original.put(ALICE_META_INPUT, ALICE_WITH_META);
        original.put(BOB_META_INPUT, BOB_WITH_META);
        original.put(RECTANGLE_META_INPUT, RECTANGLE_WITH_META);

        String json = serializer.serialize(original);
        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(3));
        assertThat(restored, hasValue(ALICE_WITH_META));
        assertThat(restored, hasValue(BOB_WITH_META));
        assertThat(restored, hasValue(RECTANGLE_WITH_META));
    }

    @Test
    void deserialize_mixedValidAndInvalidTypes_preservesValidOnes() {
        TestScenariosMap original = new TestScenariosMap();
        original.put(ALICE_META_INPUT, ALICE_WITH_META);
        original.put(RECTANGLE_META_INPUT, RECTANGLE_WITH_META);
        String json = serializer.serialize(original).replace("RectangleScenario", "InvalidScenario");

        TestScenariosMap restored = serializer.deserialize(json);

        assertThat(restored.size(), is(1));
        assertThat(restored, hasValue(ALICE_WITH_META));
    }
}

package com.codechievement.foremka.v1.internal;

import static com.codechievement.foremka.v1.fixture.TestScenarioSamples.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.codechievement.foremka.v1.api.TestScenario;
import com.codechievement.foremka.v1.fixture.UserScenario;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScenarioSerializerTest {
    private ScenarioSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new ScenarioSerializer(new ObjectMapper());
    }

    @Test
    void deserialize_emptyJsonObject_returnsEmptyMap() {
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> restored = serializer.deserialize("{}");

        assertThat(restored, is(anEmptyMap()));
    }

    @Test
    void serializeAndDeserialize_emptyMap_roundTrips() {
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> original = new ConcurrentHashMap<>();

        String json = serializer.serialize(original);
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> restored = serializer.deserialize(json);

        assertThat(restored, is(anEmptyMap()));
    }

    @Test
    void serializeAndDeserialize_singleScenario_roundTrips() {
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> original = new ConcurrentHashMap<>();
        original.put(ALICE_META_INPUT, ALICE_SCENARIO);

        String json = serializer.serialize(original);
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> restored = serializer.deserialize(json);

        assertThat(restored.size(), is(1));
        assertThat(restored.containsKey(ALICE_META_INPUT), is(true));
        UserScenario restoredScenario = (UserScenario) restored.get(ALICE_META_INPUT);
        assertThat(restoredScenario.username(), is("alice"));
        assertThat(restoredScenario.email(), is("alice@test.com"));
    }

    @Test
    void serializeAndDeserialize_multipleScenarioTypes_roundTrips() {
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> original = new ConcurrentHashMap<>();
        original.put(ALICE_META_INPUT, ALICE_SCENARIO);
        original.put(BOB_META_INPUT, BOB_SCENARIO);
        original.put(RECTANGLE_META_INPUT, RECTANGLE_SCENARIO);

        String json = serializer.serialize(original);
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> restored = serializer.deserialize(json);

        assertThat(restored.size(), is(3));
        assertThat(restored, hasValue(ALICE_SCENARIO));
        assertThat(restored, hasValue(BOB_SCENARIO));
        assertThat(restored, hasValue(RECTANGLE_SCENARIO));
    }

    @Test
    void deserialize_mixedValidAndInvalidTypes_preservesValidOnes() {
        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> original = new ConcurrentHashMap<>();
        original.put(ALICE_META_INPUT, ALICE_SCENARIO);
        original.put(RECTANGLE_META_INPUT, RECTANGLE_SCENARIO);
        String json = serializer.serialize(original).replace("RectangleScenario", "InvalidScenario");

        ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> restored = serializer.deserialize(json);

        assertThat(restored.size(), is(1));
        assertThat(restored, hasValue(ALICE_SCENARIO));
    }
}

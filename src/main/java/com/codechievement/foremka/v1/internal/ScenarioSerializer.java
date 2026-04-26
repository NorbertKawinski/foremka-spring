package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenario;
import com.codechievement.foremka.v1.api.TestScenarioMeta;
import com.codechievement.foremka.v1.api.TestScenarioWithMeta;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for converting scenario data to and from its serialized (JSON) string representation.
 *
 * <p>Scenario type classes are stored as their fully-qualified class names to allow
 * deserialization across JVM restarts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioSerializer {
    public record SerializableScenarioEntry(Object input, TestScenario scenario, TestScenarioMeta meta) {}

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String serialize(TestScenariosMap scenarios) {
        Map<String, List<SerializableScenarioEntry>> serializable = new HashMap<>();
        scenarios.forEach((input, holder) -> {
            String typeName = input.type().getName();
            serializable
                    .computeIfAbsent(typeName, k -> new ArrayList<>())
                    .add(new SerializableScenarioEntry(input.key(), holder.scenario(), holder.meta()));
        });
        return objectMapper.writeValueAsString(serializable);
    }

    @SneakyThrows
    public TestScenariosMap deserialize(String data) {
        TestScenariosMap result = new TestScenariosMap();
        Map<String, List<Map<String, Object>>> raw = objectMapper.readValue(data, new TypeReference<>() {});

        for (Map.Entry<String, List<Map<String, Object>>> typeEntry : raw.entrySet()) {
            String typeName = typeEntry.getKey();
            Class<? extends TestScenario> scenarioType;
            try {
                scenarioType = ClassUtils.forName(typeName);
            } catch (ClassNotFoundException e) {
                log.warn("Skipping scenario type that cannot be resolved: {}", typeName);
                // Users may have removed or renamed scenario classes since the last run.
                // The library will recover automatically by recreating missing scenarios as needed,
                // so we can safely skip unresolvable types at the cost of time needed to do so.
                continue;
            }
            for (Map<String, Object> rawEntry : typeEntry.getValue()) {
                Object input = rawEntry.get("input");
                Object rawScenario = rawEntry.get("scenario");
                Object rawMeta = rawEntry.get("meta");
                deserializeEntry(result, scenarioType, input, rawScenario, rawMeta);
            }
        }
        return result;
    }

    private <T extends TestScenario> void deserializeEntry(
            TestScenariosMap result, Class<T> scenarioType, Object input, Object rawScenario, Object rawMeta) {
        T scenario = objectMapper.convertValue(rawScenario, scenarioType);
        TestScenarioMeta meta = objectMapper.convertValue(rawMeta, TestScenarioMeta.class);
        result.put(new ScenarioInputWithMeta(scenarioType, input), new TestScenarioWithMeta<>(scenario, meta));
    }
}

package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenario;
import com.codechievement.foremka.v1.api.TestScenarioWithExtra;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String serialize(TestScenariosMap scenarios) {
        Map<String, List<TestScenarioWithExtra<?, ?>>> serializable = new HashMap<>();

        scenarios.forEach(s -> {
            s.meta().compress();

            String scenarioClazzName = s.scenario().getClass().getName();
            String inputClazzName = s.input().getClass().getName();
            String key = scenarioClazzName + "/" + inputClazzName;

            serializable.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        });

        JsonNode tree = objectMapper.valueToTree(serializable);
        return objectMapper.writeValueAsString(tree);
    }

    @SneakyThrows
    public TestScenariosMap deserialize(String data) {
        TestScenariosMap result = new TestScenariosMap();
        JsonNode rawScenariosByClazz = objectMapper.readTree(data);

        for (Entry<String, JsonNode> rawScenariosEntry : rawScenariosByClazz.properties()) {
            JavaType type = parseType(rawScenariosEntry.getKey());
            if (type == null) {
                continue;
            }

            JsonNode rawScenarios = rawScenariosEntry.getValue();
            rawScenarios
                    .valueStream()
                    .map(rawScenario -> deserialize(type, rawScenario))
                    .filter(Objects::nonNull)
                    .forEach(result::add);
        }
        return result;
    }

    private @Nullable JavaType parseType(String key) {
        Class<? extends TestScenario> scenarioClazz;
        Class<?> inputClazz;
        String[] keyParts = key.split("/");
        String scenarioClazzName = keyParts[0];
        String inputClazzName = keyParts[1];
        try {
            scenarioClazz = ClassUtils.forName(scenarioClazzName);
            inputClazz = ClassUtils.forName(inputClazzName);
            return objectMapper
                    .getTypeFactory()
                    .constructParametricType(TestScenarioWithExtra.class, inputClazz, scenarioClazz);
        } catch (ClassNotFoundException e) {
            log.warn("Skipping scenario type that cannot be resolved: {}", key);
            // Users may have removed or renamed scenario classes since the last run.
            // The library will recover automatically by recreating missing scenarios as needed,
            // so we can safely skip unresolvable types at the cost of time needed to do so.
            return null;
        }
    }

    private <IN, T extends TestScenario> @Nullable TestScenarioWithExtra<IN, T> deserialize(
            JavaType type, JsonNode rawScenario) {
        try {
            TestScenarioWithExtra<IN, T> scenario = objectMapper.treeToValue(rawScenario, type);
            scenario.meta().decompress();
            return scenario;
        } catch (Exception e) {
            // Users may have changed the structure of scenario classes since the last run, causing deserialization to
            // fail.
            // The library will recover automatically by recreating missing scenarios as needed,
            // so we can safely skip unresolvable entries at the cost of time needed to do so.
            log.warn("Failed to deserialize scenario entry of type {}. Skipping entry.", type, e);
            return null;
        }
    }
}

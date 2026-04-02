package com.codechievement.foremka.v1.api;

import com.codechievement.foremka.v1.internal.ScenarioInputWithMeta;
import com.codechievement.foremka.v1.internal.ScenarioSerializer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestScenarios implements InitializingBean, DisposableBean {
    private final ScenarioRepository repository;
    private final ScenarioSerializer serializer;
    private ConcurrentHashMap<ScenarioInputWithMeta, TestScenario> scenarios;

    /**
     * Loads all scenarios from the repository into the in-memory cache on startup.
     */
    @Override
    public void afterPropertiesSet() {
        scenarios = repository.findAll().map(serializer::deserialize).orElseGet(ConcurrentHashMap::new);
    }

    /**
     * Saves all in-memory scenarios to the repository when the Spring context is closed.
     * This is triggered automatically when JUnit finishes all tests and the context shuts down.
     */
    @Override
    public void destroy() {
        repository.saveAll(serializer.serialize(scenarios));
    }

    public <T extends TestScenario> T computeIfAbsent(Class<T> clazz, Supplier<T> supplier) {
        return computeIfAbsent(clazz, "default", k -> supplier.get());
    }

    @SuppressWarnings("unchecked")
    public <IN, T extends TestScenario> T computeIfAbsent(Class<T> clazz, IN input, Function<IN, T> supplier) {
        var input2 = new ScenarioInputWithMeta(clazz, input);
        return (T) scenarios.computeIfAbsent(input2, k -> supplier.apply(input));
    }

    public <IN, OUT extends TestScenario> OUT get(ScenarioFactory<IN, OUT> factory, IN input) {
        return computeIfAbsent(factory.getScenarioClass(), input, factory::create);
    }
}

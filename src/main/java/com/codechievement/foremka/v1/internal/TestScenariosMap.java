package com.codechievement.foremka.v1.internal;

import com.codechievement.foremka.v1.api.TestScenario;
import com.codechievement.foremka.v1.api.TestScenarioWithExtra;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestScenariosMap {
    public record Key(Class<? extends TestScenario> scenarioType, Object scenarioInput) {}

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<Key, TestScenarioWithExtra> delegate;

    public TestScenariosMap() {
        delegate = new ConcurrentHashMap<>();
    }

    public TestScenariosMap(@SuppressWarnings("rawtypes") TestScenarioWithExtra... scenarios) {
        this();
        for (TestScenarioWithExtra<?, ?> scenario : scenarios) {
            add(scenario);
        }
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> computeIfAbsent(
            Class<T> scenarioType, IN scenarioInput, Supplier<TestScenarioWithExtra<IN, T>> supplier) {
        var key = new Key(scenarioType, scenarioInput);
        var result = delegate.computeIfAbsent(key, k -> supplier.get());
        //noinspection unchecked
        return (TestScenarioWithExtra<IN, T>) result;
    }

    public void add(TestScenarioWithExtra<?, ?> scenario) {
        var key = new Key(scenario.scenario().getClass(), scenario.input());
        delegate.put(key, scenario);
    }

    public void removeIf(Predicate<TestScenarioWithExtra<?, ?>> predicate) {
        delegate.entrySet().removeIf(e -> predicate.test(e.getValue()));
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> get(Class<T> scenarioType, IN scenarioInput) {
        var key = new Key(scenarioType, scenarioInput);
        //noinspection unchecked
        return delegate.get(key);
    }

    public int size() {
        return delegate.size();
    }

    public boolean contains(Class<? extends TestScenario> scenarioType, Object scenarioInput) {
        var key = new Key(scenarioType, scenarioInput);
        return delegate.containsKey(key);
    }

    public Stream<TestScenarioWithExtra<?, ?>> stream() {
        return delegate.values().stream().map(e -> (TestScenarioWithExtra<?, ?>) e);
    }

    public void forEach(Consumer<TestScenarioWithExtra<?, ?>> consumer) {
        delegate.values().forEach(e -> consumer.accept((TestScenarioWithExtra<?, ?>) e));
    }
}

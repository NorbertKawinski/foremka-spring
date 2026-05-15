package com.codechievement.foremka.v1.internal;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.codechievement.foremka.v1.api.TestScenario;
import com.codechievement.foremka.v1.api.TestScenarioWithExtra;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TestScenariosMap {
    public record Key<IN, T extends TestScenario>(Class<T> scenarioType, IN scenarioInput) {}

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<Key, CompletableFuture<TestScenarioWithExtra>> delegate;

    public TestScenariosMap(TestScenarioWithExtra<?, ?>... scenarios) {
        delegate = new ConcurrentHashMap<>();
        Arrays.stream(scenarios).forEach(this::add);
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> computeIfAbsent(
            Class<T> scenarioType, IN scenarioInput, Supplier<TestScenarioWithExtra<IN, T>> supplier) {
        var key = new Key<>(scenarioType, scenarioInput);

        // Originally we used ConcurrentHashMap.computeIfAbsent and this method was much simpler.
        // However, this had a critical limitation: it forbade recursive map mutations inside the mapping function.
        // Such limitation made it impossible to create one scenario from inside another scenario's factory.
        // To allow that, we call the supplier manually *outside* the ConcurrentHashMap internals.
        // ...
        // Above change resulted in (more or less)
        // #1. var existing = map.get();
        // #2. if (existing != null) return existing;
        // #3. var computed = factory.create();
        // #4. existing = map.putIfAbsent(computed);
        // #5. return existing != null ? existing : computed;
        // While this allows to recursively create scenarios, it also opens risk of race condition between lines #1-4
        // ...
        // The full solution uses a CompletableFuture acting as a promise to atomically reserve scenario slot
        // via .putIfAbsent() and then call the supplier *outside* any map operation allowing for recursive operations.
        // The first thread to win the .putIfAbsent() is the sole caller of the supplier.
        // Other threads just join() on the same winner's Future.

        var promise = new CompletableFuture<TestScenarioWithExtra<IN, T>>();

        //noinspection unchecked,rawtypes
        var existing = (CompletableFuture<TestScenarioWithExtra<IN, T>>)
                delegate.putIfAbsent(key, (CompletableFuture) promise);
        if (existing != null) {
            // Another thread already reserved this key – wait for it to finish.
            return unchecked(existing.join());
        }

        // We own the promise; compute and complete it (or remove + fail on error).
        try {
            var computed = supplier.get();
            promise.complete(computed);
            return computed;
        } catch (RuntimeException e) {
            // Remove our incomplete promise so later callers can retry.
            delegate.remove(key);
            promise.completeExceptionally(e);
            throw e;
        }
    }

    public <IN, T extends TestScenario> void add(TestScenarioWithExtra<IN, T> scenario) {
        var key = new Key<IN, T>(scenario.type(), scenario.input());
        delegate.put(key, completedFuture(scenario));
    }

    public void removeIf(Predicate<TestScenarioWithExtra<?, ?>> predicate) {
        delegate.entrySet().removeIf(e -> {
            var f = e.getValue();
            if (!f.isDone() || f.isCompletedExceptionally()) return false;
            return predicate.test(unchecked(f).join());
        });
    }

    public <IN, T extends TestScenario> TestScenarioWithExtra<IN, T> get(Class<T> scenarioType, IN scenarioInput) {
        var key = new Key<>(scenarioType, scenarioInput);
        CompletableFuture<TestScenarioWithExtra<IN, T>> result = unchecked(delegate.get(key));
        return result.join();
    }

    public int size() {
        return delegate.size();
    }

    public <IN, T extends TestScenario> boolean contains(Class<T> scenarioType, IN scenarioInput) {
        var key = new Key<>(scenarioType, scenarioInput);
        return delegate.containsKey(key);
    }

    public Stream<TestScenarioWithExtra<?, ?>> stream() {
        return delegate.values().stream()
                .filter(f -> f.isDone() && !f.isCompletedExceptionally())
                .map(f -> unchecked(f).join());
    }

    public void forEach(Consumer<TestScenarioWithExtra<?, ?>> consumer) {
        delegate.values().forEach(f -> {
            if (f.isDone() && !f.isCompletedExceptionally()) {
                consumer.accept(unchecked(f).join());
            }
        });
    }

    private <T extends TestScenarioWithExtra<?, ?>> T unchecked(TestScenarioWithExtra<?, ?> scenario) {
        //noinspection unchecked
        return (T) scenario;
    }

    private <IN, T extends TestScenario> CompletableFuture<TestScenarioWithExtra<IN, T>> unchecked(
            @SuppressWarnings("rawtypes") CompletableFuture<TestScenarioWithExtra> scenario) {
        //noinspection unchecked
        return (CompletableFuture<TestScenarioWithExtra<IN, T>>) (CompletableFuture<?>) scenario;
    }
}

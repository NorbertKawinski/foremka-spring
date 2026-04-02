package com.codechievement.foremka.v1.components;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import java.util.Optional;

/**
 * In-memory implementation of {@link ScenarioRepository}.
 *
 * <p>Does not persist data across JVM restarts.
 */
public class InMemoryScenarioRepository implements ScenarioRepository {

    private String data;

    @Override
    public Optional<String> findAll() {
        return Optional.ofNullable(data);
    }

    @Override
    public void saveAll(String data) {
        this.data = data;
    }
}

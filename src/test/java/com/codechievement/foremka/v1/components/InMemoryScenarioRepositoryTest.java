package com.codechievement.foremka.v1.components;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryScenarioRepositoryTest {
    private InMemoryScenarioRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryScenarioRepository();
    }

    @Test
    void findAll_returnsEmptyOptionalInitially() {
        Optional<String> result = repository.findAll();
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void saveAll_thenFindAll_returnsSavedData() {
        repository.saveAll("DATA");
        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("DATA"));
    }

    @Test
    void saveAll_overwitesPreviousData() {
        repository.saveAll("first");
        repository.saveAll("second");
        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("second"));
    }
}

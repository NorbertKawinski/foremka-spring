package com.codechievement.foremka.v1.components;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileScenarioRepositoryTest {

    @TempDir
    Path tempDir;

    Path scenarioFilePath;

    FileScenarioRepository repository;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        scenarioFilePath = tempDir.resolve("foremka_test_scenarios").resolve(randomUUID() + ".db");
        if (Files.exists(scenarioFilePath)) {
            Files.delete(scenarioFilePath);
        }
        repository = new FileScenarioRepository(scenarioFilePath);
    }

    @Test
    void findAll_returnsEmptyOptionalWhenFileDoesNotExist() {
        assertThat(Files.exists(scenarioFilePath), is(false));

        Optional<String> result = repository.findAll();

        assertThat(result, is(Optional.empty()));
    }

    @Test
    void saveAll_thenFindAll_returnsPersistedData() {
        repository.saveAll("DATA");

        Optional<String> result = repository.findAll();

        assertThat(result.orElseThrow(), is("DATA"));
    }

    @Test
    void saveAll_createsRequiredPaths() {
        assertThat(Files.exists(scenarioFilePath.getParent()), is(false));
        assertThat(Files.exists(scenarioFilePath), is(false));

        repository.saveAll("DATA");

        assertThat(Files.exists(scenarioFilePath.getParent()), is(true));
        assertThat(Files.exists(scenarioFilePath), is(true));
    }

    @Test
    void saveAll_overwritesPreviousData() {
        repository.saveAll("first");
        repository.saveAll("second");

        Optional<String> result = repository.findAll();

        assertThat(result.orElseThrow(), is("second"));
    }

    @Test
    void findAll_readsExistingFileContent() throws IOException {
        Files.createDirectories(scenarioFilePath.getParent());
        Files.writeString(scenarioFilePath, "DATA");

        Optional<String> result = repository.findAll();

        assertThat(result.orElseThrow(), is("DATA"));
    }
}

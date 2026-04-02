package com.codechievement.foremka.v1.components;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.SneakyThrows;

/**
 * File-based implementation of {@link ScenarioRepository}.
 */
public class FileScenarioRepository implements ScenarioRepository {

    private final Path filePath;

    public FileScenarioRepository(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    @SneakyThrows
    public Optional<String> findAll() {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        return Optional.of(Files.readString(filePath));
    }

    @Override
    @SneakyThrows
    public void saveAll(String data) {
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(filePath, data);
    }
}

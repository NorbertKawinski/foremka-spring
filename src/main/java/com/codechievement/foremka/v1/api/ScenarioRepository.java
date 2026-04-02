package com.codechievement.foremka.v1.api;

import java.util.Optional;

/**
 * Repository for storing and restoring serialized scenario data to/from persistent storage.
 *
 * <p>Implementations provide different storage backends (in-memory, file, database).
 */
public interface ScenarioRepository {

    Optional<String> findAll();

    void saveAll(String data);
}

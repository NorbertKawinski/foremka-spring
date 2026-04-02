package com.codechievement.foremka.v1.components;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseScenarioRepositoryTest {
    private DataSource dataSource;
    private DatabaseScenarioRepository repository;

    private DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        // Use a unique in-memory DB per test to ensure isolation
        // CASE_INSENSITIVE_IDENTIFIERS=TRUE makes getTables() match case-insensitively (like PostgreSQL/MySQL)
        ds.setURL("jdbc:h2:mem:testdb_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE");
        return ds;
    }

    @BeforeEach
    void setUp() {
        dataSource = createDataSource();
        repository = new DatabaseScenarioRepository(dataSource);
        repository.afterPropertiesSet();
    }

    @Test
    void createsTableAndInitialRow() {
        Optional<String> result = repository.findAll();

        assertThat(result.orElseThrow(), is("{}"));
    }

    @Test
    void construction_isIdempotent() {
        repository = new DatabaseScenarioRepository(dataSource);

        assertDoesNotThrow(() -> {
            repository.afterPropertiesSet();
        });

        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("{}"));
    }

    @Test
    void saveAll_thenFindAll_returnsSavedData() {
        repository.saveAll("DATA");

        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("DATA"));
    }

    @Test
    void saveAll_overwritesPreviousData() {
        repository.saveAll("first");
        repository.saveAll("second");

        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("second"));
    }

    @Test
    void customTableName_isUsed() {
        DatabaseScenarioRepository repository = new DatabaseScenarioRepository(dataSource, "custom_table");
        repository.afterPropertiesSet();

        repository.saveAll("custom_data");

        Optional<String> result = repository.findAll();
        assertThat(result.orElseThrow(), is("custom_data"));
    }

    @Test
    void constructor_rejectsInvalidTableName() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseScenarioRepository(dataSource, "DROP TABLE;--"));
    }
}

package com.codechievement.foremka.v1.components;

import com.codechievement.foremka.v1.api.ScenarioRepository;
import com.codechievement.foremka.v1.internal.DatabaseUtils;
import com.codechievement.foremka.v1.internal.DatabaseUtils.SafeIdentifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.springframework.beans.factory.InitializingBean;

/**
 * Database (JDBC) implementation of {@link ScenarioRepository}.
 *
 * <p>Recommended for local development because scenario data is automatically cleared
 * whenever the underlying database is reset.
 */
public class DatabaseScenarioRepository implements ScenarioRepository, InitializingBean {
    private static final String DEFAULT_TABLE_NAME = "foremka_test_scenarios";

    private final DataSource dataSource;
    private final SafeIdentifier tableName;

    public DatabaseScenarioRepository(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }

    public DatabaseScenarioRepository(DataSource dataSource, String tableName) {
        this.dataSource = dataSource;
        this.tableName = new SafeIdentifier(tableName);
    }

    @Override
    public void afterPropertiesSet() {
        createTableIfNotExists();
    }

    @SneakyThrows
    private void createTableIfNotExists() {
        String createSql =
                "CREATE TABLE " + tableName + " (" + "id INT NOT NULL PRIMARY KEY, " + "data TEXT NOT NULL" + ")";

        String insertSql = "INSERT INTO " + tableName + " (id, data) VALUES (1, '{}')";

        try (Connection conn = dataSource.getConnection()) {
            if (DatabaseUtils.tableExists(conn, tableName)) {
                return;
            }
            try (PreparedStatement createPs = conn.prepareStatement(createSql)) {
                createPs.execute();
            }
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.execute();
            }
        }
    }

    @Override
    @SneakyThrows
    public Optional<String> findAll() {
        String sql = "SELECT data FROM " + tableName + " WHERE id = 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(rs.getString("data"));
        }
    }

    @Override
    @SneakyThrows
    public void saveAll(String data) {
        String updateSql = "UPDATE " + tableName + " SET data = ? WHERE id = 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

            updatePs.setString(1, data);
            int updated = updatePs.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException("No rows affected");
            }
        }
    }
}

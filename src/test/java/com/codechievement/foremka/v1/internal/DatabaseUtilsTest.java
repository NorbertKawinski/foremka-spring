package com.codechievement.foremka.v1.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.codechievement.foremka.v1.internal.DatabaseUtils.SafeIdentifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DatabaseUtilsTest {

    // --- SafeIdentifier tests ---

    @ParameterizedTest
    @ValueSource(strings = {"foo", "foo_bar", "A123", "TABLE_NAME", "a", "ABC_DEF_123"})
    void safeIdentifier_acceptsValidNames(String name) {
        SafeIdentifier id = new SafeIdentifier(name);
        assertThat(id.v(), is(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "foo bar", "DROP TABLE;--", "foo.bar", "foo-bar", "foo()", "123 abc"})
    void safeIdentifier_rejectsInvalidNames(String name) {
        assertThrows(IllegalArgumentException.class, () -> new SafeIdentifier(name));
    }

    @Test
    void safeIdentifier_rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new SafeIdentifier(null));
    }

    @Test
    void safeIdentifier_toStringReturnsValue() {
        SafeIdentifier id = new SafeIdentifier("my_table");
        assertThat(id.toString(), is("my_table"));
    }

    // --- tableExists tests ---

    @Test
    void tableExists_returnsFalseForNonExistentTable() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:dbutils_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");

        try (Connection conn = ds.getConnection()) {
            boolean exists = DatabaseUtils.tableExists(conn, new SafeIdentifier("nonexistent_table"));
            assertThat(exists, is(false));
        }
    }

    @Test
    void tableExists_returnsTrueAfterTableIsCreated() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:dbutils_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");

        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE test_table (id INT PRIMARY KEY)")) {
                ps.execute();
            }
            boolean exists = DatabaseUtils.tableExists(conn, new SafeIdentifier("TEST_TABLE"));
            assertThat(exists, is(true));
        }
    }
}

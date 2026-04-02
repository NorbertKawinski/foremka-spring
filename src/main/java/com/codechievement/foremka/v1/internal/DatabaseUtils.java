package com.codechievement.foremka.v1.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jspecify.annotations.NonNull;

public class DatabaseUtils {

    public record SafeIdentifier(String v) {
        public SafeIdentifier {
            if (v == null || !v.matches("[a-zA-Z0-9_]+")) {
                throw new IllegalArgumentException("Unsafe identifier: " + v);
            }
        }

        @Override
        public @NonNull String toString() {
            return v;
        }
    }

    public static boolean tableExists(Connection conn, SafeIdentifier tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName.v(), new String[] {"TABLE"})) {
            return rs.next();
        }
    }
}

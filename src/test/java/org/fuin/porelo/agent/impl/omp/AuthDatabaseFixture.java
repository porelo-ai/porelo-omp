package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.jspecify.annotations.Nullable;

/**
 * Writes an Oh My Pi database as 18.18.1 does: its two sign-in tables, schema version 8, and the
 * journal in WAL. The connection stays open until closed, like Oh My Pi's while it runs, so the
 * rows sit in the WAL and a reader that skips it sees none.
 */
final class AuthDatabaseFixture implements AutoCloseable {

    /**
     * The tables as 18.18.1's {@code packages/ai/src/auth/sqlite-credential-store.ts} creates them.
     */
    private static final String SCHEMA =
            """
            CREATE TABLE auth_credentials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                provider TEXT NOT NULL,
                credential_type TEXT NOT NULL,
                data TEXT NOT NULL,
                disabled_cause TEXT DEFAULT NULL,
                identity_key TEXT DEFAULT NULL,
                created_at INTEGER NOT NULL DEFAULT (CAST(strftime('%s','now') AS INTEGER)),
                updated_at INTEGER NOT NULL DEFAULT (CAST(strftime('%s','now') AS INTEGER))
            );
            CREATE TABLE auth_schema_version (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                version INTEGER NOT NULL
            );
            """;

    private final Connection connection;

    private AuthDatabaseFixture(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates the database at the given path with the given schema version.
     *
     * @param file Where, normally {@code agent.db} in a config directory.
     * @param version The version {@code auth_schema_version} holds.
     * @return The open database.
     * @throws SQLException If it cannot be written.
     */
    static AuthDatabaseFixture create(final Path file, final int version) throws SQLException {
        final Connection connection = DriverManager.getConnection(OmpAuthDatabase.url(file));
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            // Rows stay in the WAL as long as nothing checkpoints them.
            statement.execute("PRAGMA wal_autocheckpoint=0");
            for (final String table : SCHEMA.split(";")) {
                if (!table.isBlank()) {
                    statement.execute(table);
                }
            }
            statement.execute(
                    "INSERT INTO auth_schema_version(id, version) VALUES (1, " + version + ")");
        }
        return new AuthDatabaseFixture(connection);
    }

    /**
     * Adds a row.
     *
     * @param provider Oh My Pi's provider id.
     * @param type {@code api_key} or {@code oauth}.
     * @param data The row's JSON.
     * @param disabledCause Set for a tombstone, else null.
     * @return This.
     * @throws SQLException If it cannot be written.
     */
    AuthDatabaseFixture add(
            final String provider,
            final String type,
            final String data,
            final @Nullable String disabledCause)
            throws SQLException {
        try (PreparedStatement insert =
                connection.prepareStatement(
                        "INSERT INTO auth_credentials (provider, credential_type, data,"
                                + " disabled_cause) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, provider);
            insert.setString(2, type);
            insert.setString(3, data);
            insert.setString(4, disabledCause);
            insert.executeUpdate();
        }
        return this;
    }

    /**
     * Adds a live row.
     *
     * @param provider Oh My Pi's provider id.
     * @param type {@code api_key} or {@code oauth}.
     * @param data The row's JSON.
     * @return This.
     * @throws SQLException If it cannot be written.
     */
    AuthDatabaseFixture add(final String provider, final String type, final String data)
            throws SQLException {
        return add(provider, type, data, null);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}

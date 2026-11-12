package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fuin.porelo.wire.Json;
import org.fuin.porelo.wire.JsonException;
import org.jspecify.annotations.Nullable;
import org.sqlite.SQLiteConfig;

/**
 * The sign-ins in Oh My Pi's database, read once; a value they hold never leaves this package but
 * through {@link OmpCredentialExtractor}.
 *
 * <p><strong>What is read.</strong> Oh My Pi keeps its sign-ins in {@code agent.db}, table {@code
 * auth_credentials}: a provider, {@code credential_type} {@code api_key} or {@code oauth}, {@code
 * data} as JSON, and {@code disabled_cause}, set on a row it keeps as a tombstone. It lists the
 * live rows {@code ORDER BY id}, and so does this. The table's own version stands in {@code
 * auth_schema_version}; a version other than the ones read in the pinned package is a schema this
 * reader does not know, which says nothing about a sign-in. Read in 18.18.1's {@code
 * packages/ai/src/auth/sqlite-credential-store.ts}, and in a database it wrote.
 *
 * <p><strong>Never in Oh My Pi's way.</strong> The database is open while Oh My Pi runs, with its
 * journal in WAL, where its rows may be all there is. So the file is opened read-only, never
 * immutable (which would skip the WAL), with a short busy timeout, one query per table, and closed
 * at once. A WAL reader blocks no writer; at worst it holds a checkpoint back for one query.
 *
 * @param path The database.
 * @param state What could be read of it.
 * @param rows The live rows, in Oh My Pi's order; empty unless {@link State#READ}.
 */
record OmpAuthDatabase(Path path, State state, List<Row> rows) {

    /** The file's name in Oh My Pi's config directory. */
    static final String FILE = "agent.db";

    /** The versions of {@code auth_schema_version} read in the pinned package: 18.18.1's. */
    static final Set<Integer> KNOWN_VERSIONS = Set.of(8);

    /** How long a read waits for a lock, in milliseconds; a reader of WAL rarely needs one. */
    private static final int BUSY_TIMEOUT_MS = 1000;

    /** What could be read of the database. */
    enum State {

        /** There is no file. */
        ABSENT,

        /** It cannot be opened or queried, or is no SQLite database. */
        UNREADABLE,

        /** It is a database, of a sign-in schema this reader does not know. */
        UNKNOWN_SCHEMA,

        /** Read; its rows are the live ones. */
        READ
    }

    /**
     * One live sign-in.
     *
     * @param id Oh My Pi's row id, which orders several of one provider.
     * @param provider Oh My Pi's provider id.
     * @param type {@code api_key} or {@code oauth}.
     * @param data The row's JSON object, or null when it is none.
     */
    record Row(long id, String provider, String type, @Nullable Map<?, ?> data) {}

    /**
     * Reads the database in a config directory.
     *
     * @param configDirectory Oh My Pi's config directory, {@code .omp/agent} in a home.
     * @return What it holds.
     */
    static OmpAuthDatabase read(final Path configDirectory) {
        final Path file = configDirectory.toAbsolutePath().normalize().resolve(FILE);
        if (Files.notExists(file)) {
            return new OmpAuthDatabase(file, State.ABSENT, List.of());
        }
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            return new OmpAuthDatabase(file, State.UNREADABLE, List.of());
        }
        final SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        config.setBusyTimeout(BUSY_TIMEOUT_MS);
        try (Connection connection = config.createConnection(url(file))) {
            if (!KNOWN_VERSIONS.contains(version(connection))) {
                return new OmpAuthDatabase(file, State.UNKNOWN_SCHEMA, List.of());
            }
            return new OmpAuthDatabase(file, State.READ, rows(connection));
        } catch (SQLException ex) {
            // What failed is not said further: a message may quote the file's content.
            return new OmpAuthDatabase(file, State.UNREADABLE, List.of());
        }
    }

    /**
     * Returns the JDBC URL of a database file, as a {@code file:} URI: sqlite-jdbc reads what
     * follows a {@code ?} in a plain path as connection settings, so a directory named with one
     * would change how the file is opened, and cut its path. In a URI every such character is
     * percent-encoded.
     *
     * @param file The database.
     * @return Its URL.
     */
    static String url(final Path file) {
        return "jdbc:sqlite:" + file.toUri().toASCIIString();
    }

    private static int version(final Connection connection) throws SQLException {
        try (PreparedStatement query =
                        connection.prepareStatement(
                                "SELECT version FROM auth_schema_version WHERE id = 1");
                ResultSet result = query.executeQuery()) {
            return result.next() ? result.getInt(1) : -1;
        }
    }

    private static List<Row> rows(final Connection connection) throws SQLException {
        final List<Row> rows = new ArrayList<>();
        try (PreparedStatement query =
                        connection.prepareStatement(
                                "SELECT id, provider, credential_type, data FROM auth_credentials"
                                        + " WHERE disabled_cause IS NULL ORDER BY id ASC");
                ResultSet result = query.executeQuery()) {
            while (result.next()) {
                rows.add(
                        new Row(
                                result.getLong(1),
                                String.valueOf(result.getString(2)),
                                String.valueOf(result.getString(3)),
                                object(result.getString(4))));
            }
        }
        return List.copyOf(rows);
    }

    private static @Nullable Map<?, ?> object(final @Nullable String data) {
        if (data == null) {
            return null;
        }
        try {
            return Json.parse(data) instanceof Map<?, ?> map ? map : null;
        } catch (JsonException ex) {
            return null;
        }
    }
}

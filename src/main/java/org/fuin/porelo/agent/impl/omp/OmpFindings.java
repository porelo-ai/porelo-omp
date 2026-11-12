package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.fuin.porelo.agent.api.Finder;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.Kind;
import org.fuin.porelo.agent.api.Finding.SignedIn;
import org.fuin.porelo.agent.api.Finding.What;
import org.fuin.porelo.agent.impl.omp.OmpAuthDatabase.Row;
import org.jspecify.annotations.Nullable;

/**
 * What Oh My Pi finds of itself on the host: its database, and the sign-ins it holds, by provider
 * and kind, never a value.
 *
 * <p>Oh My Pi creates its database whether anyone signed in or not, so the file says Oh My Pi is
 * set up. Its sign-ins are read by {@link OmpAuthDatabase}: one finding per provider with a live
 * row, in the order Porelo names providers, then any other. A database that cannot be read, or one
 * of a schema the reader does not know, cannot say, which is never "not signed in".
 *
 * <p>Oh My Pi's provider ids are the names Porelo gives the same providers. What a row's {@code
 * data} holds per provider is read in 18.18.1: an {@code api_key} row holds {@code key};
 * OpenRouter's sign-in ends in a key, kept as an {@code api_key} row too; GitHub Copilot's {@code
 * oauth} row keeps the GitHub token as {@code refresh}, with {@code enterpriseUrl} for an
 * enterprise domain, which Porelo does not reach; a row a remote broker holds carries {@code
 * "__remote__"} for its refresh token, which renews nothing here.
 */
final class OmpFindings implements Finder {

    private static final String COPILOT = "github-copilot";
    private static final String OPENROUTER = "openrouter";
    private static final String ANTHROPIC = "anthropic";
    private static final String API_KEY = "api_key";
    private static final String OAUTH = "oauth";

    /**
     * The refresh token of a row a remote broker holds, 18.18.1's {@code REMOTE_REFRESH_SENTINEL}.
     */
    static final String REMOTE_REFRESH = "__remote__";

    /** Providers Porelo declares under Oh My Pi's same id, in the order Porelo names them. */
    private static final List<String> PROVIDERS =
            List.of(ANTHROPIC, "openai", "google", COPILOT, OPENROUTER);

    /** The ones of them whose sign-in, not only key, Porelo can take. */
    private static final Set<String> SIGN_IN_PROVIDERS = Set.of(ANTHROPIC, COPILOT, OPENROUTER);

    @Override
    public List<Finding> find(final Path configDirectory) {

        final Path directory = configDirectory.toAbsolutePath().normalize();
        final Path database = directory.resolve(OmpAuthDatabase.FILE);
        if (!Files.isRegularFile(database)) {
            // Neither said about a path that cannot be looked at, so only that case is left open.
            if (Files.exists(database) || Files.notExists(database)) {
                return List.of();
            }
            return List.of(cannotSay(database));
        }
        final List<Finding> findings = new ArrayList<>();
        findings.add(new Finding(What.CONFIGURATION, database.toString(), null, null, null, null));
        final OmpAuthDatabase auth = OmpAuthDatabase.read(directory);
        if (auth.state() != OmpAuthDatabase.State.READ) {
            findings.add(cannotSay(database));
            return List.copyOf(findings);
        }
        if (auth.rows().isEmpty()) {
            findings.add(
                    new Finding(
                            What.SIGN_IN,
                            database.toString(),
                            SignedIn.NOT_SIGNED_IN,
                            null,
                            null,
                            null));
            return List.copyOf(findings);
        }
        firstByProvider(auth.rows()).values().stream()
                .sorted(Comparator.comparingInt(row -> rank(row.provider())))
                .map(row -> finding(database, row))
                .forEach(findings::add);
        return List.copyOf(findings);
    }

    /**
     * Returns the row Oh My Pi uses first of each provider: its rows come by id, and so the first.
     *
     * @param rows The live rows, by id.
     * @return One row per provider, in the order they first appear.
     */
    static Map<String, Row> firstByProvider(final List<Row> rows) {
        final Map<String, Row> first = new LinkedHashMap<>();
        rows.forEach(row -> first.putIfAbsent(row.provider(), row));
        return first;
    }

    private static int rank(final String provider) {
        final int index = PROVIDERS.indexOf(provider);
        return index < 0 ? PROVIDERS.size() : index;
    }

    /**
     * Returns what a row says: signed in, by which provider and kind, and whether Porelo can take
     * it.
     *
     * @param database The database's path.
     * @param row The provider's first live row.
     * @return Its finding.
     */
    static Finding finding(final Path database, final Row row) {
        final Map<?, ?> data = row.data();
        final String provider = row.provider();
        if (data == null) {
            return cannotSay(database);
        }
        if (API_KEY.equals(row.type())) {
            if (!isSet(data.get("key"))) {
                return cannotSay(database);
            }
            final boolean known = PROVIDERS.contains(provider);
            return signedIn(database, known, known ? provider : null, Kind.API_KEY);
        }
        if (!OAUTH.equals(row.type()) || !isSet(data.get("access"))) {
            return cannotSay(database);
        }
        if (!SIGN_IN_PROVIDERS.contains(provider)) {
            return signedIn(database, false, null, null);
        }
        if (provider.equals(OPENROUTER)) {
            return signedIn(database, true, OPENROUTER, Kind.API_KEY);
        }
        final Object refresh = data.get("refresh");
        if (provider.equals(COPILOT)) {
            if (isSet(data.get("enterpriseUrl"))) {
                return signedIn(database, false, null, Kind.SUBSCRIPTION);
            }
            return signedIn(
                    database,
                    isSet(refresh) && !REMOTE_REFRESH.equals(refresh),
                    COPILOT,
                    Kind.SUBSCRIPTION);
        }
        return signedIn(database, !REMOTE_REFRESH.equals(refresh), provider, Kind.SUBSCRIPTION);
    }

    private static Finding signedIn(
            final Path database,
            final boolean importable,
            final @Nullable String provider,
            final @Nullable Kind kind) {
        return new Finding(
                What.SIGN_IN, database.toString(), SignedIn.SIGNED_IN, importable, provider, kind);
    }

    private static Finding cannotSay(final Path database) {
        return new Finding(
                What.SIGN_IN, database.toString(), SignedIn.CANNOT_SAY, null, null, null);
    }

    private static boolean isSet(final @Nullable Object value) {
        return value instanceof String text && !text.isBlank();
    }
}

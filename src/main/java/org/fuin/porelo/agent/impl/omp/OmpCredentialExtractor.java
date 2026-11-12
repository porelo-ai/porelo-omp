package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.fuin.porelo.agent.api.AgentException;
import org.fuin.porelo.agent.api.Credential;
import org.fuin.porelo.agent.api.CredentialExtractor;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.Kind;
import org.fuin.porelo.agent.impl.omp.OmpAuthDatabase.Row;
import org.jspecify.annotations.Nullable;

/**
 * Takes a sign-in out of Oh My Pi's database, one {@link OmpFindings} says an import can take.
 *
 * <p>Of several live rows of one provider, the first by id, the one Oh My Pi uses first. What each
 * provider's row holds is read in 18.18.1: an API key is {@code key}; Copilot's GitHub token is
 * {@code refresh}, the token Porelo keeps for Copilot; Anthropic's sign-in renews at its token URL
 * with Claude Code's public client, both in {@code
 * packages/catalog/src/compat/rules/auth/anthropic.kdl}.
 */
class OmpCredentialExtractor implements CredentialExtractor {

    private static final String API_KEY = "api-key";
    private static final String OAUTH = "oauth";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String ANTHROPIC = "anthropic";
    private static final String COPILOT = "github-copilot";

    /** Where 18.18.1 renews an Anthropic sign-in. */
    private static final String ANTHROPIC_TOKEN_URL = "https://api.anthropic.com/v1/oauth/token";

    /** The client 18.18.1 signs in to Anthropic as, kept there in base64. */
    private static final String ANTHROPIC_CLIENT_ID = "9d1c250a-e61b-44d9-88ed-5944d1962f5e";

    @Override
    public Optional<Credential> extract(final Path configDirectory) {
        return extract(configDirectory, null);
    }

    @Override
    public Optional<Credential> extract(
            final Path configDirectory, final @Nullable String provider) {

        final Path directory = configDirectory.toAbsolutePath().normalize();
        final OmpAuthDatabase auth = OmpAuthDatabase.read(directory);
        if (auth.state() == OmpAuthDatabase.State.ABSENT) {
            return Optional.empty();
        }
        if (auth.state() != OmpAuthDatabase.State.READ) {
            throw new AgentException(
                    auth.path() + " cannot be read, or is of a schema this adapter does not know");
        }
        final Map<String, Row> rows = OmpFindings.firstByProvider(auth.rows());
        return new OmpFindings()
                .find(directory).stream()
                        .filter(finding -> Boolean.TRUE.equals(finding.importable()))
                        .filter(finding -> provider == null || provider.equals(finding.provider()))
                        .findFirst()
                        .map(finding -> credential(finding, rows.get(finding.provider())));
    }

    private static Credential credential(final Finding finding, final @Nullable Row row) {
        final String provider = String.valueOf(finding.provider());
        if (row == null || row.data() == null) {
            // The finding came from this row; read twice, the file changed between.
            throw new AgentException("Oh My Pi's sign-in for " + provider + " changed while read");
        }
        final Map<?, ?> data = row.data();
        if ("api_key".equals(row.type())) {
            return new Credential(API_KEY, String.valueOf(data.get("key")), Map.of(), provider);
        }
        if (provider.equals(COPILOT)) {
            return new Credential(
                    ACCESS_TOKEN, String.valueOf(data.get("refresh")), Map.of(), provider);
        }
        final String access = String.valueOf(data.get("access"));
        if (finding.kind() == Kind.API_KEY) {
            return new Credential(API_KEY, access, Map.of(), provider);
        }
        return new Credential(OAUTH, access, renewal(provider, data), provider);
    }

    private static Map<String, String> renewal(final String provider, final Map<?, ?> data) {
        final Map<String, String> attributes = new LinkedHashMap<>();
        if (provider.equals(ANTHROPIC)
                && data.get("refresh") instanceof String refresh
                && !refresh.isBlank()) {
            attributes.put(Credential.REFRESH_TOKEN, refresh);
            attributes.put(Credential.TOKEN_URL, ANTHROPIC_TOKEN_URL);
            attributes.put(Credential.CLIENT_ID, ANTHROPIC_CLIENT_ID);
        }
        if (data.get("expires") instanceof Number expires) {
            attributes.put(Credential.EXPIRES_AT, String.valueOf(expires.longValue()));
        }
        return attributes;
    }
}

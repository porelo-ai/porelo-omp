package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.Kind;
import org.fuin.porelo.agent.api.Finding.SignedIn;
import org.fuin.porelo.agent.api.Finding.What;
import org.fuin.porelo.agent.impl.omp.OmpAuthDatabase.Row;
import org.jspecify.annotations.Nullable;

/**
 * What a sign-in Oh My Pi keeps in its database says of itself beside its finding, for a person who
 * chooses which one to carry to a host: the account, when it expires and when it last changed.
 * Never its value; the value goes to Porelo through {@link OmpCredentialExtractor} alone.
 *
 * <p>The row described is the one the extractor takes, the provider's first live row by id. Read in
 * 18.18.1: an {@code oauth} row's {@code data} names its account in {@code email} and its access
 * token's end in {@code expires}, epoch milliseconds; an {@code api_key} row holds neither.
 * Copilot's {@code expires} is that of its short-lived token, not of the GitHub token carried,
 * which does not expire, and an OpenRouter sign-in is carried as a key; neither says an expiry. The
 * last change is the row's {@code updated_at}, which Oh My Pi sets at each write, a renewal too.
 *
 * @param finding The finding these facts belong to.
 * @param account The account the sign-in is for, or {@code null} where the row names none.
 * @param expires When the access token carried expires, or {@code null} where none is held.
 * @param changed When Oh My Pi last wrote the row, or {@code null} where it holds no time.
 */
record OmpSignInFacts(
        Finding finding,
        @Nullable String account,
        @Nullable Instant expires,
        @Nullable Instant changed) {

    /** Longest account name kept, in characters; a longer one is not taken as one. */
    static final int LONGEST_ACCOUNT = 320;

    private static final String OAUTH = "oauth";
    private static final String COPILOT = "github-copilot";

    /**
     * Returns the facts of each finding that is a sign-in an import can take, in the findings'
     * order. A sign-in it cannot take, such as one a remote broker holds, has none.
     *
     * @param findings What {@link OmpFindings} found in the directory.
     * @param configDirectory Oh My Pi's config directory.
     * @return The facts, one per sign-in that can be carried.
     */
    static List<OmpSignInFacts> of(final List<Finding> findings, final Path configDirectory) {
        final OmpAuthDatabase auth = OmpAuthDatabase.read(configDirectory);
        if (auth.state() != OmpAuthDatabase.State.READ) {
            return List.of();
        }
        final Map<String, Row> rows = OmpFindings.firstByProvider(auth.rows());
        return findings.stream()
                .filter(finding -> finding.what() == What.SIGN_IN)
                .filter(finding -> finding.signedIn() == SignedIn.SIGNED_IN)
                .filter(finding -> Boolean.TRUE.equals(finding.importable()))
                .<OmpSignInFacts>mapMulti(
                        (finding, out) -> {
                            final @Nullable String provider = finding.provider();
                            final @Nullable Row row = provider == null ? null : rows.get(provider);
                            // A row gone since the findings were read is not described.
                            if (row != null && row.data() != null) {
                                out.accept(facts(finding, row, row.data()));
                            }
                        })
                .toList();
    }

    private static OmpSignInFacts facts(
            final Finding finding, final Row row, final Map<?, ?> data) {
        if (!OAUTH.equals(row.type())) {
            return new OmpSignInFacts(finding, null, null, row.updated());
        }
        final boolean expiresCarried =
                finding.kind() == Kind.SUBSCRIPTION && !COPILOT.equals(finding.provider());
        return new OmpSignInFacts(
                finding,
                account(data.get("email")),
                expiresCarried ? expires(data.get("expires")) : null,
                row.updated());
    }

    private static @Nullable String account(final @Nullable Object email) {
        if (email instanceof String address
                && !address.isBlank()
                && address.length() <= LONGEST_ACCOUNT
                && address.codePoints().noneMatch(Character::isISOControl)) {
            return address;
        }
        return null;
    }

    private static @Nullable Instant expires(final @Nullable Object expires) {
        if (expires instanceof Number millis && millis.longValue() > 0) {
            return Instant.ofEpochMilli(millis.longValue());
        }
        return null;
    }
}

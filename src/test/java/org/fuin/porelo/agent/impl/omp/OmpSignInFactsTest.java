package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.fuin.porelo.agent.api.Finding.Kind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The rows' shape is what Oh My Pi 18.18.1 writes, read in its credential store. */
class OmpSignInFactsTest {

    private static final String SECRET = "fixture-secret-value";
    private static final String ACCOUNT = "person@example.org";
    private static final long EXPIRES = 1_800_000_000_000L;
    private static final Instant CHANGED = Instant.parse("2026-11-01T10:00:00Z");

    @TempDir Path home;

    private AuthDatabaseFixture database() throws Exception {
        return AuthDatabaseFixture.create(home.resolve("agent.db"), 8);
    }

    private List<OmpSignInFacts> facts() {
        return OmpSignInFacts.of(new OmpFindings().find(home), home);
    }

    private static String anthropic(final String refresh, final long expires, final String email) {
        return "{\"access\":\""
                + SECRET
                + "\",\"refresh\":\""
                + refresh
                + "\",\"expires\":"
                + expires
                + ",\"email\":\""
                + email
                + "\"}";
    }

    @Test
    void anAnthropicSignInSaysItsAccountExpiryAndLastChange() throws Exception {

        // Prevents the person choosing a sign-in blind: which account, how fresh, how long it
        // holds.
        try (AuthDatabaseFixture db = database()) {
            db.add("anthropic", "oauth", anthropic(SECRET, EXPIRES, ACCOUNT))
                    .updatedAt("anthropic", CHANGED.getEpochSecond());

            assertThat(facts())
                    .singleElement()
                    .satisfies(
                            facts -> {
                                assertThat(facts.finding().provider()).isEqualTo("anthropic");
                                assertThat(facts.finding().kind()).isEqualTo(Kind.SUBSCRIPTION);
                                assertThat(facts.finding().importable()).as("carriable").isTrue();
                                assertThat(facts.account())
                                        .as("the row's email")
                                        .isEqualTo(ACCOUNT);
                                assertThat(facts.expires())
                                        .as("expires, in epoch milliseconds")
                                        .isEqualTo(Instant.ofEpochMilli(EXPIRES));
                                assertThat(facts.changed())
                                        .as("the row's updated_at, in epoch seconds")
                                        .isEqualTo(CHANGED);
                            });
        }
    }

    @Test
    void anApiKeySaysNoAccountAndNoExpiry() throws Exception {

        // Prevents a key from claiming an account or an expiry its row does not hold.
        try (AuthDatabaseFixture db = database()) {
            db.add("openai", "api_key", "{\"key\":\"" + SECRET + "\"}")
                    .updatedAt("openai", CHANGED.getEpochSecond());

            assertThat(facts())
                    .singleElement()
                    .satisfies(
                            facts -> {
                                assertThat(facts.finding().kind()).isEqualTo(Kind.API_KEY);
                                assertThat(facts.account()).as("no account").isNull();
                                assertThat(facts.expires()).as("no expiry").isNull();
                                assertThat(facts.changed()).as("last change").isEqualTo(CHANGED);
                            });
        }
    }

    @Test
    void copilotSaysNoExpiryOfItsShortLivedToken() throws Exception {

        // Prevents the GitHub token carried, which does not expire, reading as ending in minutes.
        try (AuthDatabaseFixture db = database()) {
            db.add(
                    "github-copilot",
                    "oauth",
                    "{\"access\":\""
                            + SECRET
                            + "\",\"refresh\":\""
                            + SECRET
                            + "\",\"expires\":"
                            + EXPIRES
                            + "}");

            assertThat(facts())
                    .singleElement()
                    .extracting(OmpSignInFacts::expires)
                    .as("expiry of Copilot's sign-in")
                    .isNull();
        }
    }

    @Test
    void aSignInARemoteBrokerHoldsHasNoFacts() throws Exception {

        // Prevents a sign-in an import cannot take from being offered with facts to carry.
        try (AuthDatabaseFixture db = database()) {
            db.add("anthropic", "oauth", anthropic("__remote__", EXPIRES, ACCOUNT));

            assertThat(facts()).as("facts of a brokered sign-in").isEmpty();
        }
    }

    @Test
    void aSignInWithoutAProviderHasNoFacts() throws Exception {

        // Prevents a sign-in Porelo names no provider for from being offered to carry.
        try (AuthDatabaseFixture db = database()) {
            db.add("some-other-provider", "api_key", "{\"key\":\"" + SECRET + "\"}")
                    .add("cursor", "oauth", anthropic(SECRET, EXPIRES, ACCOUNT));

            assertThat(facts()).as("facts of providers Porelo does not name").isEmpty();
        }
    }

    @Test
    void theRowDescribedIsTheOneTheExtractorTakes() throws Exception {

        // Prevents the facts of one account shown beside the value of another being carried.
        try (AuthDatabaseFixture db = database()) {
            db.add("anthropic", "oauth", anthropic(SECRET, EXPIRES, ACCOUNT))
                    .add("anthropic", "oauth", anthropic(SECRET, 1L, "other@example.org"));

            assertThat(facts())
                    .singleElement()
                    .extracting(OmpSignInFacts::account)
                    .as("account of the first row by id")
                    .isEqualTo(ACCOUNT);
        }
    }

    @Test
    void anAccountWithAControlCharacterIsNoAccount() throws Exception {

        // Prevents something that is not an address from reaching the person's dialog as one.
        try (AuthDatabaseFixture db = database()) {
            db.add(
                    "anthropic",
                    "oauth",
                    anthropic(SECRET, EXPIRES, "person\\u001b[2J@example.org"));

            assertThat(facts())
                    .singleElement()
                    .extracting(OmpSignInFacts::account)
                    .as("account with an escape sequence")
                    .isNull();
        }
    }

    @Test
    void noExpiryHeldIsNoExpiry() throws Exception {

        // Prevents a zero expiry from reading as one in 1970.
        try (AuthDatabaseFixture db = database()) {
            db.add("anthropic", "oauth", anthropic(SECRET, 0, ACCOUNT));

            assertThat(facts())
                    .singleElement()
                    .extracting(OmpSignInFacts::expires)
                    .as("expiry of 0")
                    .isNull();
        }
    }

    @Test
    void noFactCarriesACredentialsValue() throws Exception {

        // Prevents a credential's value leaving the adapter in what is shown to the person.
        try (AuthDatabaseFixture db = database()) {
            db.add("anthropic", "oauth", anthropic(SECRET, EXPIRES, ACCOUNT))
                    .add("openai", "api_key", "{\"key\":\"" + SECRET + "\"}")
                    .add(
                            "github-copilot",
                            "oauth",
                            "{\"access\":\"" + SECRET + "\",\"refresh\":\"" + SECRET + "\"}")
                    .add("openrouter", "oauth", "{\"access\":\"" + SECRET + "\"}");

            final List<OmpSignInFacts> facts = facts();

            assertThat(facts).as("every route was read").hasSize(4);
            assertThat(facts.toString()).as("no value in any field").doesNotContain(SECRET);
        }
    }
}

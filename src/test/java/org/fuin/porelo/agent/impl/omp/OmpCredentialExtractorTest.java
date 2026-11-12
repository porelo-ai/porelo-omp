package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.fuin.porelo.agent.api.AgentException;
import org.fuin.porelo.agent.api.Credential;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.SignedIn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OmpCredentialExtractorTest {

    private static final String KEY = "sk-or-v1-test";

    @TempDir Path home;

    private final OmpCredentialExtractor extractor = new OmpCredentialExtractor();

    private AuthDatabaseFixture database(final int version) throws Exception {
        return AuthDatabaseFixture.create(home.resolve("agent.db"), version);
    }

    @Test
    void theProviderAskedForIsTakenAndNoOther() throws Exception {

        // Prevents an import for one provider taking the row of another.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("anthropic", "api_key", "{\"key\":\"sk-ant-test\"}")
                    .add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");

            final Credential credential = extractor.extract(home, "openrouter").orElseThrow();

            assertThat(credential.provider()).as("the provider it names").isEqualTo("openrouter");
            assertThat(credential.type()).as("its type").isEqualTo("api-key");
            assertThat(credential.secret()).as("its value").isEqualTo(KEY);
        }
    }

    @Test
    void withoutAProviderTheFirstInPorelosOrderAnswers() throws Exception {

        // Prevents the answer depending on which row Oh My Pi happened to write first.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}")
                    .add("anthropic", "api_key", "{\"key\":\"sk-ant-test\"}");

            assertThat(extractor.extract(home).orElseThrow().provider())
                    .as("the provider of the first finding")
                    .isEqualTo("anthropic");
        }
    }

    @Test
    void ofSeveralRowsTheFirstByIdIsTaken() throws Exception {

        // Prevents an import taking a row Oh My Pi itself would use only after another.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"first\"}")
                    .add("openrouter", "api_key", "{\"key\":\"second\"}");

            assertThat(extractor.extract(home, "openrouter").orElseThrow().secret())
                    .isEqualTo("first");
        }
    }

    @Test
    void aTombstoneIsSkipped() throws Exception {

        // Prevents a sign-in Oh My Pi gave up on, kept only as a tombstone, being imported.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"revoked\"}", "revoked")
                    .add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");

            assertThat(extractor.extract(home, "openrouter").orElseThrow().secret()).isEqualTo(KEY);
        }
    }

    @Test
    void anAnthropicSignInCarriesWhatRenewsIt() throws Exception {

        // Prevents an imported subscription ending at its first expiry.
        try (AuthDatabaseFixture db = database(8)) {
            db.add(
                    "anthropic",
                    "oauth",
                    "{\"access\":\"sk-ant-oat-test\",\"refresh\":\"sk-ant-ort-test\","
                            + "\"expires\":1767225600000,\"email\":\"a@example.org\"}");

            final Credential credential = extractor.extract(home, "anthropic").orElseThrow();

            assertThat(credential.type()).isEqualTo("oauth");
            assertThat(credential.secret()).isEqualTo("sk-ant-oat-test");
            assertThat(credential.attributes())
                    .containsExactlyInAnyOrderEntriesOf(
                            Map.of(
                                    Credential.REFRESH_TOKEN, "sk-ant-ort-test",
                                    Credential.TOKEN_URL,
                                            "https://api.anthropic.com/v1/oauth/token",
                                    Credential.CLIENT_ID, "9d1c250a-e61b-44d9-88ed-5944d1962f5e",
                                    Credential.EXPIRES_AT, "1767225600000"));
        }
    }

    @Test
    void copilotGivesItsGitHubToken() throws Exception {

        // Prevents Porelo keeping Copilot's short-lived token where it needs the GitHub one.
        try (AuthDatabaseFixture db = database(8)) {
            db.add(
                    "github-copilot",
                    "oauth",
                    "{\"access\":\"tid-short\",\"refresh\":\"gho_github\",\"expires\":1}");

            final Credential credential = extractor.extract(home, "github-copilot").orElseThrow();

            assertThat(credential.type()).isEqualTo("access-token");
            assertThat(credential.secret()).isEqualTo("gho_github");
        }
    }

    @Test
    void aSignInARemoteBrokerHoldsIsNotImportable() throws Exception {

        // Prevents an import whose refresh token is only the broker's placeholder.
        try (AuthDatabaseFixture db = database(8)) {
            db.add(
                    "anthropic",
                    "oauth",
                    "{\"access\":\"sk-ant-oat-test\",\"refresh\":\"__remote__\",\"expires\":1}");

            assertThat(extractor.extract(home, "anthropic")).isEmpty();
            assertThat(new OmpFindings().find(home))
                    .filteredOn(finding -> finding.what() == Finding.What.SIGN_IN)
                    .singleElement()
                    .satisfies(
                            finding -> {
                                assertThat(finding.signedIn()).isEqualTo(SignedIn.SIGNED_IN);
                                assertThat(finding.importable()).isFalse();
                            });
        }
    }

    @Test
    void anUnknownSchemaCannotSayAndRefusesAnImport() throws Exception {

        // Prevents a release that moved the table being read as if it had not.
        try (AuthDatabaseFixture db = database(9)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");

            assertThat(new OmpFindings().find(home))
                    .filteredOn(finding -> finding.what() == Finding.What.SIGN_IN)
                    .extracting(Finding::signedIn)
                    .containsExactly(SignedIn.CANNOT_SAY);
            assertThatThrownBy(() -> extractor.extract(home, "openrouter"))
                    .isInstanceOf(AgentException.class)
                    .hasMessageNotContaining(KEY);
        }
    }

    @Test
    void brokenDataCannotSayAndIsNotImported() throws Exception {

        // Prevents a row Oh My Pi itself could not use being taken, or its text being shown.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"");

            assertThat(new OmpFindings().find(home))
                    .filteredOn(finding -> finding.what() == Finding.What.SIGN_IN)
                    .extracting(Finding::signedIn)
                    .containsExactly(SignedIn.CANNOT_SAY);
            assertThat(extractor.extract(home, "openrouter")).isEmpty();
        }
    }

    @Test
    void aFileThatIsNotSqliteCannotSayAndRefusesAnImport() throws Exception {

        // Prevents a damaged file reading as "not signed in", and its content reaching a message.
        Files.writeString(home.resolve("agent.db"), "not a database " + KEY);

        assertThat(new OmpFindings().find(home))
                .filteredOn(finding -> finding.what() == Finding.What.SIGN_IN)
                .extracting(Finding::signedIn)
                .containsExactly(SignedIn.CANNOT_SAY);
        assertThatThrownBy(() -> extractor.extract(home))
                .isInstanceOf(AgentException.class)
                .hasMessageNotContaining(KEY);
    }

    @Test
    void aDirectoryWhoseNameHoldsUrlCharactersIsReadAsItIs() throws Exception {

        // Prevents a path being cut or read as connection settings where the JDBC URL splits.
        final Path odd = Files.createDirectories(home.resolve("a?journal_mode=delete&x=#1 %41"));
        try (AuthDatabaseFixture db = AuthDatabaseFixture.create(odd.resolve("agent.db"), 8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");

            assertThat(extractor.extract(odd, "openrouter").orElseThrow().secret()).isEqualTo(KEY);
        }
    }

    @Test
    void noDatabaseFindsNothing() {

        // Prevents an account where Oh My Pi never ran failing the import.
        assertThat(extractor.extract(home, "openrouter")).isEmpty();
    }

    @Test
    void ohMyPiWritesOnWhileAndAfterTheAdapterReads() throws Exception {

        // Prevents a read holding a lock that Oh My Pi, running beside it, would wait on.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");

            for (int i = 0; i < 20; i++) {
                assertThat(new OmpFindings().find(home)).isNotEmpty();
                db.add("openai", "api_key", "{\"key\":\"sk-" + i + "\"}");
            }

            assertThat(extractor.extract(home, "openai").orElseThrow().secret())
                    .as("the first of the rows written between the reads")
                    .isEqualTo("sk-0");
        }
    }

    @Test
    void aReadLeavesNoFileBesideTheDatabase() throws Exception {

        // Prevents a read creating files in a directory that is Oh My Pi's; its own -wal and -shm
        // are there while it runs, and nothing else may come.
        try (AuthDatabaseFixture db = database(8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");
            final var before = Files.list(home).map(Path::getFileName).toList();

            extractor.extract(home, "openrouter").orElseThrow();

            assertThat(Files.list(home).map(Path::getFileName).toList())
                    .containsExactlyInAnyOrderElementsOf(before);
        }
    }
}

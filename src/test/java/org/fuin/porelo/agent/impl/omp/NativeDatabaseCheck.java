package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.fuin.porelo.agent.api.AgentProtocol;
import org.fuin.porelo.wire.varlink.VarlinkClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Whether the native binary reads Oh My Pi's database.
 *
 * <p>The reader loads SQLite as a JNI library at run time, which {@link NativeLinkageCheck} cannot
 * see in the binary's {@code NEEDED}; only the binary itself shows that it loads and reads. So this
 * starts it as Porelo does, on a socket, and asks it what the unit tests ask the classes: what it
 * finds, and the credential an import takes, from a database written as Oh My Pi 18.18.1 writes
 * one, its connection still open.
 *
 * <p>Not a unit test: the binary exists only after native-image. The {@code dist} profile runs this
 * class through failsafe, by name, beside {@link NativeLinkageCheck}.
 */
class NativeDatabaseCheck {

    private static final String KEY = "sk-or-v1-native-check";

    @TempDir Path dir;

    @Test
    void theBinaryReadsTheSignInsInOhMyPisDatabase() throws Exception {

        final Path binary = Path.of(System.getProperty("porelo.native.binary", ""));
        assertThat(Files.isRegularFile(binary)).as("%s - built by -Pnative", binary).isTrue();
        final Path home = Files.createDirectory(dir.resolve("agent"));
        final Path socket = dir.resolve("agent.sock");

        try (AuthDatabaseFixture db = AuthDatabaseFixture.create(home.resolve("agent.db"), 8)) {
            db.add("openrouter", "api_key", "{\"key\":\"" + KEY + "\"}");
            final Process agent =
                    new ProcessBuilder(binary.toString(), "serve", socket.toString())
                            .redirectErrorStream(true)
                            .start();
            try {
                awaitReady(agent);
                try (VarlinkClient client = new VarlinkClient(socket)) {

                    final Map<String, Object> found =
                            client.call(
                                    AgentProtocol.INTERFACE + ".Find",
                                    Map.of("configDirectory", home.toString()));
                    assertThat(found.toString())
                            .as("what the binary finds")
                            .contains("openrouter")
                            .doesNotContain(KEY);

                    final Map<String, Object> credential =
                            client.call(
                                    AgentProtocol.INTERFACE + ".ExtractCredential",
                                    Map.of(
                                            "configDirectory",
                                            home.toString(),
                                            "provider",
                                            "openrouter"));
                    assertThat(credential)
                            .as("the credential the binary takes")
                            .containsEntry("found", Boolean.TRUE)
                            .containsEntry("type", "api-key")
                            .containsEntry("secret", KEY)
                            .containsEntry("provider", "openrouter");
                }
            } finally {
                agent.destroy();
                if (!agent.waitFor(10, TimeUnit.SECONDS)) {
                    agent.destroyForcibly();
                }
            }
        }
        assertThat(listed(home))
                .as("files beside the database after the read")
                .doesNotContain("agent.db-journal");
    }

    private static void awaitReady(final Process agent) throws IOException {
        final BufferedReader out =
                new BufferedReader(
                        new InputStreamReader(agent.getInputStream(), StandardCharsets.UTF_8));
        final String line = out.readLine();
        assertThat(line).as("the binary's first line").startsWith("ready ");
    }

    private static List<String> listed(final Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString()).toList();
        }
    }
}

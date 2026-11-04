package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.SignedIn;
import org.fuin.porelo.agent.api.Finding.What;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OmpFindingsTest {

    @TempDir Path home;

    private List<Finding> find(final Path directory) throws IOException {
        return new OmpFindings().find(directory);
    }

    private static Finding cannotSay(final Path database) {
        return new Finding(
                What.SIGN_IN, database.toString(), SignedIn.CANNOT_SAY, null, null, null);
    }

    @Test
    void aDatabaseSaysOhMyPiIsSetUpAndItsSignInCannotBeTold() throws IOException {

        // Prevents an account that ran Oh My Pi being told it is not signed in, when its sign-in
        // may well be in the database no one here opens.
        final Path database = Files.createFile(home.resolve("agent.db"));

        assertThat(find(home))
                .as("set up from the database, and its sign-in left open")
                .containsExactly(
                        new Finding(
                                What.CONFIGURATION, database.toString(), null, null, null, null),
                        cannotSay(database));
    }

    @Test
    void noDatabaseSaysNothingIsThere() throws IOException {

        // Prevents a directory another tool made, or a config file alone, reading as Oh My Pi set
        // up.
        Files.writeString(home.resolve("config.yml"), "theme: dark\n");

        assertThat(find(home)).as("findings without an agent.db").isEmpty();
    }

    @Test
    void anAbsentDirectorySaysNothingIsThere() throws IOException {

        // Prevents an account where Oh My Pi never ran failing the question.
        assertThat(find(home.resolve("never-ran"))).as("findings of no directory").isEmpty();
    }

    @Test
    void aDirectoryNamedLikeTheDatabaseIsNotTheDatabase() throws IOException {

        // Prevents something that cannot be Oh My Pi's database reading as Oh My Pi set up.
        Files.createDirectory(home.resolve("agent.db"));

        assertThat(find(home)).as("findings with a directory for agent.db").isEmpty();
    }

    @Test
    void aDirectoryThatCannotBeReadCannotSay() throws IOException {

        // Prevents an account whose directory is closed reading as one where Oh My Pi never ran,
        // which would pass over a sign-in that may be there.
        final Path closed = Files.createDirectory(home.resolve("closed"));
        Files.createFile(closed.resolve("agent.db"));
        Files.setPosixFilePermissions(closed, Set.of());
        try {
            // The forge's runner is root, which reads through any mode.
            assumeFalse(Files.isReadable(closed), "this account reads through a mode");
            assertThat(find(closed))
                    .as("findings of a directory that cannot be looked into")
                    .containsExactly(cannotSay(closed.resolve("agent.db")));
        } finally {
            Files.setPosixFilePermissions(
                    closed,
                    Set.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE));
        }
    }

    @Test
    void aRelativeDirectoryIsReportedWithAnAbsolutePath() throws IOException {

        // Prevents Porelo refusing the finding, which it does for a path that is not absolute.
        final Path database = Files.createFile(home.resolve("agent.db"));
        final Path relative = Path.of("").toAbsolutePath().relativize(home);

        assertThat(find(relative))
                .as("paths of the findings")
                .extracting(Finding::path)
                .containsExactly(database.toString(), database.toString());
    }
}

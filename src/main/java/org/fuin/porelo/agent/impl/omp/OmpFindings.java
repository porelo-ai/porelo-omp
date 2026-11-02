package org.fuin.porelo.agent.impl.omp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.fuin.porelo.agent.api.Finder;
import org.fuin.porelo.agent.api.Finding;
import org.fuin.porelo.agent.api.Finding.SignedIn;
import org.fuin.porelo.agent.api.Finding.What;

/**
 * What Oh My Pi finds of itself on the host: whether its database is there, and never what it
 * holds.
 *
 * <p>Oh My Pi keeps its sign-ins in that SQLite database and creates it whether anyone signed in or
 * not, so the database says Oh My Pi is set up and leaves the sign-in open. The file is only looked
 * at and never opened, so a database Oh My Pi holds open is not touched.
 */
final class OmpFindings implements Finder {

    private static final String DATABASE = "agent.db";

    @Override
    public List<Finding> find(final Path configDirectory) {

        final Path database = configDirectory.toAbsolutePath().normalize().resolve(DATABASE);
        if (Files.isRegularFile(database)) {
            return List.of(
                    new Finding(What.CONFIGURATION, database.toString(), null, null, null, null),
                    signInCannotSay(database));
        }
        // Neither said about a path that cannot be looked at, so only that case is left open.
        if (Files.exists(database) || Files.notExists(database)) {
            return List.of();
        }
        return List.of(signInCannotSay(database));
    }

    private static Finding signInCannotSay(final Path database) {
        return new Finding(
                What.SIGN_IN, database.toString(), SignedIn.CANNOT_SAY, null, null, null);
    }
}

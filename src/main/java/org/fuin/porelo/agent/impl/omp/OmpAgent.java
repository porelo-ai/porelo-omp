package org.fuin.porelo.agent.impl.omp;

import java.util.List;
import org.fuin.porelo.agent.api.AgentEnd;
import org.fuin.porelo.agent.api.AgentMain;
import org.fuin.porelo.agent.api.ContainerSetup;
import org.fuin.porelo.agent.api.CredentialExtractor;
import org.fuin.porelo.agent.api.Finder;
import org.fuin.porelo.agent.api.LogFormatter;
import org.fuin.porelo.agent.api.ToolEvent;
import org.fuin.porelo.agent.api.YamlAgent;
import org.jspecify.annotations.Nullable;

/**
 * Oh My Pi.
 *
 * <p>The endpoint and the token go into the models file omp reads at start-up, because it has no
 * variable for either; its JSON events provide readable lines and tool calls; and what it finds of
 * itself on the host, and the sign-in an import takes, are read from its database. Everything else
 * is {@code omp.yaml}.
 */
public final class OmpAgent extends YamlAgent {

    /** Constructor. */
    public OmpAgent() {
        super("omp");
    }

    /**
     * Says what Oh My Pi finds of itself on the host; overrides the agent API's default once Porelo
     * publishes it.
     *
     * @return Finder looking for Oh My Pi's database.
     */
    @Override
    public Finder finder() {
        return new OmpFindings();
    }

    /**
     * Takes a sign-in out of Oh My Pi's database, for {@code porelo vault import}.
     *
     * @return Extractor reading the database read-only.
     */
    @Override
    public CredentialExtractor credentialExtractor() {
        return new OmpCredentialExtractor();
    }

    @Override
    public ContainerSetup containerSetup() {
        return new OmpContainerSetup();
    }

    @Override
    public LogFormatter logFormatter() {
        return new OmpJsonFormatter();
    }

    @Override
    public @Nullable AgentEnd ended(String line) {
        return OmpJsonFormatter.ended(line);
    }

    /**
     * Reads tool calls from Oh My Pi's unattended JSON output.
     *
     * @param line One raw output line.
     * @return Tool events for Porelo to join, or none for an unrelated line.
     */
    @Override
    public List<ToolEvent> toolEvents(String line) {
        return OmpToolEvents.read(line);
    }

    /**
     * Entry point of the {@code porelo-agent-omp} binary.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        AgentMain.run(new OmpAgent(), args);
    }
}

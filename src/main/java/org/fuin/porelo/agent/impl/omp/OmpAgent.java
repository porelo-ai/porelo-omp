package org.fuin.porelo.agent.impl.omp;

import org.fuin.porelo.agent.api.AgentEnd;
import org.fuin.porelo.agent.api.AgentMain;
import org.fuin.porelo.agent.api.ContainerSetup;
import org.fuin.porelo.agent.api.Finder;
import org.fuin.porelo.agent.api.LogFormatter;
import org.fuin.porelo.agent.api.YamlAgent;
import org.jspecify.annotations.Nullable;

/**
 * Oh My Pi.
 *
 * <p>Three overrides: the endpoint and the token go into the models file omp reads at start-up,
 * because it has no variable for either; its JSON events are rendered as readable lines; and what
 * it finds of itself on the host depends on a file being there. Everything else is {@code
 * omp.yaml}.
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
     * Entry point of the {@code porelo-agent-omp} binary.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        AgentMain.run(new OmpAgent(), args);
    }
}

package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fuin.porelo.agent.api.Agent;
import org.fuin.porelo.agent.api.ToolEvent;
import org.fuin.porelo.agent.api.ToolEvent.Kind;
import org.fuin.porelo.agent.api.ToolEvent.Phase;
import org.junit.jupiter.api.Test;

/** Guards tool accounting and exclusion of tool output using recorded JSON events. */
class OmpToolEventsTest {

    private static final String SHELL = "call_d3nclusrsDSF8vhMsNUW7eyi|fc_tmp_017satbdrjx1n";
    private static final String TASK = "call_Ha09X0p7bUuxqKOt0GAknZWY|fc_tmp_l5qg5ameork";
    private static final String COMMAND =
            "./mvnw -B -s settings.xml -pl filter/app -am test > /tmp/build.log 2>&1 & echo $!";

    private final Agent agent = new OmpAgent();

    @Test
    void preservesTheWholeShellCommandWithoutGuessingBackgroundFromAnAmpersand()
            throws IOException {
        assertThat(agent.toolEvents(fixture("omp", 0)))
                .as(
                        "the shell start keeps its id and command without recording its arguments or intent")
                .containsExactly(
                        new ToolEvent(
                                0,
                                SHELL,
                                Phase.START,
                                "bash",
                                Kind.SHELL,
                                COMMAND,
                                false,
                                null,
                                null));
    }

    @Test
    void joinsTheBackgroundHandoffAndItsLaterCompletionByJobId() throws IOException {
        assertThat(agent.toolEvents(fixture("omp", 1)))
                .as("the call's early return names its job by a second start, then ends as usual")
                .containsExactly(
                        new ToolEvent(
                                0,
                                SHELL,
                                Phase.START,
                                "bash",
                                Kind.SHELL,
                                null,
                                true,
                                "bg_1",
                                null),
                        new ToolEvent(
                                0, SHELL, Phase.END, "bash", Kind.SHELL, null, false, null, null));
        assertThat(agent.toolEvents(fixture("omp", 2)))
                .as(
                        "the custom message ends the job with its measured duration, without output or timestamp")
                .containsExactly(
                        new ToolEvent(
                                0,
                                "bg_1",
                                Phase.END,
                                "bash",
                                Kind.SHELL,
                                null,
                                true,
                                "bg_1",
                                null,
                                null,
                                null,
                                17L));
    }

    @Test
    void recordsReadsAndEditsWithoutFileContentsOrResults() throws IOException {
        for (int index : new int[] {3, 4, 5, 6}) {
            final boolean read = index < 5;
            final String id =
                    read
                            ? "call_eNt4upbITLPoPf96BRJblB3P|fc_tmp_rrdd00vrq6d"
                            : "call_sY7xYAaGbCpN85gxqSg3JjaM|fc_tmp_1uhtmajd3re";
            assertThat(agent.toolEvents(fixture("omp", index)))
                    .as("read and edit event %s carries only accounting fields", index)
                    .containsExactly(
                            new ToolEvent(
                                    0,
                                    id,
                                    index % 2 == 1 ? Phase.START : Phase.END,
                                    read ? "read" : "edit",
                                    read ? Kind.READ : Kind.EDIT,
                                    null,
                                    false,
                                    null,
                                    null));
        }
    }

    @Test
    void recordsOtherToolsRatherThanLosingTheirCalls() throws IOException {
        assertThat(agent.toolEvents(fixture("omp", 7)))
                .as("todo is a tool call even though it has no dedicated kind")
                .containsExactly(
                        new ToolEvent(
                                0,
                                "call_PU04pPyou2xcn6v46cQl77hW|fc_tmp_bw90blwrg66",
                                Phase.START,
                                "todo",
                                Kind.OTHER,
                                null,
                                false,
                                null,
                                null));
    }

    @Test
    void keepsFailedCallsInTheAccountingWithoutTheirErrorOutput() throws IOException {
        final String failed = fixture("omp", 4).replace("\"isError\":false", "\"isError\":true");
        assertThat(agent.toolEvents(failed))
                .as("a failed call still closes its start and exposes none of its result")
                .containsExactly(
                        new ToolEvent(
                                0,
                                "call_eNt4upbITLPoPf96BRJblB3P|fc_tmp_rrdd00vrq6d",
                                Phase.END,
                                "read",
                                Kind.READ,
                                null,
                                false,
                                null,
                                null));
    }

    @Test
    void keepsTheNamedSubagentOpenUntilItsProgressCompletes() throws IOException {
        assertThat(agent.toolEvents(fixture("sub-omp", 0)))
                .as("the named task has its own correlation id and job id")
                .containsExactly(
                        new ToolEvent(
                                0,
                                TASK + "/ISSUE-7",
                                Phase.START,
                                "task",
                                Kind.SUB_AGENT,
                                null,
                                true,
                                "ISSUE-7",
                                null,
                                "ISSUE-7",
                                null,
                                null));
        for (int index : new int[] {1, 2}) {
            assertThat(agent.toolEvents(fixture("sub-omp", index)))
                    .as(
                            "spawn acknowledgement or running progress %s does not close the subagent",
                            index)
                    .isEmpty();
        }
        assertThat(agent.toolEvents(fixture("sub-omp", 3)))
                .as("completed progress ends the named subagent with its own duration")
                .containsExactly(
                        new ToolEvent(
                                0,
                                TASK + "/ISSUE-7",
                                Phase.END,
                                "task",
                                Kind.SUB_AGENT,
                                null,
                                true,
                                "ISSUE-7",
                                null,
                                null,
                                null,
                                4045L));
    }

    @Test
    void alsoClosesASubagentWhoseProgressFails() throws IOException {
        final String failed = fixture("sub-omp", 3).replace("\"completed\"", "\"failed\"");
        assertThat(agent.toolEvents(failed))
                .as("failed progress closes the same named subagent rather than leaving it running")
                .containsExactly(
                        new ToolEvent(
                                0,
                                TASK + "/ISSUE-7",
                                Phase.END,
                                "task",
                                Kind.SUB_AGENT,
                                null,
                                true,
                                "ISSUE-7",
                                null,
                                null,
                                null,
                                4045L));
    }

    @Test
    void ignoresUnreadableAndUnrelatedLinesWithoutStoppingTheRun() {
        for (final String line :
                List.of(
                        "plain text",
                        "{not json",
                        "null",
                        "[]",
                        "{}",
                        "{\"type\":\"message_update\"}",
                        "{\"type\":\"tool_execution_start\"}",
                        "{\"type\":\"tool_execution_end\",\"toolName\":\"bash\",\"toolCallId\":5}")) {
            assertThat(agent.toolEvents(line))
                    .as("unreadable or unrelated input yields no events: %s", line)
                    .isEmpty();
        }
    }

    @Test
    void classifiesToolNamesWithoutCopyingNonShellArguments() throws IOException {
        final Map<String, Kind> kinds =
                Map.ofEntries(
                        Map.entry("write", Kind.EDIT),
                        Map.entry("grep", Kind.SEARCH),
                        Map.entry("find", Kind.SEARCH),
                        Map.entry("ls", Kind.SEARCH),
                        Map.entry("fetch", Kind.WEB),
                        Map.entry("web_search", Kind.WEB),
                        Map.entry("mcp_example", Kind.MCP),
                        Map.entry("wait", Kind.OTHER),
                        Map.entry("unknown", Kind.OTHER));
        for (final var entry : kinds.entrySet()) {
            final String line =
                    fixture("omp", 0)
                            .replace(
                                    "\"toolName\":\"bash\"",
                                    "\"toolName\":\"" + entry.getKey() + "\"");
            assertThat(agent.toolEvents(line))
                    .as(
                            "the name table accounts for %s without leaking shell-shaped arguments",
                            entry.getKey())
                    .containsExactly(
                            new ToolEvent(
                                    0,
                                    SHELL,
                                    Phase.START,
                                    entry.getKey(),
                                    entry.getValue(),
                                    null,
                                    false,
                                    null,
                                    null));
        }
    }

    @Test
    void startsEachNamedTaskInTheArray() throws IOException {
        final String line =
                fixture("sub-omp", 0)
                        .replace(
                                "{\"name\":\"ISSUE-7\",\"agent\":\"task\"}",
                                "{\"name\":\"ISSUE-7\",\"agent\":\"task\"},{\"name\":\"worker\",\"agent\":\"task\"}");
        assertThat(agent.toolEvents(line))
                .as("each named entry gets its own id even when several start on one line")
                .containsExactly(
                        new ToolEvent(
                                0,
                                TASK + "/ISSUE-7",
                                Phase.START,
                                "task",
                                Kind.SUB_AGENT,
                                null,
                                true,
                                "ISSUE-7",
                                null,
                                "ISSUE-7",
                                null,
                                null),
                        new ToolEvent(
                                0,
                                TASK + "/worker",
                                Phase.START,
                                "task",
                                Kind.SUB_AGENT,
                                null,
                                true,
                                "worker",
                                null,
                                "worker",
                                null,
                                null));
    }

    private static String fixture(final String name, final int index) throws IOException {
        try (var input =
                Objects.requireNonNull(
                        OmpToolEventsTest.class.getResourceAsStream(
                                "/tool-events/" + name + ".jsonl"))) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .toList()
                    .get(index);
        }
    }

    /** A {@code wait} call's end as Oh My Pi wrote it, trimmed: the jobs it waited for. */
    private static final String WAIT_END =
            "{\"type\":\"tool_execution_end\",\"toolCallId\":\"call_wait|fc_1\",\"toolName\":\"wait\","
                    + "\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"## Completed (2)\"}],"
                    + "\"details\":{\"op\":\"wait\",\"jobs\":["
                    + "{\"id\":\"ISSUE-7\",\"type\":\"task\",\"status\":\"completed\",\"durationMs\":3887},"
                    + "{\"id\":\"bg_2\",\"type\":\"bash\",\"status\":\"failed\",\"durationMs\":120},"
                    + "{\"id\":\"bg_3\",\"type\":\"bash\",\"status\":\"running\"}]},\"isError\":false}}";

    @Test
    void aJobTheAgentWaitedForEndsWithTheWait() {
        assertThat(agent.toolEvents(WAIT_END))
                .as(
                        "the wait ends, and so does each finished job it reports, never one still running")
                .extracting(
                        ToolEvent::id, ToolEvent::phase, ToolEvent::jobId, ToolEvent::durationMs)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "call_wait|fc_1", Phase.END, null, null),
                        org.assertj.core.groups.Tuple.tuple("ISSUE-7", Phase.END, "ISSUE-7", 3887L),
                        org.assertj.core.groups.Tuple.tuple("bg_2", Phase.END, "bg_2", 120L));
    }

    @Test
    void aTaskThatEndsWithItsCallEndsItsSubagent() throws IOException {
        final String update = fixture("sub-omp", 3);
        final String end =
                update.replace("\"tool_execution_update\"", "\"tool_execution_end\"")
                        .replace("\"partialResult\":", "\"result\":");
        assertThat(agent.toolEvents(end))
                .as("a sub-agent finished when its call ends is closed there")
                .extracting(ToolEvent::id, ToolEvent::phase)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(TASK + "/ISSUE-7", Phase.END));
    }

    @Test
    void anAsyncResultEndsOnlyFinishedJobs() throws IOException {
        final String done = fixture("omp", 2);
        assertThat(
                        agent.toolEvents(
                                done.replace("\"status\":\"completed\"", "\"status\":\"running\"")))
                .as("a job still running has not ended")
                .isEmpty();
        assertThat(agent.toolEvents(done.replace("\"type\":\"bash\"", "\"type\":\"other\"")))
                .as("a job of a kind Oh My Pi does not name here is not invented as one")
                .isEmpty();
    }
}

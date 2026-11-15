package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OmpJsonFormatter}, with lines in the shapes of a real {@code --mode json} run,
 * trimmed.
 */
class OmpJsonFormatterTest {

    private final OmpJsonFormatter formatter = new OmpJsonFormatter();

    @Test
    void showsNothingForTheBookkeeping() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"session\",\"version\":3,\"id\":\"a1b2\",\"cwd\":\"/workspace\"}"))
                .isNull();
        assertThat(formatter.format("{\"type\":\"agent_start\"}")).isNull();
        assertThat(formatter.format("{\"type\":\"turn_start\"}")).isNull();
        assertThat(
                        formatter.format(
                                "{\"type\":\"turn_end\",\"message\":{\"role\":\"assistant\",\"content\":[]},\"toolResults\":[]}"))
                .isNull();
        assertThat(formatter.format("{\"type\":\"advisor_cost_changed\",\"cost\":0.01}")).isNull();
    }

    @Test
    void showsNothingForThePartials() {

        // These repeat the message so far on every token; message_end and tool_execution_end say it
        // whole.
        assertThat(
                        formatter.format(
                                "{\"type\":\"message_start\",\"message\":{\"role\":\"assistant\",\"content\":[]}}"))
                .isNull();
        assertThat(
                        formatter.format(
                                "{\"type\":\"message_update\",\"assistantMessageEvent\":{\"type\":\"text_delta\","
                                        + "\"delta\":\"I'll\"},\"message\":{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"I'll\"}]}}"))
                .isNull();
        assertThat(
                        formatter.format(
                                "{\"type\":\"tool_execution_update\",\"toolCallId\":\"t1\",\"toolName\":\"bash\","
                                        + "\"partialResult\":{\"content\":[{\"type\":\"text\",\"text\":\"src\"}]}}"))
                .isNull();
    }

    @Test
    void showsTheAssistantsTextWithoutItsThinking() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"message_end\",\"message\":{\"role\":\"assistant\",\"content\":["
                                        + "{\"type\":\"thinking\",\"thinking\":\"Let me look first.\"},"
                                        + "{\"type\":\"text\",\"text\":\"I'll read the contracts\\n\\nfirst.\"},"
                                        + "{\"type\":\"toolCall\",\"id\":\"t1\",\"name\":\"read\",\"arguments\":{\"path\":\"README.md\"}},"
                                        + "{\"type\":\"text\",\"text\":\"Then  the tests.\"}],\"stopReason\":\"toolUse\"}}"))
                .isEqualTo("I'll read the contracts first. Then the tests.");
    }

    @Test
    void cutsALongMessage() {

        final String text = "word ".repeat(100);
        final String shown =
                formatter.format(
                        "{\"type\":\"message_end\",\"message\":{\"role\":\"assistant\","
                                + "\"content\":[{\"type\":\"text\",\"text\":\""
                                + text
                                + "\"}],\"stopReason\":\"stop\"}}");

        assertThat(shown).hasSize(301).endsWith("…").startsWith("word word");
    }

    @Test
    void showsNothingForAMessageWithoutText() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"message_end\",\"message\":{\"role\":\"assistant\",\"content\":["
                                        + "{\"type\":\"thinking\",\"thinking\":\"Hmm.\"},"
                                        + "{\"type\":\"toolCall\",\"id\":\"t1\",\"name\":\"bash\",\"arguments\":{}}],\"stopReason\":\"toolUse\"}}"))
                .isNull();
    }

    @Test
    void showsNothingForThePromptAndTheToolResults() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"message_end\",\"message\":{\"role\":\"user\","
                                        + "\"content\":[{\"type\":\"text\",\"text\":\"Review the contracts.\"}]}}"))
                .isNull();
        assertThat(
                        formatter.format(
                                "{\"type\":\"message_end\",\"message\":{\"role\":\"toolResult\",\"toolCallId\":\"t1\","
                                        + "\"toolName\":\"bash\",\"content\":[{\"type\":\"text\",\"text\":\"ok\"}],\"isError\":false}}"))
                .isNull();
    }

    @Test
    void showsTheProvidersRefusal() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"message_end\",\"message\":{\"role\":\"assistant\",\"content\":[],"
                                        + "\"stopReason\":\"error\",\"errorStatus\":403,\"errorMessage\":\"403 Key limit exceeded\\n (total limit).\"}}"))
                .isEqualTo("-- error: 403 Key limit exceeded (total limit).");
    }

    @Test
    void namesTheToolCalledAndOnlyTheOneThatFailed() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"tool_execution_start\",\"toolCallId\":\"t1\",\"toolName\":\"bash\","
                                        + "\"args\":{\"command\":\"ls\"}}"))
                .isEqualTo("[bash]");
        assertThat(
                        formatter.format(
                                "{\"type\":\"tool_execution_end\",\"toolCallId\":\"t1\",\"toolName\":\"bash\","
                                        + "\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"src\"}]},\"isError\":false}"))
                .isNull();
        assertThat(
                        formatter.format(
                                "{\"type\":\"tool_execution_end\",\"toolCallId\":\"t2\",\"toolName\":\"read\","
                                        + "\"result\":{\"content\":[{\"type\":\"text\",\"text\":\"ENOENT\"}]},\"isError\":true}"))
                .isEqualTo("[read failed]");
    }

    @Test
    void endsWithTheTurnsCounted() {

        assertThat(
                        formatter.format(
                                "{\"type\":\"agent_end\",\"messages\":["
                                        + "{\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"Review.\"}]},"
                                        + "{\"role\":\"assistant\",\"content\":[],\"stopReason\":\"toolUse\"},"
                                        + "{\"role\":\"toolResult\",\"toolCallId\":\"t1\",\"toolName\":\"bash\",\"content\":[],\"isError\":false},"
                                        + "{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Done.\"}],\"stopReason\":\"stop\"}]}"))
                .isEqualTo("-- done, 2 turns");
        assertThat(
                        formatter.format(
                                "{\"type\":\"agent_end\",\"messages\":["
                                        + "{\"role\":\"assistant\",\"content\":[],\"stopReason\":\"stop\"}]}"))
                .isEqualTo("-- done, 1 turn");
    }

    @Test
    void endsWithTheProvidersRefusal() {

        // The real run's end: the key's limit reached on the last call, after the earlier turns had
        // answered.
        assertThat(
                        formatter.format(
                                "{\"type\":\"agent_end\",\"messages\":["
                                        + "{\"role\":\"assistant\",\"content\":[{\"type\":\"text\",\"text\":\"Reading.\"}],\"stopReason\":\"toolUse\"},"
                                        + "{\"role\":\"toolResult\",\"toolCallId\":\"t1\",\"toolName\":\"read\",\"content\":[],\"isError\":false},"
                                        + "{\"role\":\"assistant\",\"content\":[],\"stopReason\":\"error\",\"errorStatus\":403,"
                                        + "\"errorMessage\":\"403 Key limit exceeded (total limit). Manage it using the provider's settings.\"}]}"))
                .isEqualTo(
                        "-- ended with an error: 403 Key limit exceeded (total limit). Manage it using the provider's"
                                + " settings., 2 turns");
    }

    @Test
    void dropsAnUnknownEvent() {

        assertThat(formatter.format("{\"type\":\"something_new\",\"value\":1}"))
                .as("an unknown event without an error is raw JSON to a person")
                .isNull();
    }

    @Test
    void showsAnUnknownEventThatCarriesAnError() {

        assertThat(formatter.format("{\"type\":\"something_new\",\"error\":\"disk full\"}"))
                .as("an error string hidden")
                .isEqualTo("[error] disk full");
        assertThat(
                        formatter.format(
                                "{\"type\":\"something_new\",\"error\":{\"message\":\"quota gone\"}}"))
                .as("an error object's message hidden")
                .isEqualTo("[error] quota gone");
        assertThat(
                        formatter.format(
                                "{\"type\":\"auto_retry_start\",\"attempt\":1,\"errorMessage\":\"429 slow down\"}"))
                .as("an error message hidden")
                .isEqualTo("[error] 429 slow down");
        assertThat(
                        formatter.format(
                                "{\"type\":\"something_new\",\"isError\":true,\"message\":\"it broke\"}"))
                .as("a flagged error's message hidden")
                .isEqualTo("[error] it broke");
        assertThat(formatter.format("{\"type\":\"something_new\",\"isError\":true}"))
                .as("a flagged error without a message hidden")
                .isEqualTo("[error] something_new");
        assertThat(
                        formatter.format(
                                "{\"type\":\"something_new\",\"error\":\""
                                        + "x".repeat(1000)
                                        + "\"}"))
                .as("a long error shown whole or as raw JSON")
                .isEqualTo("[error] " + "x".repeat(300) + "…");
    }

    @Test
    void showsNoRawEventLineOfARecordedRun() throws IOException {

        // Recorded unattended with Oh My Pi 18.18.1; values replaced.
        final List<String> lines =
                Files.readAllLines(
                        Path.of(
                                "src/test/resources/streams/unattended-background-run-18.18.1.jsonl"));

        assertThat(lines).as("the recorded run of Oh My Pi 18.18.1").isNotEmpty();
        assertThat(lines.stream().map(formatter::format).filter(Objects::nonNull))
                .as("lines of the Oh My Pi 18.18.1 run a person would read as raw JSON")
                .noneMatch(shown -> shown.startsWith("{"));
    }

    @Test
    void showsALineThatIsNotJson() {

        assertThat(formatter.format("plain text from a crash"))
                .isEqualTo("plain text from a crash");
        assertThat(formatter.format("{not valid json")).isEqualTo("{not valid json");
        assertThat(formatter.format("y".repeat(400))).isEqualTo("y".repeat(300) + "…");
    }

    @Test
    void marksAKnownEventItCannotRead() {

        final String noMessage = "{\"type\":\"message_end\"}";
        assertThat(formatter.format(noMessage)).isEqualTo("[unreadable] " + noMessage);
        final String noTool = "{\"type\":\"tool_execution_start\"}";
        assertThat(formatter.format(noTool)).isEqualTo("[unreadable] " + noTool);
        final String noMessages = "{\"type\":\"agent_end\"}";
        assertThat(formatter.format(noMessages)).isEqualTo("[unreadable] " + noMessages);
    }

    @Test
    void passesNoTerminalControlCharacterToTheTerminal() {

        // The log is shown on a person's terminal; an escape sequence from a model, a tool or a
        // provider could clear it, move the cursor or retitle the window there. A newline and a tab
        // only lay text out.
        final String escape = "\u001b[2J";
        final String csi = "\u009b2J";
        final List<String> lines =
                List.of(
                        "a" + escape + "b" + csi + "c\u0007d",
                        "{\"type\":\"something_new\",\"error\":\"a\u001b]0;title\u0007b\"}",
                        "{\"type\":\"message_end\",\"message\":{\"role\":\"assistant\",\"content\":["
                                + "{\"type\":\"text\",\"text\":\"a\\u001b[2Jb\\u009b2Jc\"}]}}",
                        "{\"type\":\"tool_execution_start\",\"toolCallId\":\"t1\",\"toolName\":\"ba\\u001b[2Jsh\"}",
                        "{\"type\":\"agent_end\",\"messages\":[{\"role\":\"assistant\",\"content\":[],"
                                + "\"stopReason\":\"error\",\"errorMessage\":\"401 \\u001b[2Jno\"}]}");
        for (final String line : lines) {
            final String shown = formatter.format(line);
            assertThat(shown).as("what %s shows", line).isNotNull();
            assertThat(shown.codePoints().filter(OmpJsonFormatterTest::control).toArray())
                    .as("control characters shown for %s: %s", line, shown)
                    .isEmpty();
        }
        assertThat(formatter.format("a\tb")).as("a tab stays").isEqualTo("a\tb");
    }

    private static boolean control(final int codePoint) {
        return Character.isISOControl(codePoint) && codePoint != '\n' && codePoint != '\t';
    }

    @Test
    void showsAnErrorByItsKindItsLevelOrItsStatus() {
        assertThat(
                        List.of(
                                        "{\"type\":\"stream_failure\",\"message\":\"quota exceeded\"}",
                                        "{\"type\":\"something_new\",\"status\":\"Error\",\"message\":\"quota exceeded\"}",
                                        "{\"type\":\"something_new\",\"level\":\"FATAL\",\"message\":\"quota exceeded\"}",
                                        "{\"type\":\"something_new\",\"error\":true,\"message\":\"quota exceeded\"}",
                                        "{\"type\":\"something_new\",\"is_error\":\"true\",\"message\":\"quota exceeded\"}")
                                .stream()
                                .map(formatter::format)
                                .toList())
                .as(
                        "an unknown event that reports an error by its type, a status or level in any case, or a flag")
                .allSatisfy(
                        shown ->
                                assertThat(shown).startsWith("[error]").contains("quota exceeded"));
        assertThat(
                        formatter.format(
                                "{\"type\":\"something_new\",\"level\":\"warning\",\"message\":\"slow\"}"))
                .as("a warning")
                .startsWith("[warning]")
                .contains("slow");
    }

    @Test
    void anErrorCannotOpenALineOfItsOwn() {
        assertThat(
                        formatter.format(
                                "{\"type\":\"something_new\",\"error\":\"first\\n[error] forged\\u2028x\"}"))
                .as("a line break or a Unicode separator in an error is folded away")
                .doesNotContain("\n", "\u2028");
    }
}

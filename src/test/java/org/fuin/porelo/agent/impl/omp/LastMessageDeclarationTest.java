package org.fuin.porelo.agent.impl.omp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.fuin.porelo.agent.api.Waiting;
import org.fuin.porelo.wire.Json;
import org.junit.jupiter.api.Test;

/**
 * What a finished unattended run said last: the harm is a reader whose findings stand in its last
 * message and whose run is judged on an empty answer, or on its reasoning or a tool's output in
 * place of its reply.
 *
 * <p>The records are a whole run of the pinned Oh My Pi 18.18.1, recorded unattended in a Porelo
 * task on 2026-11-12, as the agent wrote them, without the streamed pieces ({@code message_update}
 * and {@code tool_execution_update}). The run reasoned, wrote a sentence and called {@code ls -a}
 * in one message, then reasoned again and replied {@code ALL DONE}.
 */
class LastMessageDeclarationTest {

    private static final Path RUN =
            Path.of("src/test/resources/streams/unattended-run-18.18.1.jsonl");

    private static List<Object> records() throws IOException {
        return Files.readAllLines(RUN).stream()
                .filter(line -> !line.isBlank())
                .map(line -> (Object) Json.parse(line))
                .toList();
    }

    private static Waiting.LastMessage lastMessage() {
        final Waiting waiting = new OmpAgent().definition().waiting();
        assertThat(waiting).as("omp.yaml declares session.waiting").isNotNull();
        final Waiting.LastMessage last = waiting.lastMessage();
        assertThat(last).as("omp.yaml declares session.waiting.last_message").isNotNull();
        return last;
    }

    private static boolean isAssistantEnd(final Object record) {
        return isEnd(record, "assistant");
    }

    private static boolean isEnd(final Object record, final String role) {
        return record instanceof Map<?, ?> fields
                && "message_end".equals(fields.get("type"))
                && fields.get("message") instanceof Map<?, ?> message
                && role.equals(message.get("role"));
    }

    @Test
    void answersTheTextOfTheLastReplyPastItsReasoning() throws IOException {

        // The last message holds a reasoning before its text; turn_end and agent_end repeat it.
        assertThat(lastMessage().of(records()))
                .as("the last reply's text, never its reasoning or a tool's output")
                .isEqualTo("ALL DONE");
    }

    @Test
    void aRunCutAfterItsFirstReplyAnswersThatReplysTextAlone() throws IOException {

        // The first message holds a reasoning, a sentence and a tool call; only the sentence is
        // said. Cut there, as a run stopped while its tool ran.
        final List<Object> all = records();
        final List<Object> cut = all.subList(0, all.indexOf(firstAssistantEnd(all)) + 1);

        assertThat(lastMessage().of(cut)).isEqualTo("I will look at the directory now.");
    }

    @Test
    void aToolsOutputIsNeverTheAnswer() throws IOException {

        // Cut after the tool's result, whose message_end holds the output as a text part: the
        // answer is still the sentence before the call.
        final List<Object> all = records();
        final Object result =
                all.stream().filter(each -> isEnd(each, "toolResult")).findFirst().orElseThrow();

        assertThat(lastMessage().of(all.subList(0, all.indexOf(result) + 1)))
                .isEqualTo("I will look at the directory now.");
    }

    @Test
    void aLaterMessageWithoutTextLeavesTheReplyBeforeIt() throws IOException {

        // A last message of a tool call alone, as a run stopped while its tool ran would leave:
        // the first message as recorded, its text part taken out.
        final List<Object> all = new ArrayList<>(records());
        final Map<?, ?> first = (Map<?, ?>) firstAssistantEnd(all);
        final Map<?, ?> message = (Map<?, ?>) first.get("message");
        final List<?> parts =
                ((List<?>) message.get("content"))
                        .stream()
                                .filter(part -> !"text".equals(((Map<?, ?>) part).get("type")))
                                .toList();
        assertThat(parts).as("its parts without the text").hasSize(2);
        final Map<Object, Object> toolCallOnly = new LinkedHashMap<>(message);
        toolCallOnly.put("content", parts);
        final Map<Object, Object> end = new LinkedHashMap<>(first);
        end.put("message", toolCallOnly);
        all.add(end);

        assertThat(lastMessage().of(all)).isEqualTo("ALL DONE");
    }

    @Test
    void aRunWithoutAReplyAnswersNothing() throws IOException {

        // Cut before the agent's first message ended: no answer, which is not an empty one.
        final List<Object> all = records();

        assertThat(lastMessage().of(all.subList(0, all.indexOf(firstAssistantEnd(all))))).isNull();
    }

    @Test
    void theDeclarationAddsNoScreenRule() {

        // Porelo wakes an agent by its screen rules; the answer of a finished run must not change
        // which screens read as waiting.
        assertThat(new OmpAgent().definition().waiting().screen())
                .containsExactly(
                        new Waiting.Rule("↑/↓ move · ⎋ cancel", 5, "an answer to its question"));
    }

    private static Object firstAssistantEnd(final List<Object> records) {
        return records.stream()
                .filter(LastMessageDeclarationTest::isAssistantEnd)
                .findFirst()
                .orElseThrow();
    }
}

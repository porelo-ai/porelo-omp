package org.fuin.porelo.agent.impl.omp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.fuin.porelo.agent.api.ToolEvent;
import org.fuin.porelo.agent.api.ToolEvent.Kind;
import org.fuin.porelo.agent.api.ToolEvent.Phase;
import org.fuin.porelo.wire.Json;
import org.jspecify.annotations.Nullable;

/** Reads tool accounting from one Oh My Pi JSON line without retaining state or tool output. */
public final class OmpToolEvents {

    private OmpToolEvents() {}

    /**
     * Maps a raw output line to tool events for Porelo to join by call or job id.
     *
     * @param line One line of Oh My Pi's JSON output.
     * @return The events, or an empty list for an unreadable or unrelated line.
     */
    public static List<ToolEvent> read(final String line) {
        final Object parsed;
        try {
            parsed = Json.parse(line);
        } catch (RuntimeException ex) {
            return List.of();
        }
        if (!(parsed instanceof Map<?, ?> event)) {
            return List.of();
        }
        if ("message_end".equals(event.get("type"))) {
            return jobs(map(event.get("message")));
        }
        if (!(event.get("toolCallId") instanceof String id)
                || id.isBlank()
                || !(event.get("toolName") instanceof String tool)
                || tool.isBlank()) {
            return List.of();
        }
        final Object type = event.get("type");
        if ("task".equals(tool)) {
            if ("tool_execution_start".equals(type)) {
                return tasks(id, map(event.get("args")));
            }
            if ("tool_execution_update".equals(type)) {
                return progress(id, map(map(event.get("partialResult")).get("details")));
            }
            if ("tool_execution_end".equals(type)) {
                // A task that ends with its call says so here, with no update before.
                return progress(id, map(map(event.get("result")).get("details")));
            }
            return List.of();
        }
        if ("tool_execution_start".equals(type)) {
            final Object command = map(event.get("args")).get("command");
            return List.of(
                    new ToolEvent(
                            0,
                            id,
                            Phase.START,
                            tool,
                            kind(tool),
                            "bash".equals(tool) && command instanceof String text ? text : null,
                            false,
                            null,
                            null));
        }
        if ("tool_execution_end".equals(type)) {
            final Map<?, ?> async = map(map(map(event.get("result")).get("details")).get("async"));
            final String job = "bash".equals(tool) ? text(async.get("jobId")) : null;
            final ToolEvent returned =
                    new ToolEvent(0, id, Phase.END, tool, kind(tool), null, false, null, null);
            if ("wait".equals(tool)) {
                // A job the agent waited for is reported here and never again as an async result.
                final List<ToolEvent> events = new ArrayList<>();
                events.add(returned);
                events.addAll(jobEnds(map(map(event.get("result")).get("details"))));
                return List.copyOf(events);
            }
            if (job == null) {
                return List.of(returned);
            }
            // The job's id comes only now: a second start names it, so the call ends with its job.
            return List.of(
                    new ToolEvent(0, id, Phase.START, tool, kind(tool), null, true, job, null),
                    returned);
        }
        return List.of();
    }

    private static List<ToolEvent> tasks(final String id, final Map<?, ?> args) {
        final List<ToolEvent> events = new ArrayList<>();
        for (final Object value : list(args.get("tasks"))) {
            final String name = text(map(value).get("name"));
            if (name != null) {
                events.add(subagent(id, name, Phase.START, null));
            }
        }
        return List.copyOf(events);
    }

    private static List<ToolEvent> progress(final String id, final Map<?, ?> details) {
        final List<ToolEvent> events = new ArrayList<>();
        for (final Object value : list(details.get("progress"))) {
            final Map<?, ?> entry = map(value);
            final String name = text(entry.get("id"));
            if (name != null && finished(entry.get("status"))) {
                final Long own = duration(entry.get("durationMs"));
                events.add(
                        subagent(
                                id,
                                name,
                                Phase.END,
                                own != null ? own : duration(details.get("totalDurationMs"))));
            }
        }
        return List.copyOf(events);
    }

    private static ToolEvent subagent(
            final String id, final String name, final Phase phase, final @Nullable Long duration) {
        return new ToolEvent(
                0,
                id + "/" + name,
                phase,
                "task",
                Kind.SUB_AGENT,
                null,
                true,
                name,
                null,
                phase == Phase.START ? name : null,
                null,
                duration);
    }

    private static List<ToolEvent> jobs(final Map<?, ?> message) {
        if (!"custom".equals(message.get("role"))
                || !"async-result".equals(message.get("customType"))) {
            return List.of();
        }
        return jobEnds(map(message.get("details")));
    }

    /** The ends of the finished jobs a report lists, a shell's or a sub-agent's, by job id. */
    private static List<ToolEvent> jobEnds(final Map<?, ?> details) {
        final List<ToolEvent> events = new ArrayList<>();
        for (final Object value : list(details.get("jobs"))) {
            final Map<?, ?> job = map(value);
            final String given = text(job.get("jobId"));
            final String id = given != null ? given : text(job.get("id"));
            final Object type = job.get("type");
            if (id != null
                    && ("bash".equals(type) || "task".equals(type))
                    && finished(job.get("status"))) {
                final boolean shell = "bash".equals(type);
                events.add(
                        new ToolEvent(
                                0,
                                id,
                                Phase.END,
                                shell ? "bash" : "task",
                                shell ? Kind.SHELL : Kind.SUB_AGENT,
                                null,
                                true,
                                id,
                                null,
                                null,
                                null,
                                duration(job.get("durationMs"))));
            }
        }
        return List.copyOf(events);
    }

    private static Kind kind(final String tool) {
        return switch (tool) {
            case "bash" -> Kind.SHELL;
            case "edit", "write" -> Kind.EDIT;
            case "read" -> Kind.READ;
            case "grep", "find", "ls" -> Kind.SEARCH;
            case "task" -> Kind.SUB_AGENT;
            case "fetch", "web_search" -> Kind.WEB;
            default -> tool.startsWith("mcp_") ? Kind.MCP : Kind.OTHER;
        };
    }

    private static boolean finished(final @Nullable Object status) {
        return "completed".equals(status) || "failed".equals(status);
    }

    private static @Nullable Long duration(final @Nullable Object value) {
        return value instanceof Number number && number.doubleValue() >= 0
                ? Long.valueOf(number.longValue())
                : null;
    }

    private static @Nullable String text(final @Nullable Object value) {
        return value instanceof String string && !string.isBlank() ? string : null;
    }

    private static Map<?, ?> map(final @Nullable Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }

    private static List<?> list(final @Nullable Object value) {
        return value instanceof List<?> list ? list : List.of();
    }
}

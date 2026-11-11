# OM51 — The Adapter Reports Each Tool Call With Its Time

```json
{
  "$schema": "issue-1.json",
  "id": "OM51",
  "title": "The Adapter Reports Each Tool Call With Its Time",
  "state": "Implementing",
  "size": "M",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "the adapter reads each tool call from its agent's output and hands Porelo its tool, kind, start, end, a shell call's command line, whether it ran in the background, and a sub-agent's name and the calls made under it, in Porelo's common entry",
  "openQuestions": 0
}
```

**Why this size.** a reader over the start and end events, the end of a background job in a later message, the `task` tool's sub-agents and their progress, a name table and tests from the recorded runs of CC57 and CC59 in porelo-claude-code. Grown from S: CC57 and CC59 in porelo-claude-code measured background jobs and sub-agents of Oh My Pi's own, each ending on a line other than its call's end. It builds against `ToolEvents` of Porelo's agent API, contract version 5.

**Estimate.** M: 53 min, the measured median of M over all repositories (n=113).

**What must be true.** For every tool call the agent makes in an unattended task, Porelo records one entry in its
common tool-call format: the task, the tool as the agent names it, its kind (shell, edit, read, search, web,
sub-agent, mcp or other), when it started and when it ended, for a shell call its command line, whether it ran in the
background, and for a sub-agent the name it was given and the calls made under it. Porelo joins those calls to the
issue whose id names the sub-agent. The adapter only maps its agent's own output; the format, the time a line without
its own time gets, the masking of a command line and where the entry is kept are Porelo's.

## Plan

- Source: Oh My Pi's JSON mode, measured with the pinned Oh My Pi (CC57 and CC59 in porelo-claude-code). `tool_execution_start`
  (`toolCallId`, `toolName`, `args`, `intent`) is START; `tool_execution_end` (`toolCallId`, `isError`) is END.
  `OmpJsonFormatter` reads the same events today.
- No tool line has a clock time; `at` stays empty. A job's or a sub-agent's END passes `durationMs`: the job's
  `durationMs` in the async-result, a sub-agent's `totalDurationMs`; porelod dates the start from it.
- `OmpAgent` overrides `Agent#toolEvents(String line)`; `OmpToolEvents` maps one output line to its
  `ToolEvent(line, id, phase, tool, kind, command, background, jobId, at, name, parent, durationMs)`. It keeps no state between
  lines: Porelo joins a start to its end by `id`, a background job's by `jobId`, and a call to its sub-agent by
  `parent`.
- Kinds: `bash` -> shell (`command` = `args.command`), `edit`/`write` -> edit, `read` -> read, `grep`/`find`/`ls` -> search, `task` -> sub-agent, `fetch`/`web_search` -> web, `mcp_*` -> mcp, any other (`todo`, `wait`, ...) -> other.
- A shell call's `command` is its whole command line as the stream gives it, unmasked; Porelo masks it before it is
  stored.
- Sub-agents: a `task` call may start several, one per `args.tasks[]` entry: each is a START with `id` =
  `<toolCallId>/<name>`, `name` = the entry's `name`, `jobId` = that name (Oh My Pi's job id). The call itself ends at
  once ("Spawned agent ... (job ...)"). A sub-agent ends with the `tool_execution_update` of that call whose
  `details.progress[]` entry turns completed or failed. Its own calls are not in the stream, so `parent` stays empty.
- Background: a `bash` call Oh My Pi moves to a job ends at once with "Backgrounded as job `bg_N`"; START and END
  of the call as usual, and the job is `background` with `jobId` = the end's `details.async.jobId`. The job ends with a `message_end` whose
  message has `role: custom`, `customType: async-result` and `details.jobs[{jobId, status, durationMs}]`. A command
  started with `&` inside a shell is not seen.
- Red first: `OmpToolEventsTest` feeds recorded lines (a shell call, a shell moved to job `bg_1` and its async-result end, a `task` call with a named entry and its progress end, a failed call, and a line that is not JSON) and expects the events above; it fails while
  `toolEvents` still answers the API's default, nothing.
- Level: unit tests over recorded output only; the acceptance legs show the entries of a real run once Porelo writes
  them.

## Acceptance

- A recorded unattended run yields one entry per tool call with tool, kind, start, end and, for a shell call, its
  command line; a background job's start and end are joined by its id; a sub-agent carries its name.
- No entry holds a file's content or a tool's output; a command line reaches Porelo unmasked and is stored masked.
- A line the reader cannot read yields nothing and never stops the run.

## What makes it uncertain

- A sub-agent's end comes as progress on a call that has already ended; a new pin may report it otherwise.

## Decisions

- Only unattended tasks are measured. Attended (tmux) sessions write no JSON stream; whether their tool calls are
  recorded is decided later.
- A shell call keeps its whole command line, not only its program. Porelo masks it before it is stored; the adapter
  passes the line as the stream gives it.
- A task gives each issue a sub-agent of its own, named by the issue's id; an issue's time is its sub-agent's span.

## To be checked

- The agent API with `name`, `parent` and `durationMs` on `ToolEvent`, installed locally to build against.

# OM51 — The Adapter Reports Each Tool Call With Its Time

```json
{
  "$schema": "issue-1.json",
  "id": "OM51",
  "title": "The Adapter Reports Each Tool Call With Its Time",
  "state": "Analyzed",
  "size": "S",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "the adapter reads each tool call from its agent's output and hands Porelo its tool, kind, start, end, a shell call's command line and whether it ran in the background, in Porelo's common entry",
  "openQuestions": 0
}
```

**Why this size.** one reader over the two events the formatter already reads, a name table and its tests; the background case to be measured first. It builds against `ToolEvents`, which Porelo's agent API adds in its contract version 5;
nothing of that format is decided here.

**What must be true.** For every tool call the agent makes in a task, Porelo records one entry in its common
tool-call format: the task, the tool as the agent names it, its kind (shell, edit, read, search, web, sub-agent, mcp
or other), when it started and when it ended, for a shell call its command line, and whether it ran in the
background; a background job's start and end are joined by its id. The adapter only maps its agent's own output into
that entry; the format, the time a line without its own time gets, and where the entry is kept are Porelo's.

## Plan

- Source: Oh My Pi's JSON mode. `tool_execution_start` and `tool_execution_end`, joined by `toolCallId`, with `toolName` and `args`; `OmpJsonFormatter` reads the same two events today.
- `OmpAgent` overrides `Agent#toolEvents(String line)`, which the agent API's contract version 5 adds and AgentServer
  serves as `ToolEvents`; `OmpToolEvents` maps one output line to its tool events (`id`, START or END, `tool`, `kind`,
  `command`, `background`, `jobId`, `at` where the line has a time). It keeps no state between lines: Porelo joins a
  start to its end by `id`, and a background job's by `jobId`.
- Kinds: `bash` -> shell (command from `args.command`), `edit`/`write` -> edit, `read` -> read, `grep`/`find`/`ls` -> search, `task` -> sub-agent, `fetch`/`web_search` -> web, `mcp_*` -> mcp, any other -> other.
- A shell call's `command` is its whole command line as the stream gives it. The adapter does not mask it: Porelo
  masks the line once, before anything is stored.
- Background: Whether the pinned Oh My Pi runs a shell call in the background, and by which id its end is told, is measured before the build; until then a call is foreground.
- Red first: `OmpToolEventsTest` feeds a recorded run's lines (a shell call, an edit, an unknown tool, a failed call,
  and a line that is not JSON) and expects one start and one end per call, the kinds above and each shell call's whole command line; it
  fails while `toolEvents` still answers the API's default, nothing.
- Level: unit tests over recorded output only; the acceptance legs show the entries of a real run once Porelo writes
  them.

## Acceptance

- A recorded unattended run yields one entry per tool call with tool, kind, start, end and, for a shell call, its
  command line; a background job's start and end are one entry.
- No entry holds a file's content or a tool's output; a command line reaches Porelo unmasked and is stored masked.
- A line the reader cannot read yields nothing and never stops the run.

## Decisions

- Only unattended tasks are measured. Attended (tmux) sessions write no JSON stream; whether their tool calls are
  recorded is decided later.
- A shell call keeps its whole command line, not only its program. Porelo masks it before it is stored (option values,
  an `Authorization:` header, env assignments of secrets, a URL's user information, credential-shaped strings and the
  task's vault values); the adapter passes the line as the stream gives it.

## To be checked

- Porelo's agent API with contract version 5 and `Agent#toolEvents`, installed locally to build against.
- Whether the pinned Oh My Pi starts a background shell job, and which event and id report its end.
- Whether its tool events carry a time of their own.

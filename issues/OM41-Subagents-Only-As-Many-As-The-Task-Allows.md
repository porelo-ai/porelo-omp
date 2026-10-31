# OM41 — Subagents Only As Many As The Task Allows

**Status:** Accepted (operator, 2026-10-31).

**What must be true.** omp in a Porelo task starts no subagent unless the task allows it, and then no more
at once and no deeper than the task says.

## Plan

- **The contract built against:** Porelo's agent interface carries the number of subagents an agent may
  start inside a task, 0 (none) by default; each agent definition declares what it can enforce - off, a
  concurrent count, a depth - and Porelo refuses a value the definition cannot enforce instead of
  enforcing less.
- Read and measured headless at the pin 18.12.1, on the VM through OpenRouter, the settings passed in a
  `--config` overlay: by default a subagent starts, and one it starts can start another.
  - **Off:** `task.maxRecursionDepth: 0` holds - the session has no `task` tool, and `agent()` inside the
    eval tool is refused with "Cannot spawn another agent at task depth 0; maximum depth is 0".
  - **Not off:** `tools.approval: {task: deny}` blocks the `task` tool, but the model then started a
    subagent through `agent()` in the eval tool; a `--tools` list without `task` leaves the same way open.
  - **Depth:** `task.maxRecursionDepth` (default 2) is the number of levels that may start subagents.
  - **Concurrent count:** `task.maxConcurrency: 1` holds by queueing - three asked at once all ran, one
    after the other. 0 means unlimited, not none.

## Acceptance

- Seen to fail first: a task with the default asks omp to start a subagent, and it does.
- With the change, the same task starts none; a task allowing N starts at most N at once and refuses the
  next.

## To be checked

- Whether the adapter can hand omp these settings other than through a config overlay, and where the
  overlay lives in a task.
- The same switches attended, at a terminal.

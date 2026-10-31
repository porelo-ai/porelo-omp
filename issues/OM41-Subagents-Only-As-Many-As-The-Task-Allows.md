# OM41 — Subagents Only As Many As The Task Allows

**Status:** Accepted (operator, 2026-10-31).

**What must be true.** omp in a Porelo task starts no subagent unless the task allows it, and then no more
at once and no deeper than the task says.

## Plan

- **The contract built against:** Porelo's agent interface carries the number of subagents an agent may
  start inside a task, 0 (none) by default; each agent definition declares what it can enforce - off, a
  concurrent count, a depth - and Porelo refuses a value the definition cannot enforce instead of
  enforcing less.
- Read in 16.3.11, two major versions below the pin: subagents are the tool `task`; `task.maxConcurrency`
  (default 32) caps how many run at once, where 0 means unlimited, not none, and `task.maxRecursionDepth`
  (default 2) caps the depth. Off is a `--tools` list without `task`.

## Acceptance

- Seen to fail first: a task with the default asks omp to start a subagent, and it does.
- With the change, the same task starts none; a task allowing N starts at most N at once and refuses the
  next.

## To be checked

- Read again at the pinned version first: the setting names, what 0 means, and whether off still needs a
  whole `--tools` list, which would have to follow every tool a pin bump adds.
- On the VM, at the pinned version: that each switch holds, attended and headless.

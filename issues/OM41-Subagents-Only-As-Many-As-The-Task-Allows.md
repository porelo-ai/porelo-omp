# OM41 — Subagents Only As Many As The Task Allows

**Status:** Estimated (operator, 2026-10-31).
**Size:** S - one module and its tests, the settings go into a file already written; about 35 minutes: the S
median of 24.5, raised by the scenario on the VM, the check that the keys hold in `config.yml` as in the
`--config` overlay, and the attended check.

**What must be true.** omp in a Porelo task starts no subagent unless the task allows it, and then no more at
once than the task says.

## Plan

- **The contract built against:** Porelo's agent interface carries one number, the most subagents an agent may
  run at the same time inside a task (not a total over the task), 0 (none) by default, and no depth; each
  agent definition declares what it can enforce of it - off only, or a count - and Porelo refuses a value the
  definition cannot enforce with exit code 64 instead of enforcing less. Where a CLI needs a depth to make the
  count hold, the adapter sets it itself.
- Read and measured headless at the pin 18.12.1, on the VM through OpenRouter with
  `deepseek/deepseek-v4.1-flash` and `--auto-approve`, the settings passed in a `--config` overlay in
  `config.yml` form, nested (`task:` / `  maxRecursionDepth: 0`): by default a subagent starts, and one it starts can start another.
  - **Off:** `task.maxRecursionDepth: 0` holds - the session has no `task` tool, and `agent()` inside the
    eval tool is refused with "Cannot spawn another agent at task depth 0; maximum depth is 0".
  - **Not off:** `tools.approval: {task: deny}` blocks the `task` tool, but the model then started a
    subagent through `agent()` in the eval tool; a `--tools` list without `task` leaves the same way open.
  - **Depth:** `task.maxRecursionDepth` (default 2) is the number of levels that may start subagents.
  - **Concurrent count:** `task.maxConcurrency: 1` holds by queueing - three asked at once all ran, one
    after the other. 0 means unlimited, not none.
- **`omp.yaml`** declares off and a count.
- **`OmpConfigFile.document`**, which already writes `/home/agent/.omp/agent/config.yml` for every task, adds
  `task.maxRecursionDepth` - 0 when no subagent is allowed, which is what switches them off, eval's `agent()`
  included - and above 0 `task.maxConcurrency` with the allowed number and `task.maxRecursionDepth: 1`. The
  depth is the adapter's own: only the agent itself starts subagents, so the count holds for the whole task.
  Never 0 for the concurrency, which omp reads as unlimited.
- **Red first:** `OmpContainerSetupTest` - a context with the default yields `maxRecursionDepth: 0`; today
  the file has no `task` section, and omp's default of 2 lets subagents start.
- **On the VM:** a `session.feature` scenario asks for a subagent in a task with the default and sees no
  `task` call succeed.
- **A soft switch:** the agent has a shell and can start another copy of its CLI; this issue governs only
  the built-in subagents. A hard bound is Porelo's own, outside this repository.
- **Porelo's version:** `porelo.version` in `pom.xml` moves to the Porelo snapshot whose agent API
  carries the value; until then nothing here compiles against it.

## Acceptance

- Seen to fail first: a task with the default asks omp to start a subagent, and it does.
- With the change, the same task starts none; a task allowing N starts at most N at once, and the next waits
  until one ends, since omp queues it.

## Open questions

- None.

## To be checked

- That the keys hold the same in `config.yml` itself as in the `--config` overlay they were measured in.
- The same switches attended, at a terminal.

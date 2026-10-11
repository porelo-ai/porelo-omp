# OM19 — Declare a thinking level and a read-only mode

**Status:** Analyzed; blocked by sokar B101.
**Size:** S - here entries in `omp.yaml` and their tests; the keys a definition may declare, and the
start's options for them, are Sokar's.

**What must be true.** A task start can set omp's thinking level, and can ask for a read-only mode by
name, without being able to pass any other flag through.

## Why

omp has `--thinking=<level>`, `--tools=<list>` (only the named tools are enabled) and `--max-time`,
but a task can use none of them: `sokar task start` gives an agent only what its definition declares,
and `omp.yaml`'s `headless:` declares only `--model` and the output flags. That is by design - a start
that could pass any flag through could undo `--auto-approve` and the rest the definition holds. A long
review that should only read can today be held to that only by its prompt.

## Plan

- **After B101** has given a definition named modes and the start `--mode <name>`:
  `src/main/resources/agent/omp.yaml` declares `read-only` as `--tools=` with omp 18.4.1's reading and
  searching tools and its one write tool, the names read from `omp --help` and its tool list.
- **The thinking level:** a `headless:` key beside `model_flag` naming `--thinking`, once Sokar's
  definition reader (`AgentDefinitionReader`, `HeadlessFlags`) knows such a key and the start passes a
  value from omp's list (`off` to `max`, `auto`).
- **Red first:** `OmpAgentTest` reads the definition and expects the mode and the key; then, on the VM,
  a task started in `read-only` is asked to write outside it and is seen refused by omp.
- `sokar agents --verbose` shows both, which the definition test checks through the API.

## Acceptance

- A `headless:` entry for the thinking level, so a start sets it the way it sets `--model`.
- A read-only mode declared in `omp.yaml` (read and search tools plus one write), which a start asks
  for by name; in that mode a write outside it is seen to be refused.

## To be checked

- Whether B101 also carries a thinking-level key, or that is a Sokar issue of its own - Agent Core's.
- Which of omp's tools make "read and search plus one write" - mine, read from 18.4.1 when built.

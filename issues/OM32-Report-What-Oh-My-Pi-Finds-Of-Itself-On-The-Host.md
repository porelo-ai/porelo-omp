# OM32 — Report What Oh My Pi Finds Of Itself On The Host

**Status:** Analyzed; waits for the agent API's method that asks an agent what it finds of itself on the
host.
**Size:** S - one class and its tests in this module, reading only whether files exist; the contract it answers is
porelo's.

**What must be true.** Asked what it finds of itself on the host, the Oh My Pi adapter answers whether Oh My Pi is
set up there, with the path, and says "cannot say" for its sign-in, which stays with `vault login`.

## Why

Porelo makes the agent already on the machine the default: each packaged agent reports what it finds of itself, and `porelo` chooses. Oh My Pi keeps its
sign-ins in a SQLite database, and this adapter reads none. Reading them is
[OM33](README.md#om33).

## Plan

- **`OmpFindings`**, answering porelo's `Find` with the directory `~/.omp/agent`:
  - `agent.db` there, a regular file: Oh My Pi is set up, with its path; the sign-in is `CANNOT_SAY`, saying it is
    kept in that database.
  - no `agent.db`: not set up, `NOT_SIGNED_IN`.
  - a directory it may not read: `CANNOT_SAY`, with the path and the reason.
  - The file is only tested for existence, never opened, so a database Oh My Pi holds open is not touched.
- **`OmpAgent`** overrides the agent API's findings method with it; `omp.yaml` gains `config_dir: "~/.omp/agent"`,
  which `vault import` then reads and finds nothing to import, as today.
- **Red first:** `OmpFindingsTest` over a directory with `agent.db`, one without, and one that cannot be read.

## Acceptance

- On the VM's `smith`, after Oh My Pi ran once, the installed adapter reports it set up from `~/.omp/agent/agent.db`
  and its sign-in as "cannot say"; on an account where it never ran, not set up.
- `porelo vault import --agent omp` still says there is nothing to import, and `vault login` still works.

## To be checked

- Whether the pinned Oh My Pi 18.11.0 moves `~/.omp/agent` by an environment variable, as Pi does with
  `PI_CODING_AGENT_DIR`.

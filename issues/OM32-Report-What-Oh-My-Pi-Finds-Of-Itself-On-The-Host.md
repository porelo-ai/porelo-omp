# OM32 — Report What Oh My Pi Finds Of Itself On The Host

**Status:** Review; acceptance through the installed adapter on a VM, built against the published agent API.
**Size:** S - one class and its tests in this module, reading only whether files exist; the contract it answers is
porelo's.

**What must be true.** Asked what it finds of itself on the host, the Oh My Pi adapter answers whether Oh My Pi is
set up there, with the path, and says "cannot say" for its sign-in.

## Why

Porelo makes the agent already on the machine the default: each packaged agent reports what it finds of itself, and `porelo` chooses. Oh My Pi keeps its
sign-ins in a SQLite database, and this adapter reads none. Reading them is
[OM33](README.md#om33).

## Plan

- **`OmpFindings`**, answering porelo's `Find` with the directory `.omp/agent` in the home:
  - `agent.db` there, a regular file: a configuration finding with its path, and a sign-in finding `CANNOT_SAY` with
    the same path.
  - no `agent.db`, or something else by that name: no finding, which reads as not signed in.
  - a directory it may not look into: a sign-in finding `CANNOT_SAY` with the path. The contract has no field for a
    reason, so none is given.
  - The file is only tested for existence, never opened, so a database Oh My Pi holds open is not touched.
- **`OmpAgent`** overrides the agent API's findings method with it; `omp.yaml` gains `config_dir`, naming `.omp/agent` in the home,
  which `vault import` then reads and finds nothing to import, as today.
- **Red first:** `OmpFindingsTest` over a directory with `agent.db`, one without, and one that cannot be read.

## Acceptance

- On a VM, after Oh My Pi ran once, the installed adapter's `Find` reports it set up from
  `.omp/agent/agent.db` in the home and its sign-in as "cannot say"; on an account where it never ran, nothing.
- `porelo vault import omp` says there is nothing to import and offers no login, `porelo vault login omp` refuses
  without starting a container, and `porelo vault authorize github-copilot` still works for Oh My Pi.

## What Oh My Pi does

Read in the pinned package 18.12.1, not run. `PI_CODING_AGENT_DIR` moves `.omp/agent` in the home and `PI_CONFIG_DIR` moves
`.omp` in the home, and `OMP_PROFILE` selects a profile elsewhere. The adapter reads no environment: a moved directory reaches it
only as the `configDirectory` Porelo passes, so an account that moved it reads as nothing there. A sign-in for Oh My Pi
is made with `vault put` and `vault authorize github-copilot`. `vault login omp` refuses, and only because no
directory was named: with `config_dir` it must go on refusing, which needs Porelo to read a `login:` section without
`arguments` as no login, since the definition's section holds only `flow` and `grants`.

## Checked

- On a VM, with an empty database file in place of a real run, the installed adapter reported it set up and its sign-in
  as "cannot say"; a directory without one answered nothing.
- `vault import omp` says there is nothing to import and offers no login, 69; `vault login omp` refuses, 69, without a
  container. `login:` holding only its flow and grants reads as no login, held by a test.

## To be checked

- That `vault authorize github-copilot` still works for Oh My Pi: the definition's grants are unchanged and held by a
  test, but the device flow needs a person at GitHub, and has not run.

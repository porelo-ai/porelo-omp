# OM39 — The Busy Check Waits On A Tool The Scenario Ends

**Status:** Backlog.
**Size:** S - one scenario of `wake.feature` in this repository, no contract another repository reads.

**What must be true.** In `wake.feature`'s scenario *a message that arrives while the agent works wakes it once it
is at rest*, the check that omp is busy no longer depends on how the model schedules its tools. The tool call
runs until the scenario ends it, and the message is sent while it still runs.

## Why

Today the agent is asked to run `sleep 10`, and the check passes only on a screen that shows `$ sleep 10` with no
`Wall:` yet. Build 40897646019 (65bf980, job 123532426491, Acceptance on ubuntu) failed at `wake.feature:83`:
- DeepSeek V4.1 Flash ran the sleep and an `ls` of the mailbox in parallel.
- omp drew both blocks only once both had ended, the sleep with `Wall: 10.06s`.
- So no capture ever showed the sleep without its result, and the 120 s ran out.

The same scenario was green an hour before, in 7edc818. Nothing about Porelo or the adapter differed; the model
chose differently. Ten seconds is also a window that a slow first token can use up on its own.

`porelo-claude-code` solved this in its own `wake.feature`. It has the agent run
`until [ -e /tmp/porelo-busy-done ]; do sleep 2; done` in the foreground, and the scenario ends the loop itself.

## Plan

- Ask omp to run `until [ -e /tmp/porelo-busy-done ]; do sleep 2; done` in the foreground with its shell tool,
  then reply DONE. It runs with a timeout above the scenario's own waits, which the prompt names, since omp's bash
  tool otherwise stops it at its default timeout (30 s in the failed run).
- The busy check passes on the loop's command line in omp's tool block with no `Wall:` under it. omp runs a
  command in its own process, so `podman top` does not show it.
  - A capture that sees the line still standing in the input box presses Enter again, as in claude-code.
  - A capture that sees neither says what it saw.
- After `porelo talk tell` exits zero, a step runs `podman exec porelo-busy-busy touch /tmp/porelo-busy-done`, so
  the turn ends only after the message is in `inbox/new`. The wake check that follows stays as it is.

## Acceptance

- Seen to fail first: the check as it is today fails on a screen captured from the failed run, where the sleep's
  block already shows `Wall:`.
- The same screen with the loop running and no `Wall:` passes the new check, and with `Wall:` under the loop it does
  not.
- Acceptance on ubuntu and on fedora runs the scenario green.

## To be checked

- That omp 18.x's bash tool takes a timeout from the prompt, and what its block shows while a command runs
  longer than its default timeout.
- That a model that runs a second tool beside the loop still leaves the loop's block without `Wall:` until the file
  exists.

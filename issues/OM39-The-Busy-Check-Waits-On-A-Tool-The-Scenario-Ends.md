# OM39 — The Busy Check Waits On A Tool The Scenario Ends

**Status:** Review; three runs on the VM green, the legs on ubuntu and fedora are the Build's.
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

- Ask omp to run `touch /tmp/porelo-busy-started; until [ -e /tmp/porelo-busy-done ]; do sleep 2; done; rm -f
  /tmp/porelo-busy-started` in the foreground with its shell tool and a timeout of 600 seconds, then reply DONE.
  The timeout is named because the model sets its own (30 s for the sleep in the failed run); omp's default is
  300 s. The started file exists only while the loop runs.
- The busy check passes once the started file exists, omp is not at rest (no `π > ` in the last 20 lines, which
  `omp.yaml`'s `at_rest` reads) and the screen shows no `Backgrounded:`, omp's mark of a job sent to the
  background, which would end the turn at once. The file is read before the screen. The check no longer reads
  the tool's block, which omp may draw only with its result. omp runs a command inside its own process, so
  `podman top` does not show it.
- After `porelo talk tell` exits zero, a step checks again that the started file exists and omp is not at rest,
  then touches `/tmp/porelo-busy-done`, so the turn ends only after the message is in `inbox/new`. The wake
  check that follows stays as it is.

## Acceptance

- Seen to fail first: the check as it was fails on the screen of the failed run, where the sleep's block already
  shows `Wall:`. The new check fails while the started file is missing, while omp is at rest and while
  `Backgrounded:` shows, and it passes once the loop has started.
- Acceptance on ubuntu and on fedora runs the scenario green.

## To be checked

- That a model which runs a second tool beside the loop still leaves the turn open until the file exists.

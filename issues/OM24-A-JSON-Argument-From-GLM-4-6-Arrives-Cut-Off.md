# OM24 — A JSON argument from GLM-4.6 arrives cut off

**Status:** Estimated.
**Size:** XS - a line in `doc/`; nothing is measured.

**What must be true.** Whoever runs omp with `z-ai/glm-4.6` can have the agent write a file whose
content is JSON - a mailbox message among them - or is told plainly that this model cannot.

## Why

Measured headless on the VM, the agent told to `Write` a one-line JSON message into a file: with
`z-ai/glm-4.6` the `write` call's `content` arrived as `"to` or `to`, everything from the first `":` on
gone, and the model retried it 105 times in three minutes. In the mailbox the same cut leaves
`content` empty, and omp refuses "content is required" until the task ends. With `z-ai/glm-5.3-flash`
the same call carried the whole JSON, once. CI runs `z-ai/glm-5.3-flash` for that reason.

## Plan

- **`doc/index.md`, "Start a task":** one line names `z-ai/glm-4.6` as unable to send a message with Oh My
  Pi, and `z-ai/glm-5.3-flash` as one that can. Not measured further; if the cut shows with a newer model, it
  is measured then.

## Acceptance

- `doc/index.md` names `z-ai/glm-4.6` as unable to send a message with Oh My Pi.

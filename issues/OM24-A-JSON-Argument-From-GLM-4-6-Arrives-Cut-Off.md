# OM24 — A JSON argument from GLM-4.6 arrives cut off

**Status:** Analyzed.
**Size:** S - a measurement with the raw stream and a line in `doc/`; a fix in omp or at the provider
would be upstream's, not built here.

**What must be true.** Whoever runs omp with `z-ai/glm-4.6` can have the agent write a file whose
content is JSON - a mailbox message among them - or is told plainly that this model cannot.

## Why

Measured headless on the VM, the agent told to `Write` a one-line JSON message into a file: with
`z-ai/glm-4.6` the `write` call's `content` arrived as `"to` or `to`, everything from the first `":` on
gone, and the model retried it 105 times in three minutes. In the mailbox the same cut leaves
`content` empty, and omp refuses "content is required" until the task ends. With `z-ai/glm-5.3-flash`
the same call carried the whole JSON, once. CI runs `z-ai/glm-5.3-flash` for that reason.

## Plan

- **The provider's stream without omp:** the same request - omp's `write` tool schema and the prompt -
  sent straight to OpenRouter with `stream: true`, from the host, and the tool call's argument deltas
  read as they arrive. Cut there: the model or the provider. Whole there: omp's reading of it.
- **omp's side, if the stream is whole:** a headless `omp --print --mode json` run with the same prompt,
  its tool-call record compared with the stream.
- **Then one of three:** a report upstream with both streams; or, if it is the model, a line in
  `doc/index.md` naming `z-ai/glm-4.6` as unable to send a message.
- **Red first, if a fix lands:** a scenario with that model writing a JSON file fails today and passes
  after it.

## Acceptance

- Where the argument is cut - in what the model emits, in the provider's translation, or in omp's
  reading of it - is measured from the raw stream.
- The cut is fixed where it happens, reported upstream, or `doc/` names the model as unable to send a
  message; a scenario with that model writing a JSON file is seen to fail before and pass after a fix.

## Open questions

- Whether a model this old is still worth the measurement, or `doc/` names it as unable to send a
  message now.

## To be checked

- Whether other models of the same family cut the same argument - in the same measurement.

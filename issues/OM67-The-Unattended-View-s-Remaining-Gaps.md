# OM67 — The Unattended View's Remaining Gaps

```json
{
  "$schema": "issue-1.json",
  "id": "OM67",
  "title": "The Unattended View's Remaining Gaps",
  "state": "Backlog",
  "size": "S",
  "priority": "Medium",
  "rank": 1,
  "blockedBy": [],
  "covers": "what [OM66](README.md#om66) left open in the unattended view: an error carried by an event of a known silent type, line breaks in a tool name or a line that is not JSON, the event types of the pinned CLI, and a recorded-run test that guards nothing"
}
```

**Why this size.** One class, the formatter, and its tests, plus one recorded run on the test VM.

**Estimate.** S: 27 min, the measured median of S over all repositories (n=168).

**What must be true.** Nothing [OM66](README.md#om66) left open is lost: each point below is fixed, or shown by a test or a recording not
to happen.

## Why

Kept from [OM66](README.md#om66) (*The Unattended View Shows No Raw Event Lines*, Finished 2026-11-15) when it was deleted: the points the
reading by another model raised and [OM66](README.md#om66) noted but did not fix, and the part of its point to be checked that its
recording did not answer.

## Plan

- **An error in a silent event.** An event of a known silent type (`turn_end`, `tool_execution_update`) or a
  `message_end` that is not the assistant's is dropped even when it carries an error, e.g. `message_end` with
  `{"role":"toolResult","isError":true}`. The error rule applied to them too, with a test seen red first.
- **Line breaks.** A tool name and a line that is not JSON keep their line breaks (`printable` keeps `\n`), so they
  can hold a line that reads like `[error] ...`. Folded the way the error line is.
- **The events 18.18.1 writes beyond one run.** [OM66](README.md#om66)'s one recorded run held only known events. A longer run of the
  pin, with a retry and a failed tool, read for event types beyond the handled ones.
- **A recorded-run test that guards nothing.** `showsNoRawEventLineOfARecordedRun` passes without the rule, since
  its run holds only known events; with the new recording it guards something, or it goes.
- **Level:** unit tests of the formatter; the recording through the coordinator's VM slot.

## Acceptance

- Each point above is fixed with a test seen red first, or recorded as not happening with its evidence.

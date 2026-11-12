# OM54 — A Finished Run Answers Its Last Message

```json
{
  "$schema": "issue-1.json",
  "id": "OM54",
  "title": "A Finished Run Answers Its Last Message",
  "state": "Review",
  "size": "S",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "a finished unattended Oh My Pi run answers the text of its last message, declared in session.waiting.last_message, so a reader's answer is not empty; the declaration adds no screen rule and so changes nothing in how Porelo wakes the agent"
}
```

**Why this size.** one declaration and its test, after reading one recorded run; no contract another repository reads beyond the existing field.

**What must be true.** When an unattended Oh My Pi run ends, Porelo answers what the agent said last. A reader whose
findings stand in its last message is judged on them, not on an empty answer, which is what a run answers while the
definition declares no last message.

## Plan

- The contract built against: `session.waiting.last_message` names the last record whose fields match `record` and
  the field holding its text; with `part: { record: {...}, text: <field> }` that field may hold a list of parts, and
  the answer is the text of each matching part, joined by a newline. A `waiting` that holds no screen rule declares
  no screen: Porelo then types no wake line into the agent for it.
- Oh My Pi's unattended stream (`--print --mode json`) ends each message with a `message_end` record whose
  `message.content` is, as far as its kinship with Pi suggests, a list of parts (thinking, text, tool calls); the last
  assistant message's text parts are the answer. `src/main/resources/agent/omp.yaml` declares it under its existing
  `waiting`, beside its screen rules, with `part` if the content is such a list.
- Tests: a unit test answers the last message over the agent's records of a run's end (red first, without the
  declaration), from records as the agent writes them in a recorded run, never as the code assumes them.

## Acceptance

- Over a recorded run's end - a reply, a tool call, its reasoning, a later reply - the answer is the text of the last
  reply, and a run without one answers nothing.
- The declaration changes neither the agent's screen rules nor whether Porelo wakes it.

## To be checked

- The record and field the agent's last reply stands in, read in a recorded unattended run at the pinned version.
  Read in a run of 18.18.1 recorded unattended in a Porelo task on 2026-11-12 (`src/test/resources/streams/unattended-run-18.18.1.jsonl`,
  without the streamed `message_update` and `tool_execution_update` records): `message_end` of role `assistant`
  holds `message.content` as a list of parts, as the plan expected - the first `thinking`, `text` "I will look at the
  directory now." and `toolCall` `bash`; the last `thinking` and `text` "ALL DONE". `turn_end` and `agent_end` repeat
  the messages. The declaration names `message_end` of role `assistant`, `message.content`, and its `text` parts.
- Over the recording the answer is `ALL DONE`; cut after the first message it is that message's sentence alone, and
  cut before it nothing; cut after the tool's result, whose text part holds the output, still the sentence (seen to
  fail without `message.role: assistant`); a later message of the recorded tool call alone leaves `ALL DONE`. The
  screen rules are unchanged, compared whole.
- A run on an unknown model is refused before it starts (`Model "…" not found`, no record written, measured the same
  day) and answers nothing. Not seen: an API error during a run, a turn limit, a sub-agent's messages in the stream.
  A last message holding a text part with empty text answers an empty text; the declaration cannot leave it out.
- Read-only review by a Claude Code sub-agent, Sonnet: confirmed from the contract's bytecode that the last matching
  message holding a text part answers; found the claims on a text-less message and on a tool's output untested, now
  tested, and an empty text part and a sub-agent unmentioned, now written down. Not taken: loading the records from
  the classpath, as the adapters' tests read them by path.

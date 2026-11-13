# OM59 — The Word Check Skips The First Turn

```json
{
  "$schema": "issue-1.json",
  "id": "OM59",
  "title": "The Word Check Skips The First Turn",
  "state": "Backlog",
  "size": "XS",
  "priority": "Low",
  "rank": 1,
  "blockedBy": [],
  "covers": "session.feature's check that the continued omp session knows the word leaves out the first turn's own line, so that line on the screen is never taken for the answer"
}
```

**Why this size.** One `grep -v` in one scenario of `session.feature`; no code.

**Estimate.** XS: 9 min, the measured median of XS over all repositories (n=105). Expect more for the acceptance
run that proves it.

**What must be true.** After the restart, the scenario passes only when the continued session answers with PAPAYA.
Today its last check drops only the question's line ("Which word did I ask you to remember?") before looking for
PAPAYA. The first turn's own line, "Remember the word PAPAYA. Reply with OK only.", still on the screen of a
continued session, passes it with no answer at all.

## Why

Found on 2026-11-13 while porelo-claude-code took the same check over (its CC67). There the check leaves out both lines;
checked by hand, the first turn's line alone then fails it and an answer passes.

## Plan

- `session.feature`, the continued-session scenario: `grep -v -e 'Which word did I ask' -e 'Remember the word
  PAPAYA'` before `grep -q PAPAYA`, and a comment saying neither line counts as the answer.
- **Level:** the acceptance leg on Ubuntu and Fedora, through the Build.

## Acceptance

- The check leaves out both lines, and both legs pass with it.

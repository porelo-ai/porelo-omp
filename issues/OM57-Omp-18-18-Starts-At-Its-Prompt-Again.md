# OM57 — Omp 18.18 Starts At Its Prompt Again

```json
{
  "$schema": "issue-1.json",
  "id": "OM57",
  "title": "Omp 18.18 Starts At Its Prompt Again",
  "state": "Implementing",
  "size": "S",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "an attended omp task on the pinned 18.18.1 starts at its prompt, not at the setup wizard's update step"
}
```

**Why this size.** One value in `OmpConfigFile`, its unit test and the decision record. The fix is already measured;
what costs time is one acceptance run on the VM and finding out why the pin's legs passed.

**What must be true.** An attended omp task started on the pinned release opens at omp's prompt and takes its opening
text, with no menu in between.

## How it stands

Measured on 2026-11-12 on the VM `smith`, in a task of porelo main c43062468 with the adapter of `main` (omp 18.18.1):

- The attended task stops at "Setup step 1 of 1 - Keep omp up to date" (update automatically or notify me). The
  opening text never reaches omp. That blocked OM53's re-measure.
- The adapter writes `setupVersion: 2` to `.omp/agent/config.yml`, measured against 18.1.13. In the same container,
  with a copy of that file, omp 18.18.1 shows the update step with `setupVersion: 2` and starts at its prompt with
  `setupVersion: 3`. So 18.18 added a setup step and raised its version to 3.
- `startup.checkUpdate: false` stays; the wizard asks regardless.

## Plan

- **`OmpConfigFile`**: `setupVersion: 3`, with the measurement against 18.18.1 in its comment.
- **Tests, red first:** `OmpContainerSetupTest` expects `setupVersion: 3`.
- **`doc/decisions.md`**, "omp starts at its prompt: the setup wizard is marked done": the version, why it moved,
  and the index row.
- **Level:** unit test, then `session.feature` and `wake.feature` against the VM.

## Acceptance

- An attended omp 18.18.1 task reaches its prompt without being asked anything, on the VM.
- The unit test fails with `setupVersion: 2`.

## To be checked

- Why the acceptance legs of the 18.18.1 pin (#9) passed, though four features start omp attended and expect it to
  reach work without being asked. If the step reads a wizard as work, it needs to fail on one, or the next raised
  setup version lands on `main` the same way.

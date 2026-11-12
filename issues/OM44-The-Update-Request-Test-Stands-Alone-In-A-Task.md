# OM44 — The Update Request Test Stands Alone In A Task

```json
{
  "$schema": "issue-1.json",
  "id": "OM44",
  "title": "The Update Request Test Stands Alone In A Task",
  "state": "Review",
  "size": "S",
  "priority": "Medium",
  "rank": 2,
  "blockedBy": [],
  "covers": "`UpdateRequestTest` runs its scripts in an environment of its own, so a task's `GIT_CONFIG_*` variables are not taken for a leaked header"
}
```

**Why this size.** test code only, no contract: the environment the process helpers build (`UpdateRequestTest`'s own
and `WorkflowSteps.run`, which the other workflow tests share), one test that holds it, and a check of the others
that start a process; nothing in `src/main` or the workflows changes.

**Estimate.** S: 27 min, the measured median of S over all repositories (n=167).

**What must be true.** `UpdateRequestTest` passes the same way on a developer's machine, on the forge's runner and in a
Porelo task: its scripts run with an environment the test builds itself, so git configuration the caller passes
through `GIT_CONFIG_COUNT`, `GIT_CONFIG_KEY_<n>` and `GIT_CONFIG_VALUE_<n>` (a task does so for the gate's credential)
is not read as a header the workflow leaked.

## Plan

- **Red first:** `UpdateRequestTest` run under Maven with `GIT_CONFIG_COUNT=1`,
  `GIT_CONFIG_KEY_0=http.https://github.com/.extraheader` and `GIT_CONFIG_VALUE_0=x` in the caller's environment, as
  a task passes them, fails on the header it reads as leaked; the same run without them passes.
- The helpers start each process with the caller's environment minus `GIT_CONFIG_COUNT`, every
  `GIT_CONFIG_KEY_<n>` and `GIT_CONFIG_VALUE_<n>`, and `GIT_CONFIG_PARAMETERS`, then the test's own variables; one
  function builds that environment, and a unit test hands it a caller's map holding them and expects none back.
- **Level:** unit tests; the run above with the variables set then passes, as does `verify` without them.

## To be checked

- Which other tests in this repository start a process with an inherited environment that git reads: only
  `WorkflowSteps.run`, which every step's test goes through, and `UpdateRequestTest`'s own `git`; both now take
  `WorkflowSteps.environment`. `PublishTokenTest`, `SnapshotTokenTest` and `WorkflowCommitsTest` clear the environment
  before they set theirs, and the `*Check` classes start `readelf` and the built package, not git. With the caller's
  `GIT_CONFIG_COUNT`, `GIT_CONFIG_KEY_0` and `GIT_CONFIG_VALUE_0` set as above, all 188 tests pass, as they do without
  them.

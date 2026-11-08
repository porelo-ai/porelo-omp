# OM44 — The Update Request Test Stands Alone In A Task

**Status:** Backlog.

**What must be true.** `UpdateRequestTest` passes the same way on a developer's machine, on the forge's runner and in a
Porelo task: its scripts run with an environment the test builds itself, so git configuration the caller passes
through `GIT_CONFIG_COUNT`, `GIT_CONFIG_KEY_<n>` and `GIT_CONFIG_VALUE_<n>` (a task does so for the gate's credential)
is not read as a header the workflow leaked.

## To be checked

- Which other tests in this repository start a process with an inherited environment that git reads.

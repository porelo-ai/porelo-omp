# OM26 — Read The Code Changed Since The Last Skills Review

**Status:** Analyzed.
**Size:** M - 30 files read against seven skills, each finding fixed with its own test; no
contract another repository reads.

**What must be true.** The code changed since this repository was last read against its skills, on
2026-09-27, has been read against them as well, and the versions read are on record.

## Why

The reading of 2026-09-27 went through the whole repository against the seven skills `AGENTS.md` names
and was closed in `94a8883` (on `pre-public`, since the history was squashed into `main`). Everything
written after that was never read against them, and that reading did not record the version and SHA-256
of the skills it read, although its acceptance asked for both. The shared rule now says a repository's
skills are applied, not only named.

## Plan

- **Fetch the seven skills** - `graal`, `java-code-review`, `test-quality`, `security-audit`,
  `concurrency-review`, `clean-code`, `solid-principles` - in their latest version from
  `skills.json`, as *Skills* in `AGENTS.md` says, each checked against the SHA-256 its storage
  reports, and load them in the harness (asked of the harness, not read from a directory).
- **Scope:** `git diff --name-only 94a8883 main -- src pom.xml`, taken again on the day the
  reading starts. Today that is 30 files (30 files changed, 1600 insertions(+), 399 deletions(-)):
  - production and build: `pom.xml`, `OmpAgent.java`, `OmpConfigFile.java`, `OmpContainerSetup.java`, `OmpJsonFormatter.java`, `src/main/resources/agent/omp.yaml`;
  - 19 unit test files and 4 acceptance files under `src/test` and
    `src/acceptance`.

  Files unchanged since `94a8883` were read on 2026-09-27 and stay out.
- **Read each file against each skill.** A finding is a guess until a test reproduces it: it is fixed
  with a test seen to fail on the unfixed code, or declined with the reason written here, or opened
  as its own issue when it is larger than this one or belongs to another repository.
- **A fix to what ships** gets its changelog entry in the same commit; the native build and the
  acceptance suite run as for any change to the adapter.
- **Red first:** each fix's test, run against the code before the fix.

## Acceptance

- The seven skills listed here with the version read and its verified SHA-256.
- Every file in scope read; each finding fixed with a test seen red first, declined here with its
  reason, or opened as an issue named here.
- Unit tests, the native build and the acceptance suite green on the commit handed over.

## To be checked

- Whether the workflows under `.github/workflows` changed since `94a8883` are read as well, against
  `security-audit`: the reading of 2026-09-27 left them out, and they hold the release and update jobs.

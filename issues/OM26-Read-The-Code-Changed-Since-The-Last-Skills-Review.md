# OM26 — Read The Code Changed Since The Last Skills Review

**Status:** Review.
**Size:** M - 30 files read against seven skills, each finding that can do harm fixed with its own
test; no contract another repository reads.

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
- **Fetched on 2026-10-17**, each SHA-256 matching what `agent-skills` reports:
  - `graal` 2026.910.3, SHA-256 `b24ec6272fb13490e32977c21d4543869a7e85ed474fdc0ff34022fbddd88bf6`
  - `java-code-review` 2026.906.3, SHA-256 `802f86339442ed9fa9d5bca14b6d16e1f6420ae92a047b18bba11239e32c698b`
  - `test-quality` 2026.906.3, SHA-256 `9c5d23ad7a6d9c6f5c3679f3cc44a7fcc2fb3cc1c0f0d0ba4a2c0f7812428bad`
  - `security-audit` 2026.906.3, SHA-256 `a46fd1be07bd539b955291537dea0f24f65a2f8b15d40d50a6a9098bbd992747`
  - `concurrency-review` 2026.906.3, SHA-256 `3a0aab612f25483a3fe777eb18fbcc434e936b19298708bf88beff8b22d79e56`
  - `clean-code` 2026.906.3, SHA-256 `5cf777a24630298b4b801c934f599722978c21aeac53d61683243908fc041c7b`
  - `solid-principles` 2026.906.3, SHA-256 `bd47cdab9126dde76cf1703472167a1c88c85306a4e0263a8f2db4c462740cf5`
- **No interface another repository reads changes:** the manifest's fields, the packages' names and paths,
  the service file, the guide written into a task. A finding that would change one is named in the channel,
  not built.
- **`persist-credentials: false` on every `actions/checkout`**, as decided for every repository on
  2026-10-17. Its red check is the shared `check-actions` from Agent Core's tools snapshot, run on today's
  workflows. The checkouts in `build.yml` and `shared-rules.yml` push nothing.
- **`update.yml` pushes its branch with git** (`git push --force origin "$BRANCH"`, :232), today through
  the write token the checkout leaves in `.git/config` for the whole job, the build and both acceptance legs
  included. The operator allowed the change on 2026-10-17: its checkout gets `persist-credentials: false`
  as well, and only the push step gives the token, never on a command line. In that one step the shell
  builds `x-access-token:<token>` in base64 with `printf` (a builtin) into `base64` on stdin, masks it with
  `::add-mask::`, and exports it as `GIT_CONFIG_COUNT=1`, `GIT_CONFIG_KEY_0=http.https://github.com/.extraheader`
  and `GIT_CONFIG_VALUE_0=AUTHORIZATION: basic <value>` for the `git push` after it; nothing goes to
  `$GITHUB_ENV`, so no later step holds it. `gh pr create` keeps `GH_TOKEN`. **Still to be seen:** only
  the next real update run pushing its branch proves it.
- **Scope:** `git diff --name-only 94a8883 main -- src pom.xml`, taken again on the day the
  reading starts. Today that is 30 files (30 files changed, 1600 insertions(+), 399 deletions(-)):
  - production and build: `pom.xml`, `OmpAgent.java`, `OmpConfigFile.java`, `OmpContainerSetup.java`, `OmpJsonFormatter.java`, `src/main/resources/agent/omp.yaml`;
  - 19 unit test files and 4 acceptance files under `src/test` and
    `src/acceptance`.

  Files unchanged since `94a8883` were read on 2026-09-27 and stay out.
- **Read each file against each skill.** A finding that can do harm is a guess until a test
  reproduces it: it is fixed with a test seen to fail on the unfixed code, or declined with the reason
  written here, or opened as its own issue when it is larger than this one or belongs to another
  repository. A finding that names no harm, and only reads wrong, is fixed without a test.
- **A fix to what ships** gets its changelog entry in the same commit; the native build and the
  acceptance suite run as for any change to the adapter.
- **Red first:** each guarding fix's test, run against the code before the fix.

## Acceptance

- The seven skills listed here with the version read and its verified SHA-256.
- Every file in scope read; each finding that can do harm fixed with a test seen red first, each
  that only reads wrong fixed without one, or declined here with its reason, or opened as an issue
  named here.
- Unit tests, the native build and the acceptance suite green on the commit handed over.

## Findings, read on 2026-10-17

Read on `b51d539` with the skills above, `SKILL.md` read from outside the repository. **The scope** is 28 files whose
content changed beyond the rename to Porelo since `94a8883`: `pom.xml`, `src/deb/control/control`, `OmpAgent`,
`OmpConfigFile`, `OmpContainerSetup`, `OmpJsonFormatter`, `omp.yaml`, the four features `credential`, `session`,
`waiting`, `wake`, and 17 unit test files. The workflows were read against `security-audit` too.

Each finding that can do harm is a guess until its test is seen red; A1, B4 and B6 name none and are fixed
without a test. None is fixed before [OM28](README.md#om28).

**What ships:**
- **A1, `OmpJsonFormatter.cut`: the cut at 300 characters can split a surrogate pair.** It only reads wrong: cut on a
  code point, no test.
- **A2, terminal control characters pass through the formatter** (found in porelo-pi's reading, the same shape here).
  Red: `"a\u001b[2Jb"` formats to text with no ISO control character, through `format` and `ended`.

**Tests that cannot fail, or prove less than they say:**
- **B1, `ReleaseChannelTest`:** output read to its end before `waitFor(30 s)`. Red: `sleep 120` fails within ~35 s.
- **B2, `WorkflowRulesTest`:** the empty-key guard matches text; the package-build scans assert no build found.
  Red: the step run with an empty key; `-Pdist,native -DskipTests` green today.
- **B3, `WorkflowTimeLimitTest`:** lower-case job ids only, no set of jobs asserted. Red: a `Publish:` job.
- **B4, `OmpJsonFormatter`'s `willRetry` branch has no test**; deleting it fails nothing. It only words a line, and
  `ended` already has its own test for a retried end, so it gets none; its comment, split by the reformat, is
  rejoined.
- **B5, `credential.feature`, the mailbox scenario:** its grep also matches the `agent_end` line, which carries
  every tool result, so a model that lists `/run/porelo` passes it without the guide. Red: a log whose only mention
  of the path is a tool result inside `agent_end`. Fix: only the assistant's text blocks, and no tool run at all.
  The step's script was run on three fixture logs: the old grep passes a tool's result and the agent's thinking,
  the new one only the answer. **Still to be seen:** the scenario on the VM against a real run.
- **B6, `NoOldNameTest` runs git with the machine's global configuration.** Fix: `GIT_CONFIG_GLOBAL=/dev/null`, no
  test.
- **To be measured:** whether `session.feature`'s attached scenario passes without `--resume`, if omp's welcome box
  lists recent sessions; it is trusted once seen red with the resume flag broken.

**Workflows, against `security-audit`:**
- **W1, `update.yml`: a killed step opens a "Green" pull request.** Every gate reports by `|| echo STOP=...`; a step
  ended by its `timeout-minutes`, the job's limit or a cancel writes none. "Open the pull request" and "Merge, or say
  why not" run under `always()` and decide on STOP alone, so the body says **Green** and, with
  `PORELO_UPDATE_AUTO_MERGE=true`, an unverified pin is merged. Fix: both steps treat anything but the job's success
  as STOP. Red: the two steps' scripts run with STOP unset, a failed status given and a `gh` stub on `PATH`; no
  `gh pr merge`, no "Green".
- **W2, `build.yml`: "A published release is never replaced" fails open.** `curl -sfL … | grep -qF` sits in an `if`
  under `set -eux` without `pipefail`, so a failed fetch reads as "not published" and `jf rt upload` overwrites the
  release. Fix: fetch to a file, stop on curl's failure, then look. Red: the step's script with `CHANNEL=releases`, a
  `curl` stub exiting 22 and a `jf` stub recording its calls; non-zero exit, no upload.
- **W3, `build.yml`: "The package is indexed" greps the package name without its version**, so after the first
  publish it cannot fail, against the shared rule that the index shows the exact version. Red: a stub index holding
  only an older version makes the step exit 1.
- **W4, `build.yml`: `${{ github.event_name }}` and `${{ github.ref }}` are interpolated into a shell line.** A
  pushed branch named `x$(cmd)` runs `cmd`; it needs push access and the step holds no secret, so it is hardening.
  Fix: through `env`, as `update.yml` already says. Red: the step's script with the expressions replaced as the
  runner does, and a ref `refs/heads/x'$(touch ran)'`; no file `ran`.
- **W7, `persist-credentials`** and the push through the environment, as planned above. Red: `check-actions` on
  today's workflows, five checkouts; and the request step run against a local remote, its `git push` seen without
  the header. Its test also sees that no later `gh` call holds the header and that `.git/config` never does.

**Declined:** `format` and `ended` in one class with two scans for the last assistant message (one event schema,
both tested); the broad catch around `Json.parse` (documented); qualified regex names; a fixture's wording already
measured by `credential.feature`. `graal` and `concurrency-review` found nothing.

**For Porelo, named in the channel, not built here:** Porelo reads the last 64 KiB of `task.log` for how a run ended
and drops the first, cut line (`AgentEnding.TAIL`). omp's `agent_end` is one line carrying every message of the run,
so a run whose tool output passes 64 KiB never says how it ended.

**Size:** 13 fixes: nine with a unit test seen red, B5 a scenario checked on fixture logs, A1, B4 and B6
without a test; M holds.

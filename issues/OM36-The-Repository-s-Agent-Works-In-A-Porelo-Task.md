# OM36 — The Repository's Agent Works In A Porelo Task

**Status:** Implementing; blocked by [OM35](README.md#om35), in Review.
**Size:** S - no code here beyond `porelo.yml`, the move itself done by the procedure; about 2 hours of the agent's
work, plus the operator's attended start of the task.

**What must be true.** `porelo-omp`'s agent works in a Porelo task of the project `porelo` on the move's host, reached
in the project's room under the repository's name, and works this issue from start to Review inside that task with
the local forge's build green.

One agent per repository, each move checked before the next, as the project's procedure for an agent's move lays out.

## Plan

Read in today's `porelo.yml`, `settings.xml` and agent definition, porelo's `doc/project-file.md` (*A repository's own
image*) and `doc/running.md` (the agent's layer), and porelo-project's `project.yml`.

- **The image:** today's `porelo.yml` holds the pinned GraalVM, shfmt and the build tools, and `mvnw` brings
  Maven, so the unit suite and the native packages build in the task as porelo-codex's did in a real task. The agent's
  CLI comes from its package's own layer, not from `porelo.yml`. The acceptance legs do not run in a task (no container
  inside it, no rented machine); they run in the forge's Build ([OM35](README.md#om35)).
- **What the task reaches while it runs:** Maven Central, Sonatype or the forge's registry, `fuinorg.jfrog.io` for
  the skills, `raw.githubusercontent.com` for `check-citations`, and the forge's address as a host service. That
  egress is the project's, in porelo-project's `project.yml`; the image, the agent's CLI with it, is built on the host outside the
  task's firewall. The task's build reads Sonatype's snapshots: the forge's registry refuses a task today (the organization is private), and nothing sets
  `MAVEN_SNAPSHOTS_URL` in a task yet, so the `forge` profile stays off there until both are in.
- **The sub-agent reading:** no sub-agent CLI is in the task's image, since a project file names one
  agent per repository and porelo describes no second sign-in inside a task. Until Porelo has a task that reviews, an agent outside the task reads the pull request's diff against the skills, as today's sub-agent reading does, and posts its
  findings in the room.
- **The CLI:** Claude Code runs the agent in the task, as it runs this repository's agent today: woken by a
  message, its subscription brokered from the vault. Oh My Pi itself runs it in an issue of its own, afterwards.
- **Steps:**
  0. *A dry run on this machine first*, only the part that needs nobody: the task's image built from
     `porelo.yml` with podman, and inside it, as a user other than root on an empty local Maven repository,
     `./mvnw -B -s settings.xml verify` and `./mvnw -B -s settings.xml -Pnative,dist verify` on a fresh clone.
     No task starts here and nothing goes through a gate: the room, the wake and the gate are shown on the
     rented host directly. Passed: the image built in 2 min; `verify` ran 129 tests,
     none failed or skipped, in 42 s; the native packages, `.deb` and `.rpm`, built and checked in 79 s.
  1. *Before, from this machine:* a dry run of the task start on the host names each image key's source as
     `porelo.yml`; the image builds; inside it, `./mvnw -s settings.xml verify` and the native packages pass.
  2. *porelo-project (the coordinator):* `repositories.omp.agent` in `project.yml`, no nickname, since the key
     `omp` gives the room's name, and the upstream switched to the host's forge, as the project's procedure for an agent's move lays out.
  3. *The start:* the operator starts the task attended; inside it the agent takes this issue to Review, its push
     through the gate to `porelo/<task>`, the forge's Build read through the `forgejo` reader.
  4. *After the check:* the session on this machine gives up `porelo-omp`;
     its test accounts stay with the repositories not moved yet.
- **Red first:** the move's check from the project's procedure for an agent's move, for
  `porelo-omp` before the move, each line failing by name.

## Acceptance

- The task's image is built from `porelo-omp`'s `porelo.yml`, with no sub-agent CLI in it.
- The task follows `porelo-omp` through the gate; its pushes reach the local Forgejo, and it reads its builds through the
  `forgejo` reader.
- Its build runs on the local forge as on the central one, [OM35](README.md#om35).
- The room shows it as `Agent Omp`, from the capitalized key `omp` in porelo-project's `project.yml`.
- The build in the task passes on Sonatype's snapshots; it takes them from the local forge's registry only once that registry lets a task read it and the task sets `MAVEN_SNAPSHOTS_URL`, which is not this move's.
- Claude Code works in the task with the operator's subscription from the vault.
- The move's check is red for `porelo-omp` before the move and green after it; the agent's session on this machine and its
  test accounts are no longer used, and the way back the project's procedure for an agent's move describes is open.

## Findings from the trial

- **What ran, each as its own step:** the tests tagged `documents` passed (5 tests); the full `verify` failed on
  one of 129 tests; the native and dist packages failed on the same test, and passed (129 tests, none failed or
  skipped, `.deb` and `.rpm` built, about 90 seconds) once the task's `GIT_CONFIG_*` variables were unset.
- **The suite does not stand alone in a task:** the task sets git's configuration through `GIT_CONFIG_COUNT`,
  `GIT_CONFIG_KEY_<n>` and `GIT_CONFIG_VALUE_<n>` for the gate's credential, and the `gh` stub of `UpdateRequestTest`
  reads `GIT_CONFIG_VALUE_0` from the inherited environment and takes it for a leaked header. The test passes with the
  variables unset; [OM44](README.md#om44) builds its environment from a clean one.
- **Egress:** Maven Central and the skills' host are reachable and all seven skills passed their sha256; the host
  `raw.githubusercontent.com` is not, so `check-citations` cannot run in a task and is left to the forge's
  *Shared rules check*. The build resolved `porelo-parent` 0.2.1-SNAPSHOT, which in a task only Sonatype's
  snapshots hold, so that route was used; no separate check of it ran.
- **Context:** the brief names the agent, the room in place of the channel, the operator's language and time zone,
  that skills are read as files, that the checks run before a push, and that a fix is an amend pushed with `--force`
  to the task's own branch. The checkout has a `porelo` remote and no `origin`, so `origin/main` reads as
  `porelo/main`; no `forgejo` reader is in the task, so builds are read from the operator's relay.
- **The local forge's build:** both the Build and the *Shared rules check* stopped at their first Maven call, since
  the parent pom's snapshot is not in the local forge's registry, so no check or test ran there. Either can be green on
  the local forge once the forge reads the Porelo snapshots its registry does not hold from an upstream snapshot
  repository.
- **The forge's copy:** the task's remote answered fetch and listing, so the forge holds a copy of the repository.
- **Commit trailer:** the task's commit hook ends the message with `Assisted-by: Claude Code, <model>` taken from
  the session and removes a Claude `Co-Authored-By` line.
- **Skills folder:** it did not exist in the task and was created by the unpacking.

## To be checked

- That the forge's Build and *Shared rules check* pass on this issue's commits, once the forge reads the Porelo
  snapshots its registry does not hold.
- That the task reads its builds through the `forgejo` reader; in the trial none was in the task.
- That the project's egress lets the task reach `raw.githubusercontent.com`, which `check-citations` reads.
- That the outside reader can fetch `porelo/<task>` from the host's forge, not only from GitHub.
- The way back, rehearsed once: the task's open work fetched from the host's forge into the checkout here.
- That a revoked sign-in is renewed with `porelo vault login claude --force` and a stop and start of the task: a
  running task keeps the credential it started with.
- That the outside reader may join the room from its harness; otherwise it posts its findings in the channel and the
  operator relays them.

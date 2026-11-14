# Issues

What is open in this repository, grouped by state in the order of [workflow.yml](workflow.yml); a group
with nothing in it is left out. The index is generated from the issues' heads and never edited by hand.
Within a group, the order is the priority. The number is identity, not sequence.

## Idea

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om55"></a>[OM55](OM55-The-Agent-s-Own-Telemetry-Goes-To-Porelo.md) | - | - | the adapter declares Oh My Pi's own OpenTelemetry export pointed at Porelo's local receiver in the task, with every switch that would carry content - prompts, tool arguments and output, responses - set explicitly off |
| <a id="om56"></a>[OM56](OM56-The-Update-Job-Keeps-Its-First-Stop.md) | S | - | the weekly update job's major-version step no longer overwrites an earlier gate's STOP, the verified tree is named only after both legs passed, and a week that pins nothing still checks the pinned digest |
| <a id="om46"></a>[OM46](OM46-The-Acceptance-Legs-Run-Side-By-Side.md) | - | - | a Build's acceptance legs run at the same time, two machines held at once instead of one, the Build done about one leg sooner |
| <a id="om34"></a>[OM34](OM34-The-Shared-Workflow-Tests-See-Every-Refusal-Red.md) | - | - | the shared workflow tests see every publishing and channel refusal red, and the refused-key scenario runs in CI |

## Accepted

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om42"></a>[OM42](OM42-Mutation-Testing-With-PIT.md) | - | - | mutation testing by hand: a `pit` profile outside CI with no threshold, and a report sorting survivors into real gaps and harmless ones |

## Analyzed

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om19"></a>[OM19](OM19-Declare-A-Thinking-Level-And-A-Read-Only-Mode.md) | S | - | `--thinking` on the five effort levels and a `read-only` mode without edit tools, for a reviewer | 0 |

## Estimated

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om47"></a>[OM47](OM47-The-Clean-Up-Says-When-Nothing-Was-Rented.md) | XS | - | a leg that failed before resolving the machine tooling rented nothing, and its clean-up says so instead of failing on a missing class | 0 |
| <a id="om48"></a>[OM48](OM48-The-Tests-Follow-The-Pyramid.md) | S | - | the suites by level with measured times, no flow of the adapter only above the unit level; five end-to-end scenarios that test only Porelo leave the legs, and the timeout test waits 100 ms | - |
| <a id="om43"></a>[OM43](OM43-Depend-On-The-Agent-Protocol-Porelo-Provides.md) | S | - | the agent contract named at install time | 0 |

## Backlog

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om18"></a>[OM18](OM18-Prove-GitHub-Copilot-By-Hand-Before-A-Release.md) | S | - | the Copilot path proven by hand before a release | - |
| <a id="om37"></a>[OM37](OM37-The-Acceptance-Legs-Run-On-The-Host-s-Forge.md) | S | [OM35](README.md#om35) | the acceptance legs on the host's pool again, once it lends a forge job a machine by kind | 0 |
| <a id="om59"></a>[OM59](OM59-The-Word-Check-Skips-The-First-Turn.md) | XS | - | session.feature's check that the continued omp session knows the word leaves out the first turn's own line, so that line on the screen is never taken for the answer | - |

## Review

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om62"></a>[OM62](OM62-The-Docs-Say-What-The-Code-Does.md) | S | - | every sentence of the user-facing documentation checked against the code on main, and each one the code does not bear out fixed | - |
| <a id="om51"></a>[OM51](OM51-The-Adapter-Reports-Each-Tool-Call-With-Its-Time.md) | M | - | the adapter reads each tool call from its agent's output and hands Porelo its tool, kind, start, end, a shell call's command line, whether it ran in the background, and a sub-agent's name and the calls made under it, in Porelo's common entry | 0 |
| <a id="om53"></a>[OM53](OM53-An-Attended-Session-Takes-An-Opening-Text.md) | XS | - | an attended session of Oh My Pi opens on the person's opening text as its first turn and then waits for the person, the form (positional) declared in the agent definition and measured in a task | - |
| <a id="om54"></a>[OM54](OM54-A-Finished-Run-Answers-Its-Last-Message.md) | S | - | a finished unattended Oh My Pi run answers the text of its last message, declared in session.waiting.last_message, so a reader's answer is not empty; the declaration adds no screen rule and so changes nothing in how Porelo wakes the agent | - |
| <a id="om57"></a>[OM57](OM57-Omp-18-18-Starts-At-Its-Prompt-Again.md) | S | - | an attended omp task on the pinned 18.18.1 starts at its prompt, not at the setup wizard's update step | - |
| <a id="om60"></a>[OM60](OM60-Omp-Is-Installed-Root-Owned.md) | M | - | the omp binary a task starts is installed by root and owned by root, so the agent in a task cannot replace what Porelo starts as omp next time; its version stays pinned by the definition and moves only through the update job, and a self-update inside a task fails | - |
| <a id="om61"></a>[OM61](OM61-A-Leg-Installs-What-Its-Run-Built.md) | S | - | every acceptance leg of a run that publishes nothing - a dispatch, a tag - installs the packages that run built, so its result speaks for its own commit; only a push to main, which publishes its snapshot first, installs from the package repository | - |
| <a id="om36"></a>[OM36](OM36-The-Repository-s-Agent-Works-In-A-Porelo-Task.md) | S | [OM35](README.md#om35) | the repository's agent moves into its own Porelo task, after a dry run on this machine | - |
| <a id="om44"></a>[OM44](OM44-The-Update-Request-Test-Stands-Alone-In-A-Task.md) | S | - | `UpdateRequestTest` runs its scripts in an environment of its own, so a task's `GIT_CONFIG_*` variables are not taken for a leaked header | - |
| <a id="om35"></a>[OM35](OM35-The-Build-Runs-On-The-Local-Forge-As-On-The-Central-One.md) | S | - | the workflows run 1:1 on the local Forgejo and the central forge: snapshots from `MAVEN_SNAPSHOTS_URL`, publishing switched by `PORELO_PUBLISH`, legs asked by kind, green on a local Forgejo | - |

## Finished

| # | Title | What was built |
|---|---|---|
| <a id="om41"></a>[OM41](README.md#om41) | Subagents Only As Many As The Task Allows | omp in a task starts no subagent unless the task allows it: at 0 its recursion depth is 0, which also refuses `agent()` in eval, above 0 its concurrency is the task's number with a depth of 1, and skills are off at every number. |
| <a id="om52"></a>[OM52](README.md#om52) | The Build Takes Its Downloads From A Cache | A Build, an update run and the workflow check take the runner's packages from the Actions cache, each `.deb` checked against the hash its signed apt metadata names before it is installed, and the update job keeps the pinned Oh My Pi binary under its digest, checked again on every hit; an old pin's entries go by themselves. |
| <a id="om33"></a>[OM33](README.md#om33) | Read Oh My Pi's Sign-Ins From Its Database | `porelo vault import omp` takes a sign-in Oh My Pi holds in its database, read read-only with sqlite-jdbc beside a running Oh My Pi, and the findings name each signed-in provider by kind and path, never a value, and say "cannot say" for a schema they do not know. |
| <a id="om49"></a>[OM49](README.md#om49) | Third-Party Notices Ship With The Package | `porelo-agent-omp` carries the license texts of every component compiled into its executable from outside Porelo - SnakeYAML, JSpecify and the GraalVM runtime - in one `THIRD-PARTY-NOTICES` file, named in `copyright` and marked `%license` in the RPM, and `PackageNoticesCheck` refuses a package without it. |
| <a id="om50"></a>[OM50](README.md#om50) | The Acceptance Takes A Task's Container Name From Porelo | The acceptance scenarios name a task by its own name and take its container's name from `porelo task status --json`, so they hold however Porelo spells a container. |
| <a id="om58"></a>[OM58](README.md#om58) | The Issue Heads Are Checked Every Day | The daily shared-rules run checks every issue head and index with `issues check issues`, so a head that breaks the rules turns it red, not only the next push. |
| <a id="om38"></a>[OM38](README.md#om38) | Snapshots Resolved With A Read-Only Token, A Forge Without Its Registry Refused | On a forge other than the central one, every step that runs Maven reads snapshots with the read-only package token `PORELO_PACKAGES_READ_TOKEN` and no step holds the publisher's, and a Build whose `MAVEN_SNAPSHOTS_URL` is empty stops at its first step, naming the variable. |
| <a id="om39"></a>[OM39](README.md#om39) | The Busy Check Waits On A Tool The Scenario Ends | The busy check of the wake scenario no longer depends on how the model schedules its tools: omp runs a loop in the foreground that the scenario itself ends, and the message is sent while it still runs. |
| <a id="om45"></a>[OM45](README.md#om45) | Snapshots Not Published On The Forge Come From Upstream | A Build on a forge whose registry holds only its organization's packages takes the other Porelo snapshots from the anonymous repository `MAVEN_UPSTREAM_SNAPSHOTS_URL` names, the forge's token goes to the forge's registry alone, and a tag still reads no snapshot repository. |
| <a id="om32"></a>[OM32](README.md#om32) | Report What Oh My Pi Finds Of Itself On The Host | The adapter reports Oh My Pi set up on the host by its `agent.db`, which it never opens, with its sign-in "cannot say"; `omp.yaml` names `config_dir`. |
| <a id="om40"></a>[OM40](README.md#om40) | The Sign-In Says How It Is Answered | `omp.yaml` declares `login.flow: device-code`, so a person granting Oh My Pi's GitHub Copilot sign-in from a host is told to confirm the code on GitHub's device page; tested in sprint 6m: passed. |
| <a id="om30"></a>[OM30](README.md#om30) | Read Oh My Pi 18.11's Start Screen As Ready | `omp.yaml` declares `📁 /workspace`, the start of the status line under omp's prompt, as the ready marker in place of the welcome box 18.11 dropped, and the check still fails while the setup wizard is up. |
| <a id="om29"></a>[OM29](README.md#om29) | Take The Legs From A Run That Proved The Same Tree | A Build on `main` asks `porelo-release proven-legs` whether a Build or an update run already proved the pushed tree on both legs within a day and takes those legs instead of renting again, while a tag always runs its own. |
| <a id="om25"></a>[OM25](README.md#om25) | Declare The App Copilot Is Granted Through | `omp.yaml` declares the OpenCode OAuth app (`Ov23li8tweQw6odWQebz`) Oh My Pi signs in with on `github.com` under `login.grants.github-copilot`, the weekly update stops when a new pin signs in with another, and `doc/index.md` names the app and that Enterprise hosts are not offered. |
| <a id="om31"></a>[OM31](README.md#om31) | Publish Without A Downloaded Client | Both Publish steps in `build.yml` upload the packages and their bill of materials through `.github/scripts/artifactory.sh` with `curl`, with no JFrog CLI action or `jf` command left under `.github/`. |
| <a id="om28"></a>[OM28](README.md#om28) | Reformat To The Shared Style | The code is in the shared style, google-java-format in its AOSP variant with YAML and Gherkin at four spaces; `validate` refuses code that is not, and `git blame` with `.git-blame-ignore-revs` skips the reformatting. |
| <a id="om26"></a>[OM26](README.md#om26) | Read The Code Changed Since The Last Skills Review | The code changed since 2026-09-27 read against seven skills, each finding fixed, with a test where it could do harm: no terminal control character reaches a log, and no checkout keeps the job's token. |
| <a id="om27"></a>[OM27](README.md#om27) | Rename The Adapter Porelo | The repository, its artifact, its packages, its Java packages and its documents say Porelo; a documents test keeps the old name to the released part of `CHANGELOG.md`. |
| <a id="om24"></a>[OM24](README.md#om24) | A JSON argument from GLM-4.6 arrives cut off | `doc/index.md` names `z-ai/glm-4.6` as unable to send a message with Oh My Pi, and `z-ai/glm-5.3-flash` as one that can. |

## What lives here and what in Porelo

An agent's work lives in its own repository, so the requirements for this agent are issues here, and
what outlived the met ones is in [`doc/decisions.md`](../doc/decisions.md).
The candidate agents without a repository wait in `porelo-project` until a repository builds them.

## Where the rest of the open work lives

Not everything open is an issue. Accepted risks - exposures that are known, deliberate and not
being removed - are in [`doc/decisions.md`](../doc/decisions.md) with what would change the
answer. A finding that has been answered is neither: it is in the code, with its reasoning.

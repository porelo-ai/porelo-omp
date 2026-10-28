# Issues

What is open in this repository, grouped by state in the order Review, Rework, Implementing, Backlog,
Analyzed, Accepted, Idea, Estimated, Finished and Rejected; a group with nothing in it is left out.
Within a group, the order is the priority. The number is identity, not sequence.

## Analyzed

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om35"></a>[OM35](OM35-The-Build-Runs-On-The-Local-Forge-As-On-The-Central-One.md) | S | porelo-buildtools [BT25](https://github.com/porelo-ai/porelo-buildtools/blob/main/issues/README.md#bt25), [BT24](https://github.com/porelo-ai/porelo-buildtools/blob/main/issues/README.md#bt24), porelo-host [HO12](https://github.com/porelo-ai/porelo-host/blob/main/issues/README.md#ho12), porelo-host [HO27](https://github.com/porelo-ai/porelo-host/blob/main/issues/README.md#ho27), porelo [B306](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b306) | the workflows run 1:1 on the local Forgejo and the central forge: snapshots from `MAVEN_SNAPSHOTS_URL`, publishing switched by `PORELO_PUBLISH`, legs asked by kind | 0 |
| <a id="om36"></a>[OM36](OM36-The-Repository-s-Agent-Works-In-A-Porelo-Task.md) | S | [OM35](#om35), porelo-project [PJ59](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj59), porelo [B298](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b298) | the repository's agent moves into its own Porelo task, wave 7a of the move, after a dry run on this machine | 0 |
| <a id="om19"></a>[OM19](OM19-Declare-A-Thinking-Level-And-A-Read-Only-Mode.md) | S | porelo [B101](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b101) | `--thinking` on the five effort levels and a `read-only` mode without edit tools, for a reviewer ([B172](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b172)) | 0 |
| <a id="om32"></a>[OM32](OM32-Report-What-Oh-My-Pi-Finds-Of-Itself-On-The-Host.md) | S | porelo [B224](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b224), [B225](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b225) | Oh My Pi reported set up on the host by its `agent.db`, its sign-in "cannot say", for [PJ53](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj53) | 0 |

## Idea

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om34"></a>[OM34](OM34-The-Shared-Workflow-Tests-See-Every-Refusal-Red.md) |  | — | the shared workflow tests see every publishing and channel refusal red, and the refused-key scenario runs in CI |
| <a id="om33"></a>[OM33](OM33-Read-Oh-My-Pi-s-Sign-Ins-From-Its-Database.md) |  | [OM32](#om32) | a nice to have: `vault import` and the findings read Oh My Pi's sign-ins from its SQLite database, for [PJ53](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj53) |

## Estimated

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om18"></a>[OM18](OM18-Prove-GitHub-Copilot-By-Hand-Before-A-Release.md) | S |  | the Copilot path proven by hand before a release |

## Finished

| # | Title | What was built |
|---|---|---|
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

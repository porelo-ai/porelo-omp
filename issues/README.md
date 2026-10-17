# Issues

What is open in this repository, grouped by state in the order Review, Rework, Implementing, Backlog,
Analyzed, Accepted, Idea, Estimated, Finished and Rejected; a group with nothing in it is left out.
Within a group, the order is the priority. The number is identity, not sequence.

## Implementing

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om28"></a>[OM28](OM28-Reformat-To-The-Shared-Style.md) | S | — | the code reformatted to the shared style, which the build then checks |
| <a id="om26"></a>[OM26](OM26-Read-The-Code-Changed-Since-The-Last-Skills-Review.md) | M | — | the code changed since 2026-09-27 read against the seven skills |

## Estimated

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om18"></a>[OM18](OM18-Prove-GitHub-Copilot-By-Hand-Before-A-Release.md) | S |  | the Copilot path proven by hand before a release |
| <a id="om19"></a>[OM19](OM19-Declare-A-Thinking-Level-And-A-Read-Only-Mode.md) | S | porelo [B101](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b101) | a thinking level and a read-only mode a start can ask for |
| <a id="om25"></a>[OM25](OM25-Declare-The-App-Copilot-Is-Granted-Through.md) | S |  | the app `vault authorize github-copilot` signs in through for Oh My Pi |

## Finished

| # | Title | What was built |
|---|---|---|
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

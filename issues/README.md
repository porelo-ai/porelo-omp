# Issues

What is open in this repository, grouped by state in the order Review, Rework, Implementing, Backlog,
Analyzed, Accepted, Idea, Estimated, Finished and Rejected; a group with nothing in it is left out.
Within a group, the order is the priority. The number is identity, not sequence.

## Analyzed

| # | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|
| <a id="om19"></a>[OM19](OM19-Declare-A-Thinking-Level-And-A-Read-Only-Mode.md) | S | sokar [B101](https://github.com/sokar-ai/sokar/blob/main/issues/base/README.md#b101) | a thinking level and a read-only mode a start can ask for | — |
| <a id="om25"></a>[OM25](OM25-Declare-The-App-Copilot-Is-Granted-Through.md) | S |  | the app `vault authorize github-copilot` signs in through for Oh My Pi | — |

## Estimated

| # | Size | Blocked by | What it covers |
|---|---|---|---|
| <a id="om18"></a>[OM18](OM18-Prove-GitHub-Copilot-By-Hand-Before-A-Release.md) | S |  | the Copilot path proven by hand before a release |

## Finished

| # | Title | What was built |
|---|---|---|
| <a id="om24"></a>[OM24](README.md#om24) | A JSON argument from GLM-4.6 arrives cut off | `doc/index.md` names `z-ai/glm-4.6` as unable to send a message with Oh My Pi, and `z-ai/glm-5.3-flash` as one that can. |

## What lives here and what in Sokar

An agent's work lives in its own repository, so the requirements for this agent are issues here, and
what outlived the met ones is in [`doc/decisions.md`](../doc/decisions.md).
The candidate agents without a repository wait in `sokar-project` until a repository builds them.

## Where the rest of the open work lives

Not everything open is an issue. Accepted risks - exposures that are known, deliberate and not
being removed - are in [`doc/decisions.md`](../doc/decisions.md) with what would change the
answer. A finding that has been answered is neither: it is in the code, with its reasoning.

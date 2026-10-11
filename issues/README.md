# Issues

What is open in this repository, grouped by state in the order Review, Rework, Implementing, Backlog,
Analyzed, Accepted, Idea, Estimated, Finished and Rejected; a group with nothing in it is left out.
Within a group, the order is the priority. The number is identity, not sequence.

## Analyzed

| # | Status | Size | Blocked by | What it covers | Open questions |
|---|---|---|---|---|---|
| [OM18](OM18-Prove-GitHub-Copilot-In-Acceptance.md) | Analyzed | M |  | the Copilot path proven in CI | 2 |
| [OM24](OM24-A-JSON-Argument-From-GLM-4-6-Arrives-Cut-Off.md) | Analyzed | S |  | a JSON argument from GLM-4.6 arrives cut off | 2 |
| [OM19](OM19-Declare-A-Thinking-Level-And-A-Read-Only-Mode.md) | Analyzed | S | sokar B101 | a thinking level and a read-only mode a start can ask for | 2 |

## What lives here and what in Sokar

An agent's work lives in its own repository, so the requirements for this agent are issues here, and
what outlived the met ones is in [`doc/decisions.md`](../doc/decisions.md).
The candidate agents without a repository wait in `sokar-project` until a repository builds them.

## Where the rest of the open work lives

Not everything open is an issue. Accepted risks - exposures that are known, deliberate and not
being removed - are in [`doc/decisions.md`](../doc/decisions.md) with what would change the
answer. A finding that has been answered is neither: it is in the code, with its reasoning.

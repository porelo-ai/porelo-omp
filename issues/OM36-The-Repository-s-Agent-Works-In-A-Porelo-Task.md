# OM36 — The Repository's Agent Works In A Porelo Task

**Status:** Idea.

**What must be true.** `porelo-omp`'s agent works in a Porelo task of the project `porelo` on the move's host, reached
in the project's room under the repository's name, and works this issue from start to Review inside that task with
the local forge's build green.

From porelo-project [PJ59](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj59), the move's wave 7a: one agent per repository, each move checked before the next.

## Acceptance

- The task's image is built from `porelo-omp`'s `porelo.yml`: the pinned GraalVM, Maven, and the sub-agents the agent
  hands work to, Codex CLI and Antigravity CLI, each signed in from the host's vault.
- The task follows `porelo-omp` through the gate; its pushes reach the local Forgejo, and it reads its builds through the
  `forgejo` reader.
- Its build runs on the local forge as on the central one, [OM35](README.md#om35).
- The room shows it as `Agent Omp`, from the nickname `Omp` in porelo-project's `project.yml`, set once porelo
  [B298](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b298) is published.
- Oh My Pi takes porelo's snapshot from the local forge's registry.
- [PJ59](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj59)'s move check is red for `porelo-omp` before the move and green after it; the agent's session on this machine and its
  VM accounts are no longer used, and the way back [PJ59](https://github.com/porelo-ai/porelo-project/blob/main/issues/README.md#pj59) describes is open.

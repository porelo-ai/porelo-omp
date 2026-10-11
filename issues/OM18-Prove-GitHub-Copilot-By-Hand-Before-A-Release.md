# OM18 — Prove GitHub Copilot by hand before a release

**Status:** Estimated.
**Size:** S - one acceptance scenario, run by hand on the VM, and a step in the release's checklist; no CI
secret and no way into the vault without a person.

**What must be true.** Before a release, a task started with the GitHub Copilot provider is seen to work and
to hold no GitHub token, measured on the VM with the operator's grant.

## Why

The GitHub Copilot path is documented and was measured by hand on 18.4.1, but no scenario runs it. The
credential scenarios use an OpenRouter key. Copilot does not go into CI: no Copilot account's token is a CI
secret.

## Plan

- **The grant, once, by the operator:** `sokar vault authorize github-copilot` in the VM account the
  acceptance runs in, signed in at the terminal; nothing of the account goes into a committed file.
- **A scenario in `credential.feature`'s shape, tagged so CI never runs it:** a task started with
  `--provider github-copilot` answers through the broker, and its environment, its files under
  `/home/agent` and its logs hold no GitHub token. Without the grant it is skipped and says so.
- **Red first:** the same scenario with a token planted in the task's environment fails.
- **Before a release:** `AGENTS.md`'s release steps name the scenario, run on the VM against the build
  that is released.

## Acceptance

- On the VM with the operator's grant, the scenario starts a task with `--provider github-copilot`, which
  answers, and finds no GitHub token in the task's files, environment and logs; a token planted in the task
  is seen to fail it.
- CI never runs it, and the release steps name it.

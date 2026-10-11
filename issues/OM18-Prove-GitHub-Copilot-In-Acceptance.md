# OM18 — Prove GitHub Copilot in acceptance

**Status:** Analyzed.
**Size:** M - one acceptance scenario and a CI secret, but storing a grant without a person may need a
way into the vault Sokar does not offer today.

**What must be true.** A task started with the GitHub Copilot provider works, and holds no GitHub
token, as CI shows on every build.

## Why

The GitHub Copilot path is documented and was measured by hand on 18.4.1, but no scenario runs it. The
credential scenarios use an OpenRouter key, and CI fails when that key is missing.

## Plan

- **A CI secret** holding a GitHub token entitled to Copilot, passed to the acceptance legs beside
  `SOKAR_E2E_OPENROUTER_API_KEY` in `.github/workflows/build.yml`, and to the VM runs the same way.
- **The kit stores it as the `github-copilot` provider's grant** (`oauth-device` in Sokar's
  `providers/github-copilot.yaml`), the way a finished device sign-in would leave it, without a person.
- **A scenario in `credential.feature`'s shape:** a task started with `--provider github-copilot`
  answers through the broker; its environment, its files under `/home/agent` and its logs hold no
  GitHub token. Skipped and said so when the secret is absent, as the OpenRouter scenarios are.
- **Red first:** the same scenario with the token planted in the task's environment fails.

## Acceptance

- A Copilot token is a CI secret, and the acceptance kit puts an `oauth-device` grant into the vault
  without a person.
- A scenario starts a task with `--provider github-copilot` and checks that the task's files,
  environment and logs hold no GitHub token; a token planted in the task is seen to fail it.

## To be checked

- Whose GitHub account holds the Copilot entitlement the CI token comes from, and whether its use in
  CI is acceptable - the operator's.
- How a grant of kind `oauth-device` is stored without the device flow: whether `vault put` takes a
  token for that kind - Agent Core's.

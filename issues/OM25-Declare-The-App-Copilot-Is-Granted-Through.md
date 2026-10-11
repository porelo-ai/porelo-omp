# OM25 — Declare the app Copilot is granted through

**Status:** Analyzed.
**Size:** S - a `login.grants` block in `omp.yaml`, its test, a check in the weekly update and a paragraph
in `doc/`; Sokar's side is built.

**What must be true.** On a vault without a GitHub Copilot entry, `sokar vault authorize github-copilot` with
Oh My Pi installed signs in through the same OAuth app Oh My Pi uses without Sokar, for `github.com` and for
a GitHub Enterprise host, and the agent's documentation says whose app that is.

## Why

Sokar grants a provider with one command
([B132](https://github.com/sokar-ai/sokar/blob/main/issues/base/README.md#b132)), and takes the client id from
the installed agent's `login.grants.<provider>`: one id per host and the app's owner. `omp.yaml` declares
none, so the command refuses as for an agent that does not sign in.

**What Oh My Pi 18.4.1 signs in with,** read in its source
(`packages/ai/src/registry/oauth/github-copilot.ts` at `v18.4.1`, and the same in the pinned binary):
- **`github.com`** (and `www.github.com`, `api.github.com`): `Ov23li8tweQw6odWQebz`, which the source calls
  "the OpenCode OAuth app", chosen for its narrow `read:user` consent.
- **Any other host, a GitHub Enterprise one:** `Ov23ctDVkRmgkPke0Mmm`, which the source calls "the GitHub-owned
  Copilot CLI client", because a private instance does not know the OpenCode app.
- The scope is `read:user` on both, as Sokar's provider grant asks.

## Plan

- **`src/main/resources/agent/omp.yaml`:** a `login.grants.github-copilot` block, read by Sokar's
  `AgentDefinitionReader`. Its `hosts` map `github.com`, `www.github.com` and `api.github.com` to the OpenCode
  id and `"*"` to the Copilot CLI id. Its `owner` names both apps by host, as Oh My Pi's source does, "not
  verified with GitHub".
- **Red first:** `OmpAgentTest` reads the definition through the API and expects that grant, the ids by host
  and the owner; it fails on today's `omp.yaml`.
- **The weekly update** (`.github/workflows/update.yml`) checks that the new pinned binary still names both ids
  beside `read:user`, as `sokar-claude-code` checks its token URL and client id, and stops the pin move when
  one is gone.
- **`doc/index.md`, "Sign in":** the GitHub Copilot paragraph becomes the one command, says whose app is
  granted and where a person revokes it (GitHub's authorized OAuth apps), and drops the hand-made `vault put`.
- **On the VM**, with the adapter installed per account: on an empty vault, `sokar vault authorize
  github-copilot` shows a device code, which proves the client id reached GitHub, and is then left
  unfinished. A full grant needs an account with Copilot.

## Acceptance

- On an empty vault with Oh My Pi installed, `sokar vault authorize github-copilot` starts the device flow with
  the OpenCode app's id for `github.com`; today it is refused.
- The id Sokar would use for an Enterprise host is Oh My Pi's own for it.
- `doc/index.md` names both apps and their owners as Oh My Pi's source gives them.

## Open questions

- For an Enterprise host Oh My Pi uses GitHub's own Copilot CLI app. Should Sokar grant through it as well,
  as Oh My Pi does without Sokar, or declare `github.com` alone?

## To be checked

- The owner is one string per provider (`AgentGrant.owner`, kept as the entry's `client_owner`), while Oh My Pi
  has a different app per host: whether that string names both, or Sokar keeps an owner per host - to ask
  Agent Core.

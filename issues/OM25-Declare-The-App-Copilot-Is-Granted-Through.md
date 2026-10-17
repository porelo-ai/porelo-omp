# OM25 — Declare the app Copilot is granted through

**Status:** Estimated.
**Size:** S - a `login.grants` block in `omp.yaml`, its test, a check in the weekly update and a paragraph
in `doc/`; Porelo's side is built.

**What must be true.** On a vault without a GitHub Copilot entry, `porelo vault authorize github-copilot` with
Oh My Pi installed signs in through the same OAuth app Oh My Pi uses without Porelo on `github.com`, and the
agent's documentation says whose app that is. A GitHub Enterprise host is not offered.

## Why

Porelo grants a provider with one command
([B132](https://github.com/porelo-ai/porelo/blob/main/issues/base/README.md#b132)), and takes the client id from
the installed agent's `login.grants.<provider>`: one id per host and the app's owner. `omp.yaml` declares
none, so the command refuses as for an agent that does not sign in.

**What Oh My Pi 18.4.1 signs in with,** read in its source
(`packages/ai/src/registry/oauth/github-copilot.ts` at `v18.4.1`, and the same in the pinned binary):
- **`github.com`** (and `www.github.com`, `api.github.com`): `Ov23li8tweQw6odWQebz`, which the source calls
  "the OpenCode OAuth app", chosen for its narrow `read:user` consent.
- **Any other host, a GitHub Enterprise one:** `Ov23ctDVkRmgkPke0Mmm`, which the source calls "the GitHub-owned
  Copilot CLI client", because a private instance does not know the OpenCode app.
- The scope is `read:user` on both, as Porelo's provider grant asks.

## Plan

- **`src/main/resources/agent/omp.yaml`:** a `login.grants.github-copilot` block, read by Porelo's
  `AgentDefinitionReader`. Its `hosts` map `github.com`, `www.github.com` and `api.github.com` to the OpenCode
  id, with the OpenCode app as its `owner`, "as Oh My Pi's source names it, not verified with GitHub".
- **No Enterprise host:** Oh My Pi's other id, GitHub's own Copilot CLI app, is not declared, so
  `vault authorize` refuses an Enterprise host as it does any host without an id.
- **Red first:** `OmpAgentTest` reads the definition through the API and expects that grant, the ids by host
  and the owner; it fails on today's `omp.yaml`.
- **The weekly update** (`.github/workflows/update.yml`) checks that the new pinned binary still uses the
  OpenCode id for `github.com` beside `read:user`, as `porelo-claude-code` checks its token URL and client id, and stops the pin move when
  one is gone.
- **`doc/index.md`, "Sign in":** the GitHub Copilot paragraph becomes the one command, says whose app is
  granted (the OpenCode app, as Oh My Pi's source names it, not verified with GitHub), that an Enterprise host
  is not offered, and where a person revokes it (GitHub's authorized OAuth apps); the hand-made `vault put`
  goes.
- **On the VM**, with the adapter installed per account: on an empty vault, `porelo vault authorize
  github-copilot` shows a device code, which proves the client id reached GitHub, and is then left
  unfinished. A full grant needs an account with Copilot.

## Acceptance

- On an empty vault with Oh My Pi installed, `porelo vault authorize github-copilot` starts the device flow with
  the OpenCode app's id for `github.com`; today it is refused.
- An Enterprise host is refused for want of a client id, and `doc/index.md` says it is not offered.
- `doc/index.md` names the OpenCode app and its owner as Oh My Pi's source gives them.

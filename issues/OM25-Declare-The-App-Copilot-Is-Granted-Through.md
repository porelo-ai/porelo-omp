# OM25 — Declare the app Copilot is granted through

**Status:** Idea.

**What must be true.** On a vault without a GitHub Copilot entry, `sokar vault authorize github-copilot` with
Oh My Pi installed signs in through the same OAuth app Oh My Pi uses without Sokar, for `github.com` and for
a GitHub Enterprise host, and the agent's documentation says whose app that is.

## Why

Sokar grants a provider with one command
([B132](https://github.com/sokar-ai/sokar/blob/main/issues/base/README.md#b132)), and takes the client id from
the installed agent's `login.grants.<provider>`: one id per host and the app's owner. `omp.yaml` declares
none, so the command refuses as for an agent that does not sign in.

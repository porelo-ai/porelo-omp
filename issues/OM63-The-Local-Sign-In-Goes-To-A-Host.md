# OM63 — The Local Sign-In Goes To A Host

```json
{
  "$schema": "issue-1.json",
  "id": "OM63",
  "title": "The Local Sign-In Goes To A Host",
  "state": "Review",
  "size": "M",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "on the person's machine the adapter lists each Oh My Pi sign-in it finds - provider, kind, account where known, age and expiry where known, and whether it can be carried, never its value - and hands a chosen one to Porelo, which carries it into a rented host's vault",
  "openQuestions": 1
}
```

**Why this size.** `OmpFindings` and `OmpCredentialExtractor` exist; this extends them. Each route below is read, told apart and tested against a fixture file; no new contract of
the adapter's own, the fields come from Porelo's half.

**Estimate.** M: 53 min, the measured median of M over all repositories (n=113).

**What must be true.** A person with Oh My Pi signed in on their own machine carries that sign-in into a rented host's
vault from the window and is not asked to sign in a second time there. The adapter's half: for each route below it
says provider, kind, the account where the file names one, age and expiry where the file holds them, and whether it
can be carried, with no value in what it says; and taking one hands its value to Porelo through the extractor alone,
never through a file, a command line or a log.

Filed 2026-11-14 on the operator's order, under ST362 "Credentials carried to a host". Agent Core files Porelo's half,
the fields and the carrying by stdin or environment; Agent Frontend files the window's dialog.

## Plan

- `agent.db` in the definition's `config_dir`, read read-only, one live row per provider: an API key, GitHub Copilot's GitHub token, Anthropic's OAuth sign-in with its expiry; each carriable.
- A row a remote broker holds, its refresh `__remote__`: found, not carriable.
- The finder fills the fields Porelo's half adds to a finding; the extractor is the one an import already uses.
- Tests: one fixture per route, each finding's fields asserted, and a test that no finding's text holds any value
  of its fixture. The definition's `config_dir` stays the place read.

## Acceptance

- Each route above is found and said as listed, with its carriable answer, by a test.
- No finding holds a value of its fixture.
- With Porelo's half in, a sign-in found on the person's machine is in the host's vault and a task there runs on it.

## Open questions

- **Two machines on one sign-in.** An OAuth sign-in whose refresh token is replaced at each renewal works on one
  machine at a time: the first to renew signs the other out. Whether a carried sign-in is said to sign the person's
  machine out, or is carried only as a kind that does not rotate, is the operator's call.

## To be checked

- **Porelo's half.** Porelo seals the value it takes through this adapter's extractor, as an import does; the
  listing that shows these facts is still to come from Porelo, and the facts are mapped onto it then.

## What was built

- `OmpSignInFacts` says, for each sign-in `OmpFindings` finds that an import can take, the account (an `oauth` row's
  `email`), the access token's expiry (`expires`, epoch milliseconds; an Anthropic subscription, not Copilot, whose
  GitHub token does not expire, nor a key) and the row's last change (`updated_at`, epoch seconds, set at each write).
  An account with a control character or past 320 characters is none; no token is decoded.
- The row described is the one the extractor takes, the provider's first live row by id, read through
  `OmpAuthDatabase`, which now reads `updated_at` too.
- Carriable is the finding's `importable`, as before: a row a remote broker holds, or one of a provider Porelo does
  not name, has no facts and is not offered.
- The value goes to Porelo only through `OmpCredentialExtractor`, the one an import already uses.
- Tests: `OmpSignInFactsTest`, 9, each route, no account or expiry guessed, and no fixture value in any fact.
- Open until Porelo's half is in: the facts mapped onto Porelo's listing, and the third acceptance point.
- The reading by another model found a row's `updated_at` past what a time holds failing the findings and the
  import, and tests that passed with the account's guards gone; fixed. Noted, not fixed: the facts read the database
  again after the findings, so a row changed in between is described as it is then, and an `api_key` row's or
  OpenRouter's expiry guard has no fixture that would show it gone.


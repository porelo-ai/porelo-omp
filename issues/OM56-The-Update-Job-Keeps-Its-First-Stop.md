# OM56 — The Update Job Keeps Its First Stop

```json
{
  "$schema": "issue-1.json",
  "id": "OM56",
  "title": "The Update Job Keeps Its First Stop",
  "state": "Idea",
  "size": "S",
  "priority": "Medium",
  "rank": 1,
  "blockedBy": [],
  "covers": "the weekly update job's major-version step no longer overwrites an earlier gate's STOP, the verified tree is named only after both legs passed, and a week that pins nothing still checks the pinned digest"
}
```

**Why this size.** one workflow file and its step tests, no Java and no contract; the same fix was made and reviewed in
porelo-agy's update job.

**What must be true.** When the weekly update stops, its pull request and its red end name the first gate that
stopped it. No run that stopped leaves a tree name that a later Build of `main` takes as proven. A week with nothing
to pin, or one whose upstream points below the pin, still checks that the pinned digest is the one upstream
publishes.

## How it stands

Read in `.github/workflows/update.yml` on `main`:

- "The major version moved" runs on `proceed == 'yes' && major == 'moved'`, without `!env.STOP`. A major release
  whose unit suite, pin check, packages or bill comparison failed has that STOP overwritten with "the upstream major
  version moved", so the request hides the failure.
- "Name the tree this run verifies" and its upload run before the key gate and both acceptance legs. A run whose
  key is empty or whose leg fails has already named its tree. The run then ends red, so `proven-legs` should not
  take it, but nothing here holds that.
- A run that pins nothing (nothing newer, too young, or a rollback) runs no check of the pinned artifact at all.

## Plan

- **`update.yml`**:
  - "The major version moved" gains `!env.STOP`.
  - "Name the tree this run verifies" and its upload move after "Acceptance - fedora", still on
    `proceed == 'yes' && !env.STOP`.
  - Two steps for a week that pins nothing, on `proceed != 'yes'` (a rollback's STOP included): the unit suite and
    `check-pin`. They are the same as the porelo-agy job's "Build the artifact checks" and "The publisher digest
    agrees with the pin".
- **Tests, red first:**
  - A step test runs "The major version moved" with STOP already in `GITHUB_ENV` and expects it unchanged. It goes
    red today, because the step's `if:` is read as well.
  - A rule test holds the step order: no tree is named before the last leg.
  - `UpdateRequestTest` gains a gate's STOP with a successful job (no "Green", no merge) and auto-merge without its
    token (fails, no merge), as in porelo-agy.
- **Level:** unit tests over the job's steps. One dispatched run with nothing newer shows the digest check green.

## Acceptance

- With STOP set before it, "The major version moved" leaves STOP as it was; the test fails without `!env.STOP`.
- No step names the verified tree before both legs; the rule test fails with the old order.
- A scheduled run with nothing newer ends green and its log shows `check-pin` passing.

## To be checked

- Whether `proven-legs` reads only a successful job's artifact; if it does, the move is hygiene rather than a fix.

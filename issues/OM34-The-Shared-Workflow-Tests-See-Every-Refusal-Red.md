# OM34 — The Shared Workflow Tests See Every Refusal Red

**Status:** Idea.

**What must be true.** Each refusal of the Build's publishing and channel steps is seen to fail by a test that
runs the step, and the refused-key acceptance scenario runs in CI. One fix is carried byte for byte to every
copy of the shared tests, in porelo-pi, porelo-claude-code and porelo-agy.

## Why

A review of porelo-agy's copies on 2026-10-22 found these gaps, each also in this repository's copies:

- **Three channel refusals are never seen red.** `ReleaseChannelTest` covers only the two tag cases
  ([ReleaseChannelTest.java:25](../src/test/java/org/fuin/porelo/agent/impl/omp/ReleaseChannelTest.java#L25),
  [ReleaseChannelTest.java:39](../src/test/java/org/fuin/porelo/agent/impl/omp/ReleaseChannelTest.java#L39)); its
  runner always supplies the matching tag
  ([ReleaseChannelTest.java:69](../src/test/java/org/fuin/porelo/agent/impl/omp/ReleaseChannelTest.java#L69)).
  The "Which channel" step refuses a tag that is not `v<version>`, a snapshot version on a tag and a release
  version on a branch
  ([build.yml:108](../.github/workflows/build.yml#L108), [build.yml:111](../.github/workflows/build.yml#L111),
  [build.yml:120](../.github/workflows/build.yml#L120)); the release job repeats them at
  [build.yml:326](../.github/workflows/build.yml#L326), [build.yml:329](../.github/workflows/build.yml#L329)
  and [build.yml:338](../.github/workflows/build.yml#L338). A release could be published as a snapshot or the
  other way round.
- **One upload is enough to pass.** `PublishStepsTest` accepts any one `curl ... -T`
  ([PublishStepsTest.java:78](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L78),
  [PublishStepsTest.java:132](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L132)).
  Dropping the .rpm, the bills of materials or the checksum headers stays green, although both publishing
  steps must upload every file
  ([build.yml:155](../.github/workflows/build.yml#L155), [build.yml:368](../.github/workflows/build.yml#L368)).
- **The curl stand-in ignores the URL and the flags.** It records the arguments and handles only `-o`
  ([PublishStepsTest.java:163](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L163)).
  A dropped `-L` or a wrong channel URL stays green in the index read-back
  ([build.yml:179](../.github/workflows/build.yml#L179), [build.yml:391](../.github/workflows/build.yml#L391)).
- **The test supplies the step's environment itself.** It hands `EVENT` and `REF` straight to the script
  ([PublishStepsTest.java:119](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L119)), so
  removing them from the step's `env:` stays green
  ([build.yml:86](../.github/workflows/build.yml#L86)).
- **A timed-out token test leaves its process running.** `PublishTokenTest.run` asserts that its 30 s wait
  succeeded without ending the process when it fails
  ([PublishTokenTest.java:131](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishTokenTest.java#L131)).
  `WorkflowSteps.run` ends the process and its descendants
  ([WorkflowSteps.java:106](../src/test/java/org/fuin/porelo/agent/impl/omp/WorkflowSteps.java#L106)).
- **CI skips the refused-key scenario.** The scenario needs `PORELO_E2E_REFUSED_KEY`
  ([credential.feature:133](../src/acceptance/resources/org/fuin/porelo/agent/impl/omp/acceptance/credential.feature#L133)),
  which `build.yml` never sets; the acceptance leg supplies the real key and the model only
  ([build.yml:272](../.github/workflows/build.yml#L272)). The kit therefore skips it in CI; the same is true
  in porelo-pi and porelo-claude-code.

## Plan

- Close each gap with a test seen red against the step before the fix.
- Put a fixed fake refused key in the acceptance leg's environment, shaped unlike a real key and not a secret.
- Copy the changed files byte for byte into porelo-pi and porelo-claude-code, whose own issue names
  [OM34](README.md#om34), and offer them to porelo-agy.

## To be checked

- Which of the three stand-in gaps (URL, flags, `EVENT`/`REF`) a single stricter stand-in closes at once.

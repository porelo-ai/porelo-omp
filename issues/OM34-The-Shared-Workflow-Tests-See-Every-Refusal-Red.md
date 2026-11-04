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
  ([build.yml:164](../.github/workflows/build.yml#L164), [build.yml:166](../.github/workflows/build.yml#L166),
  [build.yml:175](../.github/workflows/build.yml#L175)); the release job repeats them at
  [build.yml:398](../.github/workflows/build.yml#L398), [build.yml:400](../.github/workflows/build.yml#L400)
  and [build.yml:409](../.github/workflows/build.yml#L409). A release could be published as a snapshot or the
  other way round.
- **One upload is enough to pass.** `PublishStepsTest` accepts any one `curl ... -T`
  ([PublishStepsTest.java:80](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L80),
  [PublishStepsTest.java:132](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L132)).
  Dropping the .rpm, the bills of materials or the checksum headers stays green, although both publishing
  steps must upload every file
  ([build.yml:193](../.github/workflows/build.yml#L193), [build.yml:421](../.github/workflows/build.yml#L421)).
- **The curl stand-in ignores the URL and the flags.** It records the arguments and handles only `-o`
  ([PublishStepsTest.java:167](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L167)).
  A dropped `-L` or a wrong channel URL stays green in the index read-back
  ([build.yml:236](../.github/workflows/build.yml#L236), [build.yml:462](../.github/workflows/build.yml#L462)).
- **The test supplies the step's environment itself.** It hands `EVENT` and `REF` straight to the script
  ([PublishStepsTest.java:119](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishStepsTest.java#L119)), so
  removing them from the step's `env:` stays green
  ([build.yml:141](../.github/workflows/build.yml#L141)).
- **A timed-out token test leaves its process running.** `PublishTokenTest.run` asserts that its 30 s wait
  succeeded without ending the process when it fails
  ([PublishTokenTest.java:131](../src/test/java/org/fuin/porelo/agent/impl/omp/PublishTokenTest.java#L131)).
  `WorkflowSteps.run` ends the process and its descendants
  ([WorkflowSteps.java:106](../src/test/java/org/fuin/porelo/agent/impl/omp/WorkflowSteps.java#L106)).

**The refused-key scenario has its key.** It needs `PORELO_E2E_REFUSED_KEY`
([credential.feature:133](../src/acceptance/resources/org/fuin/porelo/agent/impl/omp/acceptance/credential.feature#L133)),
and `pom.xml` sets that for the acceptance suite, as in porelo-pi and porelo-claude-code; porelo-agy sets it in its
`build.yml` too.

## Plan

- Close each gap with a test seen red against the step before the fix.
- The fixed fake refused key stays as it is in `pom.xml`, shaped unlike a real key and not a secret.
- Copy the changed files byte for byte into porelo-pi and porelo-claude-code, which hold `ReleaseChannelTest` but no
  `PublishStepsTest`, and offer them to porelo-agy.

## To be checked

- Which of the three stand-in gaps (URL, flags, `EVENT`/`REF`) a single stricter stand-in closes at once.

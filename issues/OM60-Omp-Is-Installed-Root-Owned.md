# OM60 — Omp Is Installed Root-Owned

```json
{
  "$schema": "issue-1.json",
  "id": "OM60",
  "title": "Omp Is Installed Root-Owned",
  "state": "Implementing",
  "size": "M",
  "priority": "High",
  "rank": 1,
  "blockedBy": [],
  "covers": "the omp binary a task starts is installed by root and owned by root, so the agent in a task cannot replace what Porelo starts as omp next time; its version stays pinned by the definition and moves only through the update job, and a self-update inside a task fails"
}
```

**Why this size.** M: the install moves from the agent's phase to the root phase of the image, which Porelo's agent
contract does not offer for a verified download today, so it takes one contract change agreed with Porelo; then the
definition, its tests and one check at the pin.

**Estimate.** M: 53 min, the measured median of M over all repositories (p80 114).

**What must be true.** Whatever the agent writes during a task, the next start of omp runs the pinned binary Porelo
built into the image.

- The binary goes to `/usr/local/bin/omp`, mode 0755, owned by `root:root`; nothing named `omp` stays under
  `/home/agent`.
- The version is pinned in `pom.xml` (`agent.cli.version`) with the digest from the release's `SHA256SUMS.txt`, checked
  before the file is installed; it moves only through the update job, outside any task.
- A self-update tried inside a task fails to write over the binary, by design.

## Plan

- Today the definition fetches the binary as an `install.artifacts` entry into the agent's home. Porelo
  renders every artifact after `USER agent`, so the agent owns it.
- Preferred: Porelo's agent contract lets an artifact be fetched as root, rendered with the same digest check before
  `USER agent`; the definition then names `/usr/local/bin/omp`. The pinned artifact stays what
  `porelo agents --supply-chain` and `PinAgreementTest` read. Without that change the definition would write its own
  verified fetch in `as_root` and lose that record, so the contract change comes first.
- Tests, unit level: the definition's install puts the binary under `/usr/local`, owned by root, with no line that
  writes it as the agent (red first against today's definition); `PinAgreementTest` still holds the pin.
- At the pin, in a task: omp starts from `/usr/local/bin/omp`, and a write to it as the agent fails.

## Acceptance

- In a built image, `stat /usr/local/bin/omp` shows `root`, and the agent's user cannot write it or its directory.
- An update tried in a task as the agent fails, and the next start runs the pinned version.

## To be checked

- The contract change for a root-owned artifact, agreed with Porelo's agent in the channel.
- Whether omp writes anything beside its own binary at start, measured at the pin.

# OM33 — Read Oh My Pi's Sign-Ins From Its Database

```json
{
  "$schema": "issue-1.json",
  "id": "OM33",
  "title": "Read Oh My Pi's Sign-Ins From Its Database",
  "state": "Review",
  "size": "M",
  "priority": "Medium",
  "rank": 5,
  "blockedBy": [],
  "covers": "a nice to have: `vault import` and the findings read Oh My Pi's sign-ins from its SQLite database",
  "openQuestions": 0
}
```

**Why this size.** One module: a new `OmpCredentialExtractor`, `OmpFindings` reading rows instead of the file's
existence, the override in `OmpAgent`, the decision in `doc/decisions.md`, and their tests. What makes it a day and
not hours is the SQLite reader in the native executable: a JDBC driver with a JNI library, its native-image
configuration, and a check that the built binary really reads a database. Porelo's side is there already:
`ExtractCredential` with a provider, as Pi's adapter uses it. A reader of the file format of our own instead would
be L (see *Open questions*).

**Estimate.** M: 53 min, the measured median of M over all repositories (n=113).

**What must be true.** `porelo vault import` takes a sign-in Oh My Pi already holds on the host, for the provider asked
for, and the adapter's findings name which providers Oh My Pi is signed in to, by kind and with the path, never a
credential's value. A database the adapter cannot read, or one of a schema it does not know, is a sign-in "cannot
say", never "not signed in".

## Why

Porelo offers `vault import` for what an agent finds of itself; the adapter answers "cannot say" for Oh My Pi's sign-in. A nice to have, with three
things against it:

- **A SQLite reader in the native executable**: a JDBC driver built into the native image, held to `-march=x86-64` and
  the linkage `NativeLinkageCheck` checks, or a reader of the file format for one table.
- **The schema is not a contract**: the table `auth_credentials` in `.omp/agent/agent.db` in the home (`provider`,
  `credential_type`, `data` as JSON, `disabled_cause` marking a tombstone, several rows per provider) is Oh My Pi's
  own and may change in any release.
- **The database is open while Oh My Pi runs**, in WAL mode, so a read must never take a lock Oh My Pi waits on.

The provider asked for comes through Porelo's `ExtractCredential`, as for Pi.

## Plan

- What Oh My Pi keeps, read in its source at 18.12.6 (`packages/ai/src/auth/sqlite-credential-store.ts`), not yet in
  the pinned 18.14.2: `auth_credentials(id, provider, credential_type, data, disabled_cause, identity_key, created_at,
  updated_at)`; `credential_type` is `api_key` (`data` = `{key, source?}`) or `oauth` (`data` = `{access, refresh,
  expires, email?, accountId?, enterpriseUrl?, orgId?, ...}`); a row with `disabled_cause` set is a tombstone; Oh My
  Pi lists the live ones `ORDER BY id`. The table's own version is `auth_schema_version` (id 1): 8 in 18.12.6, 5 in
  an older 16.3 database. The journal is WAL, and on a host measured all rows sat in `agent.db-wal` while `agent.db`
  held a bare 4 KB page: a reader that ignores the WAL sees nothing.
- **Reader**: `org.xerial:sqlite-jdbc` (Apache-2.0), new to this repository and to every Porelo repository's
  build. It ships `META-INF/native-image` with its own `SqliteJdbcFeature`; its Linux x86-64 library needs
  GLIBC 2.3 at most (3.49.1.0, readelf), under the package's floor of 2.34. Opened as
  `file:<path>?mode=ro` with a short `busy_timeout`, never `immutable=1` (that skips the WAL), one `SELECT` of
  the two tables, closed at once. A WAL reader blocks no writer; at worst it holds back a checkpoint for the length of
  one query.
- `OmpAuthDatabase` (package-private): opens the file, checks `auth_schema_version` against the version(s) read in
  the pinned package, and returns the live rows as `(provider, type, Map data)`, or a state `ABSENT`,
  `UNREADABLE`, `UNKNOWN_SCHEMA`, like Pi's `JsonFile`. A value never leaves the package but through the extractor.
- `OmpFindings`: configuration finding as today; then one sign-in finding per provider with a live row, in the order
  Porelo names (`anthropic`, `openai`, `google`, `github-copilot`, `openrouter`, then the rest), each with the path,
  provider, kind (`oauth` -> subscription, except OpenRouter's -> API key; `api_key` -> API key) and importable when
  Porelo has the provider and the row's shape is one it can take. A Copilot row with `enterpriseUrl`, an OAuth row whose
  refresh is the remote broker's placeholder, and a provider Porelo does not have are signed in, not importable. Any
  state other than a readable known schema is one finding "cannot say", as today.
- `OmpCredentialExtractor`: like `PiCredentialExtractor`, filtered on the findings: the provider asked for, or the
  first importable; of several live rows the first by `id`, the one Oh My Pi lists first. Anthropic OAuth carries
  its refresh token, token URL and client id as read in the pinned package; Copilot's GitHub token as
  `access-token`; an API key as `api-key`. A database that cannot be read throws `AgentException`.
- `OmpAgent#credentialExtractor()`, `omp.yaml`'s comment on `config_dir`, `doc/decisions.md` ("What the adapter finds
  of Oh My Pi on the host" and the paragraph saying `vault import` has nothing to read), the changelog, and the third-
  party notices `PackageNoticesCheck` checks (sqlite-jdbc, and SQLite, public domain).
- Red first: `OmpFindingsTest#aSignInInTheDatabaseIsNamedByProviderAndKind`, over a database the test writes in a
  temporary directory with the pinned schema and WAL on, its writer connection still open; today it yields
  "cannot say". Then `OmpCredentialExtractorTest` (provider asked for, none asked, tombstone skipped, unknown schema
  version, broken `data`, a file that is not SQLite).
- Level: unit tests over such databases for all logic. The native binary: one check in the dist build, next to
  `NativeLinkageCheck`, that the built executable answers `ExtractCredential` from such a database, since only the
  binary shows the JNI library loads.

## Acceptance

- With Oh My Pi signed in to Copilot and OpenRouter, the findings name both with path and kind, and `vault import omp`
  for each provider takes its credential; the other provider's row is not touched.
- Oh My Pi running and writing during the read: neither side fails nor waits; seen in a test with an open writer.
- A database of an unknown schema version, a file that is not SQLite, or one that cannot be opened: "cannot say", and
  an import refuses; never "not signed in".
- No finding, log line or error message holds a value from `data`.
- The native executable built with `-march=x86-64` passes `NativeLinkageCheck` and reads a database.

## What makes it uncertain

- The schema moves: version 5 to 8 between 16.3 and 18.12. Each pin bump may make the reader answer "cannot say"
  until the version is read again; the weekly update could check the pinned package's `AUTH_SCHEMA_VERSION`.
- The JNI library is loaded at run time, so `NativeLinkageCheck`, which reads the binary's `NEEDED`, does not see
  it; where the feature puts it (beside the binary or extracted to a temporary directory) decides what the package
  installs and whether `/tmp` with `noexec` breaks it.
- Oh My Pi rotates Anthropic refresh tokens about every 8 hours; once imported, Porelo and Oh My Pi hold one grant,
  and the first to renew may end the other's. Pi's import has the same exposure.
- A reader of the file format of our own would need B-tree pages, overflow pages and the WAL's frames and
  checksums: L, not M.


## Decisions

- The operator, 2026-11-11: the package may carry `sqlite-jdbc` (Apache-2.0). The decision "the adapter never opens
  Oh My Pi's database" is reversed for reading sign-ins.

## To be checked

- Read in 18.18.1 (the pin), in its source and in a database it wrote on the VM: `auth_schema_version` is 8, table
  `auth_credentials` as in 18.12.6, journal WAL. An `api_key` row holds `{key, source?}`, an `oauth` row the
  credential without its type. Copilot keeps the GitHub token as `refresh` (and `access`) with `enterpriseUrl`.
  OpenRouter's sign-in is stored as an `api_key` row. A remote broker's refresh token is `__remote__`. Anthropic renews
  at `https://api.anthropic.com/v1/oauth/token` (not Pi's `platform.claude.com`) with the client
  `9d1c250a-e61b-44d9-88ed-5944d1962f5e`, kept in base64 in `packages/catalog/src/compat/rules/auth/anthropic.kdl`.
- The host's own Oh My Pi is 16.3.11, whose database is at version 5: the findings for it are "cannot say" until a
  version other than the pin's is read and accepted.
- A read opens read-only with a one-second busy timeout and leaves no file beside the database (unit test, with the
  writer's connection open). Oh My Pi writes between reads without waiting (unit test, 20 rounds).
- The native binary loads SQLite's JNI library from a resource. sqlite-jdbc unpacks it to `/tmp` under a UUID name
  (`TMPDIR` does not move it) and removes it on exit. Measured on the host; `/tmp` is not `noexec` on the host or
  the VM. On a `noexec` `/tmp` the import would fail; `org.sqlite.tmpdir` would move it, which nothing sets yet.
  `NativeDatabaseCheck` runs the built binary over such a database in the dist build: it finds the sign-in and
  extracts it. The binary grows from 15 to 19.7 MB. The linkage check is unchanged and passes.
- On the VM `smith` (porelo main c43062468, the adapter built before the `file:` URI change below in the account, 2026-11-12; `NativeDatabaseCheck` ran the final binary): Oh My Pi 18.18.1 stored
  an OpenRouter key and a Copilot sign-in with its own `omp auth import` in a home of their own. `porelo vault import
  omp --config-dir … --provider github-copilot` stored Copilot's GitHub token as `access-token`, `--provider
  openrouter` the 73-character key as `api-key`; without a provider Porelo took Copilot, the first in its order, and
  refused to replace the entry it already held. The Copilot entry was removed after. No library was left in `/tmp`.
  An Anthropic sign-in was read only in unit tests: no real one was at hand.
- Full native dist build: 176 unit tests, `NativeLinkageCheck`, `NativeDatabaseCheck`, `PackageVersionCheck` and
  `PackageNoticesCheck` (which now requires sqlite-jdbc's section) passed.
- Read by a second model (gemini-4-argon-high), which found nothing to change. Asked by the brief whether an unusual
  path breaks the JDBC URL, it said no; a test showed it does: sqlite-jdbc cut `?journal_mode=delete&` out of a
  directory's name and took it as a setting. The database is now opened by a percent-encoded `file:` URI, and
  `aDirectoryWhoseNameHoldsUrlCharactersIsReadAsItIs` holds it.

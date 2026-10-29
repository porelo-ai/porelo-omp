# OM33 — Read Oh My Pi's Sign-Ins From Its Database

**Status:** Idea.

**What must be true.** `porelo vault import` takes a sign-in Oh My Pi already holds on the host, for the provider asked
for, and the adapter's findings name which providers Oh My Pi is signed in to, by kind and with the path, never a
credential's value.

## Why

Porelo offers `vault import` for what an agent finds of itself; [OM32](README.md#om32) answers "cannot say" for Oh My Pi's sign-in. A nice to have, with three
things against it:

- **A SQLite reader in the native executable**: a JDBC driver built into the native image, held to `-march=x86-64` and
  the linkage `NativeLinkageCheck` checks, or a reader of the file format for one table.
- **The schema is not a contract**: the table `auth_credentials` in `~/.omp/agent/agent.db` (`provider`,
  `credential_type`, `data` as JSON, `disabled_cause` marking a tombstone, several rows per provider) is Oh My Pi's
  own and may change in any release.
- **The database is open while Oh My Pi runs**, in WAL mode, so a read must never take a lock Oh My Pi waits on.

The provider asked for comes through Porelo's `ExtractCredential`, as for Pi.

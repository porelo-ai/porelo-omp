# OM64 — The Sign-In Facts' Smaller Findings

```json
{ "$schema": "issue-1.json", "id": "OM64", "title": "The Sign-In Facts' Smaller Findings", "state": "Idea",
  "size": "-", "priority": "Medium", "rank": 1, "blockedBy": [], "covers": "the smaller gaps left in how the adapter finds and describes a local Oh My Pi sign-in for carrying to a host, each fixed or measured" }
```

**What must be true.** What the adapter says of a Oh My Pi sign-in on the person's machine, before it is carried to a
host, holds in the cases below too, and each guard there has a test that shows it gone.

## To be checked

- **The facts read `agent.db` a second time.** The facts read the database again after the findings, so a row changed in between is described as it is then, not as the findings saw it.
- **Two expiry guards have no fixture.** No fixture shows the guard gone that gives an `api_key` row no expiry, nor the one that gives OpenRouter none.
- **The facts on the wire.** Porelo's listing of what can be carried names agent, provider and kind only; the account, expiry and last change stay in the adapter until a listing takes them, and are then handed over.

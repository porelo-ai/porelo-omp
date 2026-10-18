# OM30 — Read Oh My Pi 18.11's Start Screen As Ready

**Status:** Implementing.
**Size:** S - the ready marker in `omp.yaml` and its test, the pin moved to 18.11.0 in the same commit, and the
acceptance on the VM against 18.11.0.

**What must be true.** A task on Oh My Pi 18.11 is seen reaching work without being asked anything, as on 18.4.1,
so the pin can move past 18.4.1; and the check still fails while the setup wizard is up.

## What changed, read on the update run of 2026-10-18

- The update run to 18.11.0 failed the five scenarios that attach: "did not show `Welcome back!` … within 120s".
  18.11's start screen has the logo, the version, a tip and the prompt with its status line
  (`π > … > 📁 /workspace > ⑂ master …`), and no welcome box.
- **The marker is now `📁 /workspace`,** the start of that status line, already half of `session.at_rest`.

## Measured on `ubuntu26.04`

- `Welcome back!` against 18.11.0: "a person's fresh task reaches work" fails, red first.
- `📁 /workspace` with `setupVersion: 2` taken out of the build, 18.11.0: it fails and reports the wizard ("Setup step
  1 of 5 … Select provider to login"), so the marker never passes under the wizard.
- `📁 /workspace` against 18.11.0: the five attaching scenarios pass, 5 of 5.
- `📁 /workspace` against 18.4.1: four of the five pass; "an attached agent stopped and started again continues
  its conversation" failed in some runs, with the agent's answer on the screen and no session left by the stop.
  It failed in 2 of 5 runs with the new marker and in 0 of 4 with the old one, so the marker moves with the pin,
  in one commit, rather than on 18.4.1 first.

## Acceptance

- On the VM, the whole suite against 18.11.0 with the new marker, `PORELO_E2E_EXPECT_CLI` set to it.
- After the push, the Build's legs are green on 18.11.0; the update request #5 is closed as superseded.

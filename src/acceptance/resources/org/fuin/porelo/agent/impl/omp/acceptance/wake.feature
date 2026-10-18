@credential
Feature: An agent at rest is woken by a message for it

    Porelo types one line into a task's session when a message for it arrives and its agent's declared
    at_rest matches the screen. These scenarios leave tasks running while they wait for the agent, so they
    are in a file of their own that runs after every other: a wake that fails, and skips its clean-up, takes
    no other scenario down with it. Without PORELO_E2E_OPENROUTER_API_KEY and PORELO_E2E_MODEL every scenario
    here is skipped.

    Background:
        Given the suite runs as an unprivileged user
        And the environment variable "PORELO_E2E_OPENROUTER_API_KEY" is set
        And the environment variable "PORELO_E2E_MODEL" is set
        # omp marks a tool call that failed with a cross; three of them is the agent retrying, and stops a wait.
        And a tool failure on the screen looks like "✘ "

    @slow
    Scenario: an agent at rest is woken by a message for it, and answers it
        # Porelo types its one wake line into the session when a message for the task arrives and the agent's
        # declared at_rest matches its screen. The message asks for a sum, so the answer is the agent's own, not the
        # message read back.
        Given a vault of this scenario's own, unlocked with the passphrase "scenario-vault-passphrase"
        And the vault holds the value of "PORELO_E2E_OPENROUTER_API_KEY" as "openrouter" of kind "api-key"
        # The wake comes from the daemon's passes, which a leased machine's account does not run by itself.
        And a daemon of this scenario's own
        And a project called "wake" of class "guarded" with a file in it
        And a terminal on the machine
        When I run "porelo task start wake --project wake --repository wake --agent omp --provider openrouter --model ${PORELO_E2E_MODEL} --clearance deny"
        Then the "omp" agent in task "wake" of "wake" reaches work without being asked anything
        When I type "Reply with the single word PONG."
        And I press Enter
        # The model's answer, not the typed line, which has the word in it too.
        Then within 120 seconds this script exits zero:
            """
            podman exec porelo-wake-wake tmux capture-pane -p -t porelo | grep -v 'single word PONG' | grep -qw PONG
            """
        # An answer is on the screen a moment before its turn ends. At rest is what omp.yaml's at_rest reads, so the
        # message below comes to an agent at rest, not to one still finishing its turn.
        Then within 60 seconds this script exits zero:
            """
            screen=$(podman exec porelo-wake-wake tmux capture-pane -p -t porelo)
            printf '%s\n' "$screen" | grep -qF 'π > ' || exit 1
            printf '%s\n' "$screen" | grep -qF '📁 /workspace' || exit 1
            ! printf '%s\n' "$screen" | grep -qF '↑/↓ move · ⎋ cancel'
            """
        When a script runs "echo 'What is 1234 plus 4321? Reply with the digits only.' | porelo talk tell porelo-wake-wake"
        Then it exits zero
        And within 120 seconds the terminal shows "A message for you waits"
        # The guide has a message answered with a message, so the answer counts either way the agent gives it:
        # as its reply, held because this project cannot address the person, or on its screen. The kit stops
        # the wait at once when the agent loops.
        Then within 240 seconds this script exits zero:
            """
            held=$(porelo talk held porelo-wake-wake)
            for id in $(printf '%s\n' "$held" | awk '$1 ~ /\.json$/ && $2 == "held" {print $1}'); do
              porelo talk read porelo-wake-wake "$id" | grep -qE '5,?555' && exit 0
            done
            screen=$(podman exec porelo-wake-wake tmux capture-pane -p -S - -t porelo)
            printf '%s\n' "$screen" | grep -qE '5,?555' && exit 0
            printf 'held: %s\nscreen, last lines:\n' "$held"; printf '%s\n' "$screen" | grep -v '^ *$' | tail -15
            exit 1
            """
        When a script runs "porelo task remove porelo-wake-wake --force"
        And a script runs "porelo project unfollow wake --force"
        Then it exits zero

    @slow
    Scenario: a message that arrives while the agent works wakes it once it is at rest
        # The case a single look at the screen misses: told while it works, the agent is announced the message when
        # its turn has ended, not never.
        Given a vault of this scenario's own, unlocked with the passphrase "scenario-vault-passphrase"
        And the vault holds the value of "PORELO_E2E_OPENROUTER_API_KEY" as "openrouter" of kind "api-key"
        # The wake comes from the daemon's passes, which a leased machine's account does not run by itself.
        And a daemon of this scenario's own
        And a project called "busy" of class "guarded" with a file in it
        And a terminal on the machine
        When I run "porelo task start busy --project busy --repository busy --agent omp --provider openrouter --model ${PORELO_E2E_MODEL} --clearance deny"
        Then the "omp" agent in task "busy" of "busy" reaches work without being asked anything
        When I type "Run the shell command 'sleep 10' with your shell tool, then reply with the single word DONE."
        And I press Enter
        # Working, seen rather than assumed: its shell tool shows the sleep and no result yet, so the message below comes
        # while it works. omp runs a command inside its own process, so no sleep shows among the task's processes.
        Then within 120 seconds this script exits zero:
            """
            screen=$(podman exec porelo-busy-busy tmux capture-pane -p -t porelo)
            printf '%s\n' "$screen" | grep -qF '$ sleep 10' && ! printf '%s\n' "$screen" | grep -qF 'Wall:' && exit 0
            printf 'screen, last lines:\n'; printf '%s\n' "$screen" | grep -v '^ *$' | tail -25
            exit 1
            """
        When a script runs "echo 'What is 1234 plus 4321? Reply with the digits only.' | porelo talk tell porelo-busy-busy"
        Then it exits zero
        # Read on the task's own screen, so a wake not seen shows what omp showed instead of the terminal's stream.
        # A wake is owed only to a message still in inbox/new when the agent comes to rest, which is what Porelo's wake
        # reads. An eager model may read its mailbox while it works and answer there, as DeepSeek V4.1 Flash did once
        # in ten; then nothing waits at rest, no wake is due, and the answer below is still required. While the message
        # waits at rest, only the wake passes.
        And within 240 seconds this script exits zero:
            """
            screen=$(podman exec porelo-busy-busy tmux capture-pane -p -S - -t porelo)
            printf '%s\n' "$screen" | grep -qF 'A message for you waits' && exit 0
            now=$(podman exec porelo-busy-busy tmux capture-pane -p -t porelo)
            waits=$(podman exec porelo-busy-busy sh -c 'ls /run/porelo/mail/inbox/new/' | grep '\.json$')
            rest=no
            printf '%s\n' "$now" | grep -qF 'π > ' && printf '%s\n' "$now" | grep -qF '📁 /workspace' && rest=yes
            printf '%s\n' "$now" | grep -qF '↑/↓ move · ⎋ cancel' && rest=no
            [ "$rest" = yes ] && [ -z "$waits" ] && { printf 'read while it worked, so no wake is due\n'; exit 0; }
            printf 'at rest: %s, waiting: %s\n' "$rest" "${waits:-none}"
            printf 'status:\n'; porelo task status porelo-busy-busy | grep -E '^(state|activity|screen)'
            printf 'screen, last lines:\n'; printf '%s\n' "$screen" | grep -v '^ *$' | tail -25
            exit 1
            """
        # The guide has a message answered with a message, so the answer counts either way the agent gives it:
        # as its reply, held because this project cannot address the person, or on its screen. The kit stops
        # the wait at once when the agent loops.
        Then within 240 seconds this script exits zero:
            """
            held=$(porelo talk held porelo-busy-busy)
            for id in $(printf '%s\n' "$held" | awk '$1 ~ /\.json$/ && $2 == "held" {print $1}'); do
              porelo talk read porelo-busy-busy "$id" | grep -qE '5,?555' && exit 0
            done
            screen=$(podman exec porelo-busy-busy tmux capture-pane -p -S - -t porelo)
            printf '%s\n' "$screen" | grep -qE '5,?555' && exit 0
            printf 'held: %s\nscreen, last lines:\n' "$held"; printf '%s\n' "$screen" | grep -v '^ *$' | tail -15
            exit 1
            """
        When a script runs "porelo task remove porelo-busy-busy --force"
        And a script runs "porelo project unfollow busy --force"
        Then it exits zero

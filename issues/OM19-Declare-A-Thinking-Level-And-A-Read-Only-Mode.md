# OM19 — Declare a thinking level and a read-only mode

**Status:** Analyzed; waits for a definition's thinking-level key under `headless:`, named modes, and a
start's `--mode <name>`.
**Size:** S - entries in `omp.yaml` and their unit and VM tests; the keys a definition may declare, and the start's options for them, are Porelo's.

**What must be true.** A task start can set Oh My Pi's thinking level on Porelo's five effort levels, and can ask
by name for a `read-only` mode in which Oh My Pi has no tool that edits files, without being able to pass any
other flag through.

## Why

Oh My Pi has `--thinking=<level>` and `--tools=<list>`, but `porelo task start` gives an agent only what its
definition declares, and `omp.yaml`'s `headless:` declares only `--model` and the output flags. That is by design:
a start that could pass any flag through could undo `--auto-approve` and the rest the definition holds. A review or check task needs both.

A reviewer delivers its findings like every task: it writes a message in its outbox and renames it, and Oh My Pi
can rename only through its shell. So the `read-only` mode keeps `bash` and takes away the edit tools; it is a
guard for what the agent changes inside its own container, not the reviewer's security. That rests on the host:
the workspace has no push URL, the gate gives a reviewer upload-only access, and a review message is accepted
only for the task's pinned target, with every claim of authority in it refused.

## Plan

The names below were read in Oh My Pi's sources at 18.12.6; the pin is 18.12.1, and they are read again at the
pin when this is built.

- **The thinking level:** once Porelo adds it to `headless:`, `src/main/resources/agent/omp.yaml` names `--thinking`. Oh My Pi's levels
  (`CLI_THINKING_LEVELS`: `off`, `minimal`, `low`, `medium`, `high`, `xhigh`, `max`, `auto`) contain Porelo's five effort levels (`low`, `medium`,
  `high`, `xhigh`, `max`) as they are, so none steps down at the flag; Oh My Pi itself lowers a level a model
  does not support.
- **The `read-only` mode:** once Porelo gives a definition named modes and the start `--mode <name>`, `omp.yaml` declares `read-only` as
  `--tools=read,grep,glob,bash`. Left out are the edit tools `write`, `edit`, `ast_edit`, `memory_edit` and
  `manage_skill`, and the tools a review does not need (`task`, `web_search`, `github`, `lsp`, `eval` and the
  rest of the 31 built-ins).
- **What can widen the list:** a `--tools=` list from the command line does not set Oh My Pi's
  `restrictToolNames`, so settings can add tools to it again - among them a `write` tool limited to the
  session's scratch files (`tools.xdev`, `plan.enabled`), context tools, MCP tools and custom tools from the
  project. None of them gives more than `bash` already does, so the mode does not close them; the unit test
  states which tools the mode names, and the VM test sees that no edit tool is offered.
- **Red first:** `OmpAgentTest` reads the definition and expects the thinking key and the mode with exactly its
  four tools; `porelo agents --verbose` shows both, which the definition test checks through the API. Then, on
  the VM, a task started in `read-only` is asked to edit a file with an edit tool, is seen to have none, and
  delivers a message to its outbox through its shell.

## Acceptance

- `omp.yaml` declares the thinking level under `headless:`, so a start sets each of the five levels the way it
  sets `--model`; the built command is checked for each.
- `omp.yaml` declares `read-only` with `--tools=read,grep,glob,bash`, which a start asks for by name.
- On the VM, a task in `read-only` is offered no edit tool, and still delivers a message through its outbox.

## To be checked

- Whether a prompt that begins with `-` is read by `omp` as a flag: `omp.yaml` declares the prompt positional
  (`prompt_flag: ""`) and nothing places `--` before it. If it can, Porelo's start must close it before
  a mode can hold.
- The tool names and levels at the pin, read again when this is built.

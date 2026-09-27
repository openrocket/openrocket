# LLM contribution guidelines

These instructions apply to AI coding agents and contributors using them in OpenRocket. Read [CONTRIBUTING.md](CONTRIBUTING.md) 
before making changes. The person submitting a PR is responsible for understanding every change, verifying it, and 
responding to review. AI assistance does not replace that responsibility.

## Changes

- Address one concrete problem per PR. Check existing issues and PRs for duplicate work. Discuss new features and broad changes with maintainers before implementing them.
- Read the relevant code and follow existing patterns. Prefer the smallest complete fix. Leave unrelated formatting, renaming, refactoring, and cleanup out of the diff.
- Do not invent requirements or add speculative abstractions, dependencies, fallback behavior, or configuration. Explain any necessary expansion of scope.
- Review the entire diff before submitting. Remove generated clutter, redundant comments, scratch files, and changes you cannot explain. Do not submit batches of speculative fixes or generate follow-up PRs without a concrete need.

## Verification

- Run checks appropriate to the change. For bug fixes, add a regression test where practical that exercises the reported failure. Test behavior, not merely the implementation's own assumptions.
- For UI changes, exercise the affected interaction when possible. For simulation or numerical changes, validate against independent expected results and explain their basis.
- Report only checks actually run and their outcomes. State failures, skipped checks, and environment limitations plainly. Never claim a fix is verified just because the code looks plausible or an LLM says it is correct.
- If AI was used for the PR, including code, tests, or the description, include the template's AI assistance section. An agent drafting or updating the PR must fill it in, naming itself and any other models used. Give the actual model names (for example, "GPT 6 astra"), not just the tool or provider. Use the model identity supplied by the runtime or contributor; if unavailable, write "Unknown (via <tool>)" instead of guessing. Briefly explain what AI helped with and how its output was verified. Do not paste prompts or chat transcripts.

## PRs and review replies

Use the [PR template](.github/pull_request_template.md). Give the PR a specific title and start its body with 
`# Description` and a short summary of what changed and why. Follow the summary with `Fixes #123` only if the PR fully 
resolves that open issue; use `Related to #123` for issues that remain open, or omit the line when there is no issue. 
Keep `# Testing` and include `# AI assistance` whenever AI was used. Aim for under 200 words for routine changes; include 
more only when needed to explain correctness, tradeoffs, or risk.

Write normal, connected prose. Let the editor wrap lines naturally: do not insert hard line breaks within paragraphs or 
put each sentence on a separate line. Avoid stacks of subheadings, nested lists, decorative separators, emojis, and 
boilerplate checklists. A short list is fine when it makes several distinct points easier to read.

Describe the final change, not the agent's work log. Do not enumerate every changed file, repeat the diff, or add 
generic sections such as "Benefits", "Key improvements", or "Summary of changes". Keep review replies direct and 
answer the actual question. Update the description when the scope changes.

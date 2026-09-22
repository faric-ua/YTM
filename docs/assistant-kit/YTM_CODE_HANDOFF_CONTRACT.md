# YTM Importer — `ytm-code` Handoff Contract

This is the canonical phone-side code-delivery contract for YTM Importer.

## Default delivery rule

For substantial repository changes, ChatGPT should deliver an executable
`YTM_*.zip` package for the user's existing `ytm-code` runner instead of pasting
a long multiline shell script into chat.

Normal user flow:

1. ChatGPT prepares a descriptive `YTM_*.zip` package.
2. The user downloads it to the phone's normal Download folder.
3. The user runs:

```bash
ytm-code
```

4. The package performs its own repository checks, applies the intended change,
   runs relevant audits, and prints a clear PASS/FAIL result.
5. The user sends the terminal result back to ChatGPT.

## Required ZIP structure

`ytm-code` requires **exactly one top-level folder** in the ZIP.

Correct:

```text
YTM_v1.4.50_Skin_Wave1/
├── YTM_v1.4.50_Skin_Wave1.sh
└── YTM_v1.4.50_Skin_Wave1.sh.sha256
```

Incorrect:

```text
YTM_v1.4.50_Skin_Wave1.sh
YTM_v1.4.50_Skin_Wave1.sh.sha256
```

The `.sh` and matching `.sha256` must be inside that single top-level folder.

## Package format

A normal package should contain:

- exactly one top-level folder;
- one primary executable `.sh` file inside it;
- a matching `.sha256` file for that executable;
- any additional patch/data files required by the operation, also inside the
  same top-level folder.

The primary mutation script should use:

```bash
set -euo pipefail
```

and fail closed when repository state, branch, base revision, required files or
other assumptions do not match.

## Naming

Use ZIP/package names beginning with:

`YTM_`

Example:

`YTM_v1.4.50_Skin_Wave1.zip`

## Chat behavior

Do not make the user reconstruct large scripts manually from chat when the same
operation can be delivered through `ytm-code`.

The normal chat handoff should be only:

- package download link;
- command `ytm-code`;
- final PASS/FAIL marker to return.

Short diagnostic commands are still acceptable when genuinely simpler, or when
the user explicitly asks for inline commands.

## Safety

A package must not silently:

- use `git reset --hard`;
- use broad `git clean -fd`;
- delete historical release/QA evidence;
- stage unrelated files;
- bypass failing audits;
- invent phone QA or release evidence.

Prefer exact-path staging and explicit repository-state guards.

If a package commits or pushes, it must do so only after validation passes.

## Source of truth

For YTM code-delivery format, this file is authoritative.

If a future assistant is uncertain how to deliver a substantial code change,
the default is:

**prepare a correctly structured `YTM_*.zip` package for `ytm-code`.**

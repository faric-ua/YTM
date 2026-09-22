
# Assistant Kit

This directory turns the project's development method into explicit,
repository-owned knowledge.

## Project-specific layer

- `CONTEXT_FILES.txt` — mandatory context files for code-changing work;
- `AUDIT_CATALOG.md` — generated complete audit inventory;
- `PORTABLE_AUDITS.txt` — curated system audits worth carrying as reference;
- `RELEASE_DOCUMENTATION_CONTRACT.md` — release-documentation gate.

## Portable layer

`portable/` contains a reusable skeleton that is intentionally not tied to YTM
business logic.

It captures:

- assistant/project handoff rules;
- Activity recreation and rotation behavior;
- navigation ownership;
- modal lifecycle;
- form/IME draft preservation;
- remote-operation ownership;
- release documentation;
- test-diagram rules;
- templates for a new project.

## Migration archive

Generate with:

```bash
python -B scripts/export-assistant-project-skeleton.py
```

Or choose an output path:

```bash
python -B scripts/export-assistant-project-skeleton.py \
  --output /storage/emulated/0/Download/YTM-Assistant-Migration-Kit.zip
```

The archive contains a generic portable skeleton and a YTM-specific reference
layer. Generated ZIP files are not committed to the repository.

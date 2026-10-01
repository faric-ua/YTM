# v1.4.55 — Phone Test

Do not install a candidate until the exact HEAD passes release preflight, JVM tests
and unsigned release assemble, then produces a signed APK from that same source.

Install in-place. Do not clear app data.

## Wave A — consolidated UI/lifecycle matrix

Use existing phone data. Do not manufacture remote failures.

### Test A1 — Home / Menu / wording

Expected:
- normal Home state and current workspace remain intact;
- Menu text is understandable without developer terms;
- no action starts merely by opening Menu or rotating.

Result: `A1+` / `A1-`.

### Test A2 — Bulk Preview readability

Expected:
- playlist title, state badge, selection, compact counters and planned mutation are scannable in that order;
- reason and Search/cache/API diagnostics are visibly secondary;
- raw internal enum names do not dominate primary UI;
- selecting/excluding rows updates the plan summary but does not start remote work;
- portrait/landscape rotation preserves plan/selection;
- verify Neon plus at least one alternate skin.

Result: `A2+` / `A2-`.

### Test A3 — Bulk Session readability + safety

Expected:
- current state, primary result counters, attention/remaining work and next safe action are obvious;
- checkpoint/baseline/YTM IDs and detailed errors remain secondary but readable;
- if a failure Toast appears, the actionable/technical detail remains durable on the owning screen after the Toast disappears;
- reopening/rotating does not auto-resume;
- rollback remains explicit;
- verify Neon plus at least one alternate skin.

Result: `A3+` / `A3-`.

### Test A4 — shared lifecycle

Representative checks:
- long Help window;
- ordinary confirmation;
- #42 History delete confirmation + both rotation directions;
- #42 one destructive utility clear (Import / Service / Data) + rotation;
- #42 Bulk rollback confirmation + rotation;
- Back/Cancel/Close for destructive confirmation;
- verify no delete/clear/rollback fires on recreation;
- scroll retention on a long utility screen;
- selectable text if the surface supports it.

Expected:
- same semantic state after rotation;
- no action fires automatically;
- footer remains visible/readable;
- scroll/selection restoration follows the shared contract.

Result: `A4+` / `A4-`.\n\n#42 phone acceptance remains **PENDING** until this consolidated device test is run.

### Test A5 — themes

Check Neon plus one alternate skin.

Expected:
- primary/secondary/danger/disabled semantics remain readable;
- color is not the only state signal.

Result: `A5+` / `A5-`.

## Wave B/C

Add targeted tests only when those implementation waves land. Do not broaden Wave A
into remote write regression unless a shared change touched execution policy.

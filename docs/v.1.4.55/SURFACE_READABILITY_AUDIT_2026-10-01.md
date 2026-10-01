# v1.4.55 — Remaining Surface Readability Audit — 2026-10-01

## Scope

Issue: #50 / UX-046.

This is the remaining **Phase A / PRESENTATION_ONLY** pass after the Bulk,
critical-transient and Tile/card source gates.

Audited surfaces:
- Menu;
- Home;
- History;
- Queue/Pending;
- Data / Backup;
- Recent File Chooser;
- Storage Chooser;
- ListSelector.

The audit does not activate Phase B features such as History semantic filters,
Recovery Center or a typed file library.

## Classification

| Surface | Classification | Finding |
| --- | --- | --- |
| Menu | **VERIFIED GAP — wording** | old developer-heavy subtitle was already removed, but the primary title `Поточна Bulk-сесія` still exposes an internal product term where a plain-language title can describe the same destination |
| Home | **VERIFIED GAP — hierarchy** | dynamic current-playlist title still compresses playlist name + total + five glyph-only counters into one same-weight string, duplicating the readability problem already removed from Playlist Hub |
| History list/detail | **COMPLIANT with one minor wording cleanup** | list has title/status/linkage + named primary result; detail has named result sections and visible Quick Restore; YTM ID/error detail is secondary/selectable; only the restore confirmation still says `Search` instead of user-facing `пошук` |
| Queue/Pending | **VERIFIED GAP — wording/hierarchy** | primary copy still exposes `Search`, `write`, `snapshot`, `Rate limit`, `Resource limit`, `HTTP 429`; exact technical detail can remain in `lastError`, but list/detail state should be plain-language first |
| Data / Backup | **VERIFIED GAP — terminology** | primary action titles and the top summary are dominated by `Backup`, `Restore`, `History`, `SearchCache`, `URL snapshots`, `Pending Queue`, `Android Share`; user task and recovery meaning should lead, while filenames/SHA-256 remain secondary detail |
| Recent File Chooser | **VERIFIED GAP — primary label** | visible footer/empty-state copy says `SAF-папка`; the action is simply adding an extra folder and does not need Android storage jargon in the primary label |
| Storage Chooser | **COMPLIANT / specialized** | main list clearly shows remembered folders and read/write access; only Help contains the internal `SAF` term, where it can be replaced with plain Android permission wording without changing behavior |
| ListSelector | **COMPLIANT** | title/subtitle, selection count, explicit rows and adaptive confirm/cancel footer are clear; no hidden action or dense operational state |

## Verified patch set

### 1. Home dynamic workspace summary

Current:
- playlist name;
- total;
- `✓ / ! / ⧉ / ⏳ / ×` counters;
- all inside the title field.

Target:
- title = playlist name only;
- subtitle = named total/result counters + linkage state + explicit
  `Натисніть для керування`;
- no track-status calculation changes.

### 2. Queue/Pending user-facing state

Target user wording:
- `Search` → `пошук`;
- `write` → `запис`;
- `snapshot` in primary UI → `збережений стан` / named total;
- `Rate limit / Resource limit / HTTP 429` in list labels → plain-language
  Ukrainian pause reason;
- raw HTTP/API error remains available in durable `lastError` detail.

No queue ownership, resume condition or pause classification changes.

### 3. Data / Backup task wording

Target:
- `Резервні копії та відновлення`;
- `Повна резервна копія`;
- `Відновити з резервної копії`;
- `Відновити лише історію`;
- `Черга`;
- `Поділитися файлами`;
- top summary uses user-facing `Історія / Кеш пошуку / Кеш URL /
  Пошуки сьогодні / Інші API-запити / Знімок перед відновленням`.

Technical filenames, SHA-256 and quota diagnostics remain available as secondary
description, so the patch does not hide recovery/security facts.

### 4. File chooser wording

Target:
- `Додати SAF-папку…` → `Додати папку…`;
- empty-state and Help explain an additional folder in normal Android terms;
- URI/access/persisted-permission implementation remains unchanged.

### 5. Minor terminology cleanup

- Menu `Поточна Bulk-сесія` → plain-language current bulk-sync destination.
- History restore confirmation uses `пошук` instead of `Search`.

## Intentionally not changed

- History semantic filters/grouping: Phase B.
- Recovery Center/global unresolved-work badge: Phase B.
- typed artifact/file library: Phase B.
- raw technical `lastError`, IDs, filenames and integrity/checksum details when
  they serve diagnosis or recovery verification.
- callbacks, request order, retry/auto-start, Queue/Pending ownership, History
  semantics, backup/restore format and remote/API/storage behavior.

## Acceptance

Static/source/build:
- legacy Home glyph-only dynamic summary is absent;
- Queue primary labels no longer use the audited raw English/internal terms;
- Data primary cards lead with Ukrainian task wording;
- Recent File primary action does not expose `SAF`;
- exact-HEAD release preflight / JVM / unsigned assemble pass.

Phone later:
- representative portrait + landscape;
- Neon + Blue or Green;
- user can identify current state/result/next action quickly;
- rotation/navigation starts no Search/write/restore/save/remote operation.

Static/build PASS is not phone PASS.

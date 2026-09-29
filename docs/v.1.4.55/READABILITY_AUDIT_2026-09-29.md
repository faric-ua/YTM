# v1.4.55 — App-wide Readability Audit

Date: 2026-09-29

Baseline reviewed:
`e553c6dcb0f918a51f40bfa4d783cb11b3086472`

Purpose: convert the existing phone findings plus static source review into one
screen-by-screen readability backlog without changing working business behavior.

## Audit conclusion

The project already has strong window/footer/lifecycle contracts, but readability
is still inconsistent.

The dominant pattern is not "bad colors". It is information hierarchy:
- important state and counts are often embedded in long same-weight text;
- implementation terms appear in primary UI;
- several screens require line-by-line reading to answer "what happened?" and
  "what should I do next?";
- critical transient messages can be truncated;
- list/History/file surfaces are technically complete but hard to navigate quickly.

This audit is the execution plan for UX-046.

## Severity model

- P0: can cause a wrong/safety-relevant decision.
- P1: hard to understand current state/next action.
- P2: understandable but unnecessarily technical/dense.
- P3: polish/consistency.

## Screen matrix

| Surface | Current readability finding | Severity | Planned presentation-only direction |
|---|---|---:|---|
| Home | Current playlist stats use dense icon/counter string; unresolved work is not surfaced globally | P1 | structured compact counters; Recovery attention icon/badge; no business-state duplication |
| Menu | subtitles contain `Read-only Bulk preflight`, `pause / explicit resume after restart` | P1 | user wording first; technical copy in Help/secondary text |
| Playlist Hub | slash-heavy labels such as `YTM Project / export`, `Заміни / проблемні треки` | P2 | concise labels; preserve exact delegated actions |
| Review | manual override visually resembles ordinary matched result; long URL field hard to inspect | P1 | manual-selection badge/hierarchy; multiline URL input + clear |
| Destination | duplicate/videoId/API-unit language dominates explanatory text | P2 | user result first; exact IDs/units secondary |
| Bulk Preview | raw enum-like states, `remote mutations`, `ready videoId`, `units`; cards are dense | P0 | Ukrainian state badges; title primary; planned create/add counters; safety result; diagnostics secondary |
| Bulk Session | `mutation`, `Checkpoint`, `Remote baseline`, `PREPARED` and policy paragraphs compete with current state | P0 | state/result/remaining actions in strong hierarchy; technical ledger details collapsed/secondary |
| Quota | ordinary user quota mixed with developer/API terminology and phone-QA controls | P1 | user quota summary first; diagnostics/QA separated; remove temporary QA controls before public release |
| Queue/Pending | core card is readable, but HTTP/rate diagnostics can dominate and unresolved work is discoverable only by opening Queue | P1 | plain-language pause reason first; Recovery Center entry point; raw HTTP secondary |
| History list | 100-record scan is difficult; search alone insufficient | P1 | text search + semantic filters; stable provider/linkage/status affordances |
| History detail | restore action hidden in `Дії`; quick actions omit primary recovery action | P1 | add `Відновити як поточний плейлист` to Quick Actions; keep same safe confirmation |
| Data / Backup | descriptions mix Backup/Restore/History JSON/SearchCache jargon; chooser shows mixed JSON types | P1 | task wording; type-aware library; canonical folders; content-validated artifacts |
| Import | account backup / manifest / exact videoId concepts are exposed early | P2 | task-oriented labels; technical manifest/videoId details secondary |
| URL Snapshot | `exact videoId` and duplicate internals appear in primary result copy | P2 | say tracks/duplicates first; exact-ID details in secondary diagnostics |
| Service | intentionally technical surface, but main cards still mix user actions and diagnostics | P2 | keep Service as advanced area; stronger grouping between user tools and diagnostics |
| Storage/Recent chooser | navigation itself is usable, but file type/context is weak | P1 | scoped type-aware chooser; human-readable artifact cards |
| ListSelector | generally readable; keep Help + adaptive footer contracts | P3 | consistency only |
| Shared dialogs/Help | fixed footer/layout is now consolidated; long bodies still need stronger title/state/result hierarchy | P1 | shared semantic sections; avoid same-weight paragraphs |
| Toast/Snackbar | important QA/error text can truncate | P0 | never use transient-only channel for critical state/recovery/error detail |

## Shared hierarchy pattern

For operational/status cards:

1. **Title/entity** — 16–18sp equivalent, high contrast.
2. **State badge** — completed/running/paused/blocked/attention.
3. **Primary result** — key counts or outcome.
4. **Next action** — one clear sentence when action is needed.
5. **Secondary facts** — destination, provider, timestamp.
6. **Diagnostics** — ids, HTTP, units, checkpoint, ledger details.

Do not render all six layers with identical font/weight/color.

## Shared status vocabulary

Prefer user-facing Ukrainian:
- `Новий`
- `Пов'язано з YTM`
- `Уже синхронізовано`
- `Потрібен пошук`
- `У черзі`
- `Заблоковано`
- `Виконується`
- `На паузі`
- `Потребує уваги`
- `Завершено`
- `Завершено частково`
- `Відкочено`

Internal enums may remain in code/diagnostics.

## Critical-message rule

If the user must remember it after the transient message disappears, it is not
Toast-only information.

Examples that belong inline/result/detail:
- operation interrupted;
- partial failure;
- quota/rate pause;
- rollback remaining count;
- exact item that was not added;
- restore result;
- destructive action result.

## Readability wave order

### Wave A — no business behavior changes
- shared status/result hierarchy primitives;
- action-button/layout unification;
- scroll-position preservation;
- selectable-text restoration;
- destructive modal lifecycle;
- Bulk Preview/Session readability;
- Menu wording;
- transient critical-message cleanup.

### Wave B — management/discoverability
- History filters/search/grouping;
- History Quick Restore;
- Recovery Center + breathing attention icon;
- type-aware file library;
- simplified Termux operator menu.

### Wave C — local workflow convenience
- current local playlist Edit;
- blank URL inline validation;
- Review manual URL/manual-selection polish;
- Bulk preparation presentation/state cleanup.

Wave B/C may add navigation or local workflow features, but remote/API behavior stays
unchanged unless a separately reviewed functional issue says otherwise.

## Existing issue mapping

Core shared/UI:
- #37 UX-038 window/footer consolidation
- #47 UX-045 action layout
- #48 BUG-048 scroll retention
- #49 BUG-049 text selection retention
- #50 UX-046 readability audit
- #42 BUG-047 destructive modal lifecycle

Readability specifics:
- #23 UX-031 Bulk Preview scanability
- #45 UX-043 Bulk Preview semantic emphasis
- #26 UX-033 action labels/buttons
- #27 UX-034 Bulk preparation presentation
- #28 BUG-041 stale Bulk preparation status

Management/discoverability:
- #25 UX-032 History logical grouping/provider
- #41 UX-040 History filters/findability
- #53 UX-048 Recovery Center
- #54 UX-049 typed file library
- #55 UX-050 History Quick Restore
- #52 UX-047 Termux operator menu

Local workflow convenience:
- #30 UX-036 local playlist Edit
- #29 UX-035 blank URL validation
- #40 UX-039 Review manual URL/override visibility

## Definition of done

The readability wave is not "make it prettier".

A surface passes when a user can identify in about 1–2 seconds:
- what object/screen this is;
- current state;
- primary result/counters;
- whether remote work already happened;
- whether attention is required;
- the safe next action.

Diagnostics remain available without dominating the primary UI.

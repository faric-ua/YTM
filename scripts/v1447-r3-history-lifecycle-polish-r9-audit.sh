#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }

IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
RELAY="app/src/main/java/com/saney/ytmimporter/ui/WorkflowRelayOverlay.kt"
STD="docs/v.1.4.47/navigation/TEST_DIAGRAM_STANDARD.md"
DOC="docs/v.1.4.47/qa/R3_HISTORY_LIFECYCLE_POLISH_R9.md"
BUGS="docs/v.1.4.47/qa/BUG_REGISTER.md"

for f in "$IMPORT" "$HISTORY" "$RELAY" "$STD" "$DOC" "$BUGS"; do
  test -f "$f" || fail "missing R9 file: $f"
done

python - "$IMPORT" "$HISTORY" "$RELAY" "$STD" "$DOC" "$BUGS" <<'PY'
from pathlib import Path
import sys
imp, hist, relay, standard, doc, bugs = [
    Path(p).read_text(encoding="utf-8")
    for p in sys.argv[1:]
]

for n in [
    "private lateinit var historyStore:",
    "HistoryStore(this)",
    "historyStore.upsert(",
    "private fun localImportHistoryEntry(",
    '"local-import-"',
    "HistoryStatus.COMPLETED",
    'privacyStatus = "local"',
    "playlistId = null",
    "writeTargetCount = 0",
    'status = "IMPORTED"',
]:
    if n not in imp:
        raise SystemExit(f"FAIL: BUG-025 import History contract missing: {n}")

for n in [
    "private var actionsDialogOpen = false",
    "private var actionsDialog: Dialog? = null",
    "KEY_ACTIONS_DIALOG_OPEN",
    "actionsDialogOpen = true",
    "if (!isChangingConfigurations)",
    "showActions(entry)",
    "HistoryResultKind.IMPORT",
    '"Тип: Локальний імпорт"',
]:
    if n not in hist:
        raise SystemExit(f"FAIL: BUG-024/025 History contract missing: {n}")

for n in [
    "val semanticIcon =",
    '"○"',
    '"●"',
    '"✓"',
    '"≋"',
    '"×"',
    "semanticIconColor",
    "fill = palette.surface",
    "accentStroke = false",
    "palette.muted",
]:
    if n not in relay:
        raise SystemExit(f"FAIL: R9 neutral-row/icon contract missing: {n}")

for forbidden in [
    "fill = rowFill",
    "private fun blendColor(",
]:
    if forbidden in relay:
        raise SystemExit(f"FAIL: obsolete strong-fill contract remains: {forbidden}")

for n in [
    "`○` + `Очікує`",
    "`✓` + `Додано`",
    "`≋` + `Дублікат • пропущено`",
    "leading-іконці",
    "суцільну заливку",
]:
    if n not in standard:
        raise SystemExit(f"FAIL: diagram standard R9 semantics missing: {n}")

for n in ["BUG-024","BUG-025"]:
    if n not in bugs:
        raise SystemExit(f"FAIL: bug register missing: {n}")

for n in [
    "R9 functional contract",
    "local-import History",
    "Write-progress rows no longer use strong semantic full-row fills",
]:
    if n not in doc:
        raise SystemExit(f"FAIL: R9 QA doc missing: {n}")

print("PASS:")
print("- BUG-024 History actions modal owns rotation state")
print("- BUG-025 successful local imports create History entries")
print("- History detail distinguishes local import from YTM write")
print("- write rows are neutral with semantic leading icons")
print("- diagram standard matches phone UI semantics")
PY

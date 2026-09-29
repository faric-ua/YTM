#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
UI="$SRC/ui/UiChrome.kt"
RESTORABLE="$SRC/ui/RestorableModalController.kt"
SELECTABLE="$SRC/ui/SelectableTextState.kt"
QUOTA="$SRC/QuotaActivity.kt"
QA_STORE="$SRC/storage/BulkSyncQaFaultStore.kt"
SAFETY="docs/design/UX_CHANGE_SAFETY_CONTRACT.md"
READABILITY="docs/v.1.4.55/READABILITY_AUDIT_2026-09-29.md"
PLAN="docs/v.1.4.55/UX_HARDENING_MASTER_PLAN.md"
RECONCILIATION="docs/v.1.4.55/BACKLOG_RECONCILIATION_2026-09-29.md"

for f in "$UI" "$RESTORABLE" "$SELECTABLE" "$QUOTA" "$QA_STORE" "$SAFETY" "$READABILITY" "$PLAN" "$RECONCILIATION"; do
  test -f "$f" || fail "missing v1.4.55 hardening file: $f"
done

grep -Fq 'PRESENTATION_ONLY' "$SAFETY" ||
  fail "UX change classification contract missing"
grep -Fq 'FUNCTIONAL_FEATURE' "$SAFETY" ||
  fail "functional-feature separation contract missing"
grep -Fq 'must not silently change' "$SAFETY" ||
  fail "business-behavior preservation guard missing"

grep -Fq 'fitsHorizontalActionGroup(' "$UI" ||
  fail "shared screen/dialog action fit policy missing"
grep -Fq 'useHorizontalActionRow(' "$UI" ||
  fail "full-screen adaptive action contract missing"
grep -Fq 'useHorizontalDialogActionRow(' "$UI" ||
  fail "dialog adaptive action contract missing"

grep -Fq 'object SelectableTextState' "$SELECTABLE" ||
  fail "shared selectable-text state helper missing"
grep -Fq 'view.isTextSelectable' "$SELECTABLE" ||
  fail "selectable-text helper does not scope to selectable TextViews"
grep -Fq 'currentText !=' "$SELECTABLE" ||
  fail "selectable-text restore does not fail closed on changed content"
grep -Fq 'SelectableTextState.capture(' "$RESTORABLE" ||
  fail "restorable modals do not capture selectable-text ranges"
grep -Fq 'SelectableTextState.restore(' "$RESTORABLE" ||
  fail "restorable modals do not restore selectable-text ranges"
grep -Fq 'KEY_SELECTABLE_TEXT_STATE' "$RESTORABLE" ||
  fail "restorable modal selectable-text state is not persisted"

grep -Fq 'if (BuildConfig.DEBUG)' "$QUOTA" ||
  fail "release Quota UI still exposes phone-QA controls"
grep -Fq 'if (!BuildConfig.DEBUG)' "$QA_STORE" ||
  fail "release runtime can still consume persisted QA fault flags"
grep -Fq 'QA fault controls are debug-only' "$QA_STORE" ||
  fail "QA insert fault arm path is not debug-only"
grep -Fq 'QA rollback interrupt is debug-only' "$QA_STORE" ||
  fail "QA rollback interrupt arm path is not debug-only"

grep -Fq 'App-wide Readability Audit' "$READABILITY" ||
  fail "readability audit missing"
grep -Fq 'information hierarchy' "$READABILITY" ||
  fail "readability audit lost its hierarchy rule"
grep -Fq 'Backlog / Rules Reconciliation' "$RECONCILIATION" ||
  fail "backlog/rule reconciliation missing"
grep -Fq 'Recovery Center' "$PLAN" ||
  fail "management/recovery workstream missing"

echo "PASS:"
echo "- v1.4.55 UX safety contract is locked"
echo "- one label-aware action fit policy serves screen and dialog actions"
echo "- restorable modals persist active selectable-text ranges"
echo "- changed selectable text fails closed instead of restoring a stale range"
echo "- release builds cannot expose or consume v1.4.54 QA fault controls"
echo "- readability audit + backlog reconciliation + management plan are present"

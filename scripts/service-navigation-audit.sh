#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
SRC="app/src/main/java/com/saney/ytmimporter"
MAIN="$SRC/MainActivity.kt"
SERVICE="$SRC/ServiceActivity.kt"
UI="$SRC/ui/UiChrome.kt"

grep -q 'private enum class Page' "$SERVICE" || fail "Service page state missing"
grep -Fq 'when (page)' "$SERVICE" || fail "Service back-stack dispatcher missing"
grep -Fq 'Page.HOME ->' "$SERVICE" || fail "Service HOME back route missing"
grep -Fq 'Page.CHANGELOG -> {' "$SERVICE" || fail "Service CHANGELOG back route missing"
grep -Fq 'Page.VERSION -> {' "$SERVICE" || fail "Service VERSION back route missing"
grep -Fq 'page = Page.ABOUT' "$SERVICE" || fail "Service CHANGELOG parent route missing"
grep -Fq 'page = Page.HOME' "$SERVICE" || fail "Service subpage HOME back route missing"
grep -q 'private fun buildQuickStart' "$SERVICE" || fail "Quick Start detail missing"
grep -q 'private fun buildPrivacy' "$SERVICE" || fail "Privacy detail missing"
grep -q 'private fun buildDiagnostics' "$SERVICE" || fail "Diagnostics detail missing"
grep -q 'private fun buildSearchCache' "$SERVICE" || fail "SearchCache detail missing"
grep -q 'private fun buildAbout' "$SERVICE" || fail "About detail missing"
grep -q 'saveDiagnosticsRequestCode' "$SERVICE" || fail "Service local diagnostics save missing"
grep -q 'shareDiagnostics()' "$SERVICE" || fail "Service local diagnostics share missing"
if grep -q 'returnAction' "$SERVICE"; then fail "Service still exits through returnAction"; fi
if grep -q 'handleServiceResult' "$MAIN"; then fail "Main still handles Service submenu result"; fi
grep -q 'UiChrome.showRecordDialog' "$MAIN" || fail "problem tracks are not rendered as record tiles"
grep -q 'VERTICAL_WITH_TEXT_CLOSE' "$MAIN" || fail "problem-track export actions are not stacked"
grep -q 'data class DialogRecord' "$UI" || fail "DialogRecord template missing"

echo 'PASS:'
echo '- Service subpages stay inside ServiceActivity'
echo '- Back uses explicit HOME / CHANGELOG / other-subpage routing'
echo '- CHANGELOG and VERSION Back return to About; other subpages return to Service home'
echo '- Diagnostics/SearchCache/About use structured screens'
echo '- problem tracks use record tiles and stacked export actions'

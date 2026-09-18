#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

# Historical v1.4.26 feature guard.
# Active versionCode/versionName are checked by release-preflight
# for the current release. This audit verifies that the v1.4.26
# selective-export feature remains preserved in later releases.
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
EXPORTER="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt"
STATUS="RELEASE_TEST_STATUS.md"
BACKLOG="BACKLOG.md"
ROADMAP="docs/tutorial/ROADMAP.md"

grep -Fq '"Вибрати плейлисти для експорту"' "$IMPORT" || fail "selective export action missing"
grep -q 'selectiveExportFolderRequestCode' "$IMPORT" || fail "selective folder request code missing"
grep -Fq 'ListSelectorActivity.multiIntent(' "$IMPORT" || fail "full-screen multi-select picker missing"
grep -q 'selectiveExportSelectorRequestCode' "$IMPORT" || fail "selective selector request code missing"
grep -q 'pendingSelectiveExport' "$IMPORT" || fail "selective selection state missing"
grep -q 'override fun onSaveInstanceState' "$IMPORT" || fail "saved-instance-state handling missing"
grep -q 'STATE_SELECTIVE_EXPORT' "$IMPORT" || fail "selective state key missing"
grep -q 'exportSelectedYtmPlaylistsToFolder' "$IMPORT" || fail "selected export route missing"
grep -q 'playlists = selected' "$IMPORT" || fail "selected export does not use selected list"
grep -Fq 'selectionMode = "SELECTED"' "$IMPORT" || fail "SELECTED export mode missing"
grep -Fq 'selectionMode = "ALL"' "$IMPORT" || fail "ALL export mode missing"
grep -q 'private fun exportAccountPlaylistsToFolder' "$IMPORT" || fail "shared account export core missing"

grep -q 'selectionMode: String = "ALL"' "$EXPORTER" || fail "manifest selectionMode parameter missing"
grep -Fq '.put("schemaVersion", 2)' "$EXPORTER" || fail "manifest schemaVersion 2 missing"
grep -Fq '"selectionMode"' "$EXPORTER" || fail "manifest selectionMode field missing"

if grep -Eq 'createPlaylist|insertPlaylistItem|deletePlaylist' "$IMPORT"; then
  fail "ImportActivity account export must stay read-only"
fi

grep -Fq '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 FOUND** |' "$STATUS" || fail "v1.4.26 selective-export phone status missing"
grep -Fq '## v1.4.26' "$BACKLOG" || fail "v1.4.26 backlog section missing"
grep -Fq 'Yerin Exclusive hidden skin' "$BACKLOG" || fail "Yerin Exclusive future roadmap missing"
grep -Fq 'Korean (`ko`) language' "$BACKLOG" || fail "Korean localization roadmap missing"

grep -Fq '`06_ACCOUNT_LIBRARY_EXPORT.md`' "$ROADMAP" || fail "tutorial chapter 06 missing from roadmap"
grep -Fq '`19_LOCALIZATION_UK_KO_EN.md`' "$ROADMAP" || fail "tutorial localization chapter missing"
grep -Fq '`20_YERIN_EXCLUSIVE_SKIN.md`' "$ROADMAP" || fail "tutorial skin chapter missing"

test -f docs/v.1.4.26/RELEASE.md || fail "v1.4.26 release doc missing"
test -f docs/v.1.4.26/REGRESSION_CHECKLIST.md || fail "v1.4.26 checklist missing"
test -f docs/v.1.4.26/qa/PHONE_TEST.md || fail "v1.4.26 phone test missing"
test -f docs/v.1.4.26/diagrams/SELECTIVE_EXPORT_FLOW.md || fail "v1.4.26 flow diagram missing"
test -f docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md || fail "tutorial chapter 06 missing"

echo "PASS:"
echo "- v1.4.26 selective account export guards"
echo "- saved multi-selection + full-screen selector"
echo "- selected-only playlist processing"
echo "- ALL export mode retained"
echo "- manifest schema v2 + selectionMode"
echo "- read-only invariant"
echo "- localization/Yerin future roadmap"

#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SRC="app/src/main/java/com/saney/ytmimporter"
CLASSIFIER="$SRC/storage/YtmArtifactClassifier.kt"
CACHE="$SRC/storage/YtmArtifactClassificationCache.kt"
TEST="app/src/test/java/com/saney/ytmimporter/storage/YtmArtifactClassifierTest.kt"
CACHE_TEST="app/src/test/java/com/saney/ytmimporter/storage/YtmArtifactClassificationCacheTest.kt"
CONTRACT="docs/v.1.4.55/FILE_LIBRARY_CONTRACT.md"
INVENTORY="docs/v.1.4.55/FILE_LIBRARY_AUDIT_2026-10-05.md"
DATA="$SRC/DataActivity.kt"
HISTORY="$SRC/storage/HistoryStore.kt"
BACKUP="$SRC/storage/LocalBackupManager.kt"
PROJECT="$SRC/storage/PlaylistProjectCodec.kt"
CHOOSER="$SRC/RecentFileChooserActivity.kt"

for f in "$CLASSIFIER" "$CACHE" "$TEST" "$CACHE_TEST" "$CONTRACT" "$INVENTORY" "$DATA" "$HISTORY" "$BACKUP" "$PROJECT" "$CHOOSER"; do
  test -f "$f" || fail "missing #54 foundation file: $f"
done

grep -Fq 'enum class YtmArtifactType' "$CLASSIFIER" ||
  fail "artifact type enum missing"
grep -Fq 'object YtmArtifactClassifier' "$CLASSIFIER" ||
  fail "artifact classifier missing"
grep -Fq 'object YtmArtifactScopePolicy' "$CLASSIFIER" ||
  fail "artifact scope policy missing"

for marker in   'ytm-importer-local-backup'   'ytm-importer-playlist-project'   'ytm-importer-account-library-export'   '"tracks"'   '"remainingTracks"'   '"operation"'
do
  grep -Fq "$marker" "$CLASSIFIER" ||
    fail "classifier marker missing: $marker"
done

grep -Fq 'if (array.length() == 0)' "$CLASSIFIER" ||
  fail "empty JSON arrays do not fail closed"
grep -Fq 'allHistory ==' "$CLASSIFIER" ||
  fail "ambiguous/mixed array fail-closed guard missing"
grep -Fq 'YtmArtifactType.UNKNOWN' "$CLASSIFIER" ||
  fail "UNKNOWN artifact state missing"

python - "$CLASSIFIER" <<'PY'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
for forbidden in (
    "Activity",
    "ContentResolver",
    "HistoryStore(",
    "LocalBackupManager(",
    "PendingJobStore(",
    "YouTubeApi",
    "startActivity",
    "restoreBackupJson",
    "restoreHistoryJson",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: classifier owns runtime/domain work: " + forbidden
        )
PY

for test_name in   'fullLocalBackupUsesContentMarker'   'playlistProjectUsesProjectMarkerAndMetadata'   'accountManifestIsNotConfusedWithFullLocalBackup'   'historyArrayUsesHistoryStructure'   'pendingArrayUsesPendingStructure'   'emptyArrayFailsClosed'   'mixedArrayFailsClosed'   'malformedOrIncompleteJsonFailsClosed'   'scopesAcceptOnlyTheirCanonicalType'
do
  grep -Fq "$test_name" "$TEST" ||
    fail "classifier JVM case missing: $test_name"
done

grep -Fq 'A file extension is not an artifact type.'   docs/design/UX_CHANGE_SAFETY_CONTRACT.md ||
  fail "project-wide file/backup safety rule missing"
grep -Fq 'Classification is read-only UX policy.' "$CONTRACT" ||
  fail "#54 read-only classifier boundary missing"
grep -Fq 'never silently move legacy files' "$CONTRACT" ||
  fail "#54 legacy-file safety rule missing"

grep -Fq 'object YtmArtifactClassificationCache' "$CACHE" ||
  fail "rotation-safe artifact cache missing"
grep -Fq 'MAX_ENTRIES' "$CACHE" ||
  fail "artifact classification cache is not bounded"
grep -Fq 'lastModified' "$CACHE" ||
  fail "artifact cache key does not include lastModified"
grep -Fq 'size' "$CACHE" ||
  fail "artifact cache key does not include size"

python - "$CACHE" <<'PY_ARTIFACT_CACHE'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
for forbidden in (
    "android.",
    "Activity",
    "ContentResolver",
    "restoreBackupJson",
    "restoreHistoryJson",
    "YouTubeApi",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: artifact cache owns Android/domain work: " + forbidden
        )
PY_ARTIFACT_CACHE

for test_name in   'sameFileIdentityReusesClassification'   'changedMetadataInvalidatesOldIdentity'   'newVersionReplacesOldIdentityForSameUri'   'cacheIsBounded'
do
  grep -Fq "$test_name" "$CACHE_TEST" ||
    fail "artifact cache JVM case missing: $test_name"
done

grep -Fq 'EXTRA_ARTIFACT_SCOPE' "$CHOOSER" ||
  fail "scoped chooser intent contract missing"
grep -Fq 'YtmArtifactClassifier' "$CHOOSER" ||
  fail "recent-file chooser does not classify JSON content"
grep -Fq 'YtmArtifactScopePolicy' "$CHOOSER" ||
  fail "recent-file chooser does not apply artifact scope"
grep -Fq 'classifierExecutor' "$CHOOSER" ||
  fail "artifact classification is not kept off the UI thread"
grep -Fq 'YtmArtifactClassificationCache' "$CHOOSER" ||
  fail "chooser does not reuse artifact classifications after recreation"
grep -Fq 'cachedArtifactType' "$CHOOSER" ||
  fail "chooser cache lookup missing"
grep -Fq 'Thread' "$CHOOSER" ||
  fail "interrupted classification cache guard missing"
grep -Fq '"Інший файл…"' "$CHOOSER" ||
  fail "explicit legacy/system file fallback label missing"
grep -Fq 'YtmArtifactScope' "$DATA" ||
  fail "DataActivity scoped restore intent missing"
grep -Fq '.FULL_LOCAL_RESTORE' "$DATA" ||
  fail "Full Restore does not request FULL_LOCAL_RESTORE candidates"
grep -Fq '.HISTORY_RESTORE' "$DATA" ||
  fail "History Import does not request HISTORY_RESTORE candidates"
grep -Fq 'RecentFileChooserActivity.EXTRA_ARTIFACT_SCOPE' "$DATA" ||
  fail "DataActivity does not pass artifact scope to chooser"

python - "$CHOOSER" <<'PY_SCOPED_CHOOSER'
from pathlib import Path
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8")
for forbidden in (
    "restoreBackupJson",
    "restoreHistoryJson",
    ".importProject(",
    "YouTubeApi",
):
    if forbidden in text:
        raise SystemExit(
            "FAIL: scoped chooser can execute domain work: " + forbidden
        )

for required in (
    "refreshRecentFiles()",
    "inspectArtifactType(",
    "runOnUiThread",
    "generation !=",
):
    if required not in text:
        raise SystemExit(
            "FAIL: scoped chooser async/lifecycle guard missing: " + required
        )
PY_SCOPED_CHOOSER

grep -Fq '.inspectBackup(' "$DATA" ||
  fail "Full Restore owner validation missing"
grep -Fq '.inspectImportJson(' "$DATA" ||
  fail "History owner validation missing"
grep -Fq '.importProject(' "$SRC/ImportActivity.kt" ||
  fail "Project owner validation missing"
grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' "$CHOOSER" ||
  fail "explicit system-picker fallback missing"

echo "PASS:"
echo "- #54 content-first artifact classifier foundation"
echo "- fail-closed legacy array classification"
echo "- pure scoped candidate policy"
echo "- Full Restore + History Import scoped recent-file wiring"
echo "- background read-only classification with stale-result guard"
echo "- bounded URI/mtime/size classification cache for rotation continuity"
echo "- interrupted reads are not cached"
echo "- JVM matrix present"
echo "- existing owner validators remain authoritative"
echo "- explicit «Інший файл…» legacy/system fallback retained"

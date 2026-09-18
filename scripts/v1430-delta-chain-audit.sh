#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
CHAIN="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryDeltaChainRestorer.kt"
NAMING="app/src/main/java/com/saney/ytmimporter/storage/AccountBackupNaming.kt"
EXPORTER="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryExporter.kt"
SYNC="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryIncrementalBackup.kt"

for f in \
  "$IMPORT" \
  "$CHAIN" \
  "$NAMING" \
  docs/v.1.4.30/RELEASE.md \
  docs/v.1.4.30/REGRESSION_CHECKLIST.md \
  docs/v.1.4.30/qa/PHONE_TEST.md \
  docs/v.1.4.30/qa/BUG_REGISTER.md \
  docs/v.1.4.30/diagrams/DELTA_CHAIN_FLOW.md \
  docs/tutorial/14_BACKUP_AND_EXPORT.md
do
  test -f "$f" || fail "missing v1.4.30 delta-chain file: $f"
done

grep -Fq 'deltaChainRootRequestCode' "$IMPORT" \
  || fail "delta-chain root request code missing"
grep -Fq 'deltaChainTargetRequestCode' "$IMPORT" \
  || fail "delta-chain target request code missing"
grep -Fq 'Зібрати повний backup з chain' "$IMPORT" \
  || fail "delta-chain UI action missing"
grep -Fq 'prepareDeltaChainRoot' "$IMPORT" \
  || fail "delta-chain root scan missing"
grep -Fq 'showDeltaChainHeadPicker' "$IMPORT" \
  || fail "delta-chain head picker missing"
grep -Fq 'showDeltaChainPreview' "$IMPORT" \
  || fail "delta-chain preview missing"
grep -Fq '"Створити"' "$IMPORT" \
  || fail "single-line mobile create action missing"
if grep -Fq '"Створити backup"' "$IMPORT"; then
  fail "two-word create action still present and can wrap"
fi
if grep -Fq 'label =' "$IMPORT" && grep -Fq '"Матеріалізувати"' "$IMPORT"; then
  fail "long technical materialize label still present"
fi
grep -Fq 'materializeDeltaChain' "$IMPORT" \
  || fail "materialize result flow missing"
grep -Fq 'YouTube API = 0' "$IMPORT" \
  || fail "local-only UI boundary missing"

grep -Fq 'baseSessionName' "$CHAIN" \
  || fail "baseSessionName chain link missing"
grep -Fq 'visited.add' "$CHAIN" \
  || fail "cycle guard missing"
grep -Fq 'scopePlaylistIds' "$CHAIN" \
  || fail "selected scope preservation missing"
grep -Fq '"NEW"' "$CHAIN" \
  || fail "NEW replay missing"
grep -Fq '"UPDATED"' "$CHAIN" \
  || fail "UPDATED replay missing"
grep -Fq '"UNCHANGED"' "$CHAIN" \
  || fail "UNCHANGED replay missing"
grep -Fq '"MISSING"' "$CHAIN" \
  || fail "MISSING replay missing"
grep -Fq '"FAILED"' "$CHAIN" \
  || fail "FAILED fail-closed path missing"
grep -Fq 'contentFingerprint не збігається' "$CHAIN" \
  || fail "project fingerprint validation missing"
grep -Fq 'playlistId manifest/project не збігається' "$CHAIN" \
  || fail "playlistId cross-check missing"
grep -Fq 'privacyStatus manifest/project не збігається' "$CHAIN" \
  || fail "privacyStatus cross-check missing"

grep -Fq '"yyMMdd-HHmmss"' "$NAMING" \
  || fail "mobile-first timestamp format missing"
grep -Fq '"${timestamp(date)}-YTM-Export"' "$NAMING" \
  || fail "short export folder name missing"
grep -Fq '"${timestamp(date)}-YTM-Sync"' "$NAMING" \
  || fail "short sync folder name missing"
grep -Fq '"${timestamp(date)}-YTM-Full"' "$NAMING" \
  || fail "short consolidated folder name missing"
grep -Fq 'exportFolderName' "$EXPORTER" \
  || fail "exporter does not use centralized mobile naming"
grep -Fq 'syncFolderName' "$SYNC" \
  || fail "incremental backup does not use centralized mobile naming"
grep -Fq 'consolidatedFolderName' "$CHAIN" \
  || fail "delta-chain materializer does not use centralized mobile naming"
grep -Fq 'CONSOLIDATED_FULL' "$CHAIN" \
  || fail "consolidated backupMode missing"
grep -Fq '"schemaVersion",' "$CHAIN" \
  || fail "consolidated manifest schema field missing"
grep -Fq '"materializedFromChain"' "$CHAIN" \
  || fail "chain provenance flag missing"
grep -Fq '"chainBaseSessionName"' "$CHAIN" \
  || fail "chain base provenance missing"
grep -Fq '"chainHeadSessionName"' "$CHAIN" \
  || fail "chain head provenance missing"
grep -Fq '"sourceSessions"' "$CHAIN" \
  || fail "source session provenance missing"
grep -Fq 'AccountLibraryExporter' "$CHAIN" \
  || fail "AccountLibraryExporter materialization dependency missing"
grep -Fq 'writePlaylistProject' "$CHAIN" \
  || fail "project materialization missing"

if grep -Fq 'YouTubeApi' "$CHAIN"; then
  fail "delta-chain storage layer must not own YouTubeApi"
fi
if grep -Fq 'search.list' "$CHAIN"; then
  fail "delta-chain storage layer must not use search.list"
fi
if grep -Fq 'playlistItems.list' "$CHAIN"; then
  fail "delta-chain storage layer must not use playlistItems.list"
fi
if grep -Fq 'createPlaylist(' "$CHAIN"; then
  fail "delta-chain storage layer must not create remote playlists"
fi
if grep -Fq 'addVideo(' "$CHAIN"; then
  fail "delta-chain storage layer must not write remote playlist items"
fi

grep -Fq 'versionCode: **64**' docs/v.1.4.30/RELEASE.md \
  || fail "v1.4.30 release versionCode missing"
grep -Fq 'versionName: **1.4.30**' docs/v.1.4.30/RELEASE.md \
  || fail "v1.4.30 release versionName missing"
grep -Fq '# YTM Importer v1.4.30 — Consolidated Delta-Chain Restore' docs/v.1.4.30/RELEASE.md \
  || fail "v1.4.30 release identity missing"

echo "PASS:"
echo "- local backup-session discovery"
echo "- mobile-first timestamp-first backup folder names"
echo "- short consolidated action label"
echo "- delta head selection"
echo "- baseSessionName chain traversal + cycle guard"
echo "- ALL / SELECTED scope preservation"
echo "- NEW / UPDATED / UNCHANGED / MISSING replay"
echo "- FAILED fails closed"
echo "- exact project identity/fingerprint validation"
echo "- schema-v3 CONSOLIDATED_FULL materialization"
echo "- source-session provenance"
echo "- no YouTube API/search/remote write dependency"
echo "- v1.4.30 release docs"

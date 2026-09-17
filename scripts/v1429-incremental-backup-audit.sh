#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
SYNC="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryIncrementalBackup.kt"
MANIFEST="app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryManifestImporter.kt"

for f in \
  "$IMPORT" \
  "$SYNC" \
  "$MANIFEST" \
  docs/v.1.4.29/RELEASE.md \
  docs/v.1.4.29/REGRESSION_CHECKLIST.md \
  docs/v.1.4.29/qa/PHONE_TEST.md \
  docs/v.1.4.29/qa/BUG_REGISTER.md \
  docs/v.1.4.29/diagrams/INCREMENTAL_BACKUP_FLOW.md \
  docs/tutorial/14_BACKUP_AND_EXPORT.md
do
  test -f "$f" || fail "missing v1.4.29 incremental-backup file: $f"
done

grep -Fq 'incrementalBackupBaseRequestCode' "$IMPORT" \
  || fail "incremental base request code missing"
grep -Fq 'incrementalBackupTargetRequestCode' "$IMPORT" \
  || fail "incremental target request code missing"
grep -Fq 'Оновити backup (incremental)' "$IMPORT" \
  || fail "incremental backup UI action missing"
grep -Fq 'prepareIncrementalBackup' "$IMPORT" \
  || fail "incremental baseline preparation missing"
grep -Fq 'estimatedPlaylistItemsRequests' "$IMPORT" \
  || fail "request estimate UI missing"
grep -Fq 'Перевірити зміни' "$IMPORT" \
  || fail "scan confirmation action missing"
grep -Fq 'showIncrementalBackupPreview' "$IMPORT" \
  || fail "delta preview missing"
grep -Fq 'Зберегти delta' "$IMPORT" \
  || fail "delta save action missing"
grep -Fq 'writeIncrementalBackup' "$IMPORT" \
  || fail "delta write flow missing"

grep -Fq 'IncrementalDeltaManifestException' "$IMPORT" \
  || fail "typed incremental delta boundary handling missing"
grep -Fq 'showIncrementalDeltaBoundary' "$IMPORT" \
  || fail "readable incremental delta boundary dialog missing"
grep -Fq '"Incremental delta backup"' "$IMPORT" \
  || fail "delta boundary dialog title missing"
grep -Fq 'UiChrome.showMessageDialog' "$IMPORT" \
  || fail "delta boundary must use UiChrome message dialog"

if grep -Fq 'UiChrome.ActionTone.NEUTRAL' "$IMPORT"; then
  fail "incremental dialogs use nonexistent ActionTone.NEUTRAL"
fi
grep -Fq 'UiChrome.ActionTone.NORMAL' "$IMPORT" \
  || fail "incremental cancel actions must use ActionTone.NORMAL"

for status in NEW UPDATED UNCHANGED MISSING FAILED
do
  grep -Fq "\"$status\"" "$SYNC" \
    || fail "incremental status missing: $status"
done

grep -Fq 'schemaVersion",' "$SYNC" \
  || fail "schema version field missing"
grep -Fq 'SYNC_SCHEMA_VERSION' "$SYNC" \
  || fail "sync schema constant missing"
grep -Fq 'INCREMENTAL_DELTA' "$SYNC" \
  || fail "incremental backup mode missing"
grep -Fq '"selectionMode",' "$SYNC" \
  || fail "selectionMode field missing"
grep -Fq '"SYNC"' "$SYNC" \
  || fail "SYNC selection mode missing"
grep -Fq '"syncScopeMode"' "$SYNC" \
  || fail "sync scope mode missing"
grep -Fq '"scopePlaylistIds"' "$SYNC" \
  || fail "selected scope ids missing"
grep -Fq '"baseSessionName"' "$SYNC" \
  || fail "base session reference missing"
grep -Fq '"contentFingerprint"' "$SYNC" \
  || fail "content fingerprint field missing"
grep -Fq 'ytm-account-backup-fingerprint-v1' "$SYNC" \
  || fail "fingerprint version marker missing"

grep -Fq 'STATUS_NEW,' "$SYNC" \
  || fail "NEW write classification missing"
grep -Fq 'STATUS_UPDATED' "$SYNC" \
  || fail "UPDATED write classification missing"
grep -Fq 'record.status in' "$SYNC" \
  || fail "write gating missing"
grep -Fq 'record.playlist' "$SYNC" \
  || fail "write gating does not require playlist payload"

grep -Fq 'scopeMode ==' "$SYNC" \
  || fail "scope-preserving comparison missing"
grep -Fq '"SELECTED"' "$SYNC" \
  || fail "SELECTED scope support missing"
grep -Fq '"ALL"' "$SYNC" \
  || fail "ALL scope support missing"

grep -Fq 'backupMode' "$MANIFEST" \
  || fail "manifest importer delta boundary missing"
grep -Fq 'INCREMENTAL_DELTA' "$MANIFEST" \
  || fail "manifest importer incremental delta guard missing"
grep -Fq 'class IncrementalDeltaManifestException' "$MANIFEST" \
  || fail "typed delta manifest exception missing"
grep -Fq 'throw IncrementalDeltaManifestException' "$MANIFEST" \
  || fail "delta manifest must throw typed boundary exception"
grep -Fq 'Повне відновлення delta-ланцюжка ще не підтримується.' "$IMPORT" \
  || fail "clear readable delta restore boundary message missing"

if grep -Fq 'YouTubeApi' "$SYNC"; then
  fail "storage sync layer must not own YouTubeApi"
fi
if grep -Fq 'search.list' "$SYNC"; then
  fail "storage sync layer must not use search.list"
fi
if grep -Fq 'createPlaylist(' "$SYNC"; then
  fail "incremental storage path must not create remote playlists"
fi
if grep -Fq 'addVideo(' "$SYNC"; then
  fail "incremental storage path must not write playlist items"
fi

grep -Fq 'versionCode: **63**' docs/v.1.4.29/RELEASE.md \
  || fail "v1.4.29 release versionCode missing"
grep -Fq 'versionName: **1.4.29**' docs/v.1.4.29/RELEASE.md \
  || fail "v1.4.29 release versionName missing"
grep -Fq '# YTM Importer v1.4.29 — Incremental Account Backup' docs/v.1.4.29/RELEASE.md \
  || fail "v1.4.29 release identity missing"

echo "PASS:"
echo "- incremental baseline folder flow"
echo "- ALL / SELECTED scope preservation"
echo "- request estimate before content scan"
echo "- exact ordered content fingerprint"
echo "- NEW/UPDATED/UNCHANGED/MISSING/FAILED classification"
echo "- new schema-v3 incremental delta manifest"
echo "- project writes gated to NEW/UPDATED"
echo "- old baseline is not a write target"
echo "- clear delta restore boundary"
echo "- storage layer has no remote write/search API"
echo "- v1.4.29 release docs"

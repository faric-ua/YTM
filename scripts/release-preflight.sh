#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

echo "YTM Importer release preflight"
echo "========================="

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

check_file() {
  [ -f "$1" ] || fail "Missing required file: $1"
}

check_file "app/build.gradle.kts"
check_file "app/src/main/AndroidManifest.xml"
check_file ".github/workflows/build-apk.yml"
check_file "TERMUX_ORGANIZE_DOWNLOAD_ZIPS.txt"
check_file "docs/v.1.4.30/RELEASE.md"
check_file "docs/v.1.4.30/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.31/RELEASE.md"
check_file "docs/v.1.4.31/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.31/qa/PHONE_TEST.md"
check_file "docs/v.1.4.31/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.32/RELEASE.md"
check_file "docs/v.1.4.32/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.32/qa/PHONE_TEST.md"
check_file "docs/v.1.4.32/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.33/RELEASE.md"
check_file "docs/v.1.4.33/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.33/qa/PHONE_TEST.md"
check_file "docs/v.1.4.33/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.34/RELEASE.md"
check_file "docs/v.1.4.34/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.34/qa/PHONE_TEST.md"
check_file "docs/v.1.4.34/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.35/RELEASE.md"
check_file "docs/v.1.4.35/SAF_AUDIT.md"
check_file "docs/v.1.4.35/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.35/qa/PHONE_TEST.md"
check_file "docs/v.1.4.35/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.36/RELEASE.md"
check_file "docs/v.1.4.36/FILE_SAVE_AUDIT.md"
check_file "docs/v.1.4.36/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.36/qa/PHONE_TEST.md"
check_file "docs/v.1.4.36/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.37/RELEASE.md"
check_file "docs/v.1.4.37/UX_AUDIT.md"
check_file "docs/v.1.4.37/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.37/qa/PHONE_TEST.md"
check_file "docs/v.1.4.37/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.38/RELEASE.md"
check_file "docs/v.1.4.38/UX_AUDIT.md"
check_file "docs/v.1.4.38/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.38/qa/PHONE_TEST.md"
check_file "docs/v.1.4.38/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.38/R1.md"
check_file "docs/v.1.4.38/R2.md"
check_file "docs/v.1.4.39/RELEASE.md"
check_file "docs/v.1.4.39/UX_AUDIT.md"
check_file "docs/v.1.4.39/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.39/qa/PHONE_TEST.md"
check_file "docs/v.1.4.39/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.40/RELEASE.md"
check_file "docs/v.1.4.40/UI_AUDIT.md"
check_file "docs/v.1.4.40/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.40/qa/PHONE_TEST.md"
check_file "docs/v.1.4.40/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.41/RELEASE.md"
check_file "docs/v.1.4.41/UX_AUDIT.md"
check_file "docs/v.1.4.41/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.41/qa/PHONE_TEST.md"
check_file "docs/v.1.4.41/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.41/R1.md"
check_file "docs/v.1.4.41/R2.md"
check_file "docs/v.1.4.42/RELEASE.md"
check_file "docs/v.1.4.42/FILE_OPEN_AUDIT.md"
check_file "docs/v.1.4.42/qa/PHONE_TEST.md"
check_file "docs/v.1.4.42/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.42/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.42/R1.md"
check_file "docs/v.1.4.43/RELEASE.md"
check_file "docs/v.1.4.43/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.43/qa/PHONE_TEST.md"
check_file "docs/v.1.4.43/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.44/RELEASE.md"
check_file "docs/v.1.4.44/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.44/qa/PHONE_TEST.md"
check_file "docs/v.1.4.44/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.44/R1.md"
check_file "docs/v.1.4.45/RELEASE.md"
check_file "docs/v.1.4.45/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.45/qa/PHONE_TEST.md"
check_file "docs/v.1.4.45/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.46/RELEASE.md"
check_file "docs/v.1.4.46/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.46/qa/PHONE_TEST.md"
check_file "docs/v.1.4.46/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.47/RELEASE.md"
check_file "docs/v.1.4.47/REGRESSION_CHECKLIST.md"
check_file "docs/v.1.4.47/qa/PHONE_TEST.md"
check_file "docs/v.1.4.47/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.47/R1.md"
check_file "docs/v.1.4.47/qa/PHONE_TEST_R1.md"
check_file "docs/v.1.4.47/R2.md"
check_file "docs/v.1.4.47/qa/PHONE_TEST_R2.md"
check_file "docs/v.1.4.47/R3.md"
check_file "docs/v.1.4.47/qa/PHONE_TEST_R3.md"
check_file "docs/v.1.4.47/qa/R3_LIFECYCLE_WAVE1.md"
check_file "docs/v.1.4.47/qa/R3_OAUTH_RETRY.md"
check_file "docs/v.1.4.47/qa/R3_HISTORY_SEMANTICS.md"

check_file "app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/ui/HomeDashboardChrome.kt"
check_file "app/src/main/java/com/saney/ytmimporter/QuotaActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/SafRecentFileQuery.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/AllFilesAccess.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/DirectDownloadFileQuery.kt"
check_file "app/src/main/res/xml/file_paths.xml"
check_file "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/DataActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/PendingActivity.kt"

check_file "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt"
check_file "scripts/mainactivity-audit.sh"
check_file "scripts/mainactivity-cleanup-audit.sh"
check_file "scripts/search-coordinator-audit.sh"
check_file "scripts/ui-chrome-audit.sh"
check_file "scripts/dialog-style-audit.sh"
check_file "scripts/button-layout-audit.sh"
check_file "scripts/compact-review-audit.sh"
check_file "scripts/action-hierarchy-audit.sh"
check_file "scripts/service-navigation-audit.sh"
check_file "scripts/dialog-bounds-audit.sh"
check_file "app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt"
check_file "app/src/main/java/com/saney/ytmimporter/auth/AuthSessionStore.kt"
check_file "app/src/main/java/com/saney/ytmimporter/auth/GoogleAccessTokenRecovery.kt"
check_file "app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"
check_file "scripts/configuration-state-audit.sh"
check_file "scripts/rotation-layout-audit.sh"
check_file "scripts/dialog-animation-audit.sh"
check_file "scripts/project-handoff-audit.sh"

bash scripts/mainactivity-audit.sh
bash scripts/mainactivity-cleanup-audit.sh
bash scripts/search-coordinator-audit.sh
bash scripts/playlist-write-coordinator-audit.sh
bash scripts/destination-coordinator-audit.sh
python -B scripts/v1426-apply-selftest.py
bash scripts/v1426-qa-close-audit.sh
bash scripts/v1426-selective-export-audit.sh
python -B scripts/v1427-apply-selftest.py
python -B scripts/v1427-exact-id-search-audit.py
bash scripts/v1427-qa-close-audit.sh
bash scripts/v1428-manifest-import-audit.sh
bash scripts/v1428-qa-close-audit.sh
bash scripts/v1429-incremental-backup-audit.sh
bash scripts/v1429-qa-close-audit.sh
bash scripts/v1430-delta-chain-audit.sh
bash scripts/v1430-qa-close-audit.sh
python -B scripts/v1430-delta-status-qa-selftest.py
bash scripts/v1430-delta-status-qa-close-audit.sh
bash scripts/v1431-auth-ui-audit.sh
bash scripts/v1432-dialog-first-frame-audit.sh
bash scripts/v1433-dialog-unification-audit.sh
bash scripts/v1434-back-navigation-audit.sh
bash scripts/v1435-saf-navigation-audit.sh
bash scripts/v1436-saf-file-save-audit.sh
bash scripts/v1437-fullscreen-utility-ui-audit.sh
bash scripts/v1438-selectors-safety-copy-audit.sh
bash scripts/v1438-r1-audit.sh
bash scripts/v1438-r2-audit.sh
bash scripts/v1439-history-json-restore-audit.sh
bash scripts/v1440-release-history-audit.sh
bash scripts/v1441-auth-ui-consistency-audit.sh
bash scripts/v1441-r1-audit.sh
bash scripts/v1441-r2-audit.sh
bash scripts/v1442-recent-file-selector-audit.sh
bash scripts/v1442-r1-all-files-audit.sh
bash scripts/v1443-auth-freshness-audit.sh
bash scripts/v1444-adaptive-actions-audit.sh
bash scripts/v1444-r1-audit.sh
bash scripts/v1445-title-emphasis-audit.sh
bash scripts/v1446-home-layout-audit.sh
bash scripts/v1447-playlist-hub-audit.sh
bash scripts/v1447-r1-audit.sh
bash scripts/v1447-r2-audit.sh
bash scripts/v1447-r3-lifecycle-wave1-audit.sh
bash scripts/v1447-r3-oauth-retry-audit.sh
bash scripts/v1447-r3-history-semantics-audit.sh
bash scripts/v1447-r3-consolidation-audit.sh
bash scripts/v1447-r3-navigation-ownership-audit.sh
bash scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh
bash scripts/project-handoff-audit.sh
bash scripts/auth-persistence-audit.sh
bash scripts/result-modal-audit.sh
bash scripts/qa-plan-audit.sh
bash scripts/ui-chrome-audit.sh
bash scripts/dialog-style-audit.sh
bash scripts/button-layout-audit.sh
bash scripts/compact-review-audit.sh
bash scripts/action-hierarchy-audit.sh
bash scripts/service-navigation-audit.sh
bash scripts/dialog-bounds-audit.sh
bash scripts/configuration-state-audit.sh
bash scripts/rotation-layout-audit.sh
bash scripts/dialog-animation-audit.sh
check_file "app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/SafTreeAccess.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/SafTreeFileWriter.kt"
check_file "app/src/main/java/com/saney/ytmimporter/ui/SafFileSaveFlow.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryManifestImporter.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryIncrementalBackup.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/AccountLibraryDeltaChainRestorer.kt"
check_file "app/src/main/java/com/saney/ytmimporter/storage/AccountBackupNaming.kt"
check_file "OPEN_QUESTIONS.md"
check_file "RELEASE_TEST_STATUS.md"
check_file "docs/v.1.4.17/qa/BUG_REGISTER.md"
check_file "docs/v.1.4.17/qa/TEST_DATA.md"
check_file "docs/v.1.4.17/qa/TEST_RUN_TEMPLATE.md"
check_file "docs/v.1.4.17/qa/RELEASE_TEST_PLAN.md"
check_file "docs/v.1.4.17/qa/MASTER_TEST_PLAN.md"
check_file "qa/BUG_REGISTER.md"
check_file "scripts/playlist-write-coordinator-audit.sh"
check_file "scripts/destination-coordinator-audit.sh"
check_file "scripts/v1417-auth-flow-audit.sh"
check_file "scripts/v1418-account-library-import-audit.sh"
check_file "scripts/v1419-account-library-export-audit.sh"
check_file "scripts/v1420-import-button-layout-audit.sh"
check_file "scripts/v1422-visual-structure-audit.sh"
check_file "scripts/v1423-button-fit-audit.sh"
check_file "scripts/v1425-accent-card-audit.sh"
check_file "scripts/v1425-apply-selftest.py"
check_file "scripts/v1426-apply-selftest.py"
check_file "scripts/v1427-apply-selftest.py"
check_file "scripts/v1427-exact-id-search-audit.py"
check_file "scripts/v1427-qa-close-audit.sh"
check_file "scripts/v1428-manifest-import-audit.sh"
check_file "scripts/v1428-qa-close-audit.sh"
check_file "scripts/v1429-incremental-backup-audit.sh"
check_file "scripts/v1429-qa-close-audit.sh"
check_file "scripts/v1430-delta-chain-audit.sh"
check_file "scripts/v1430-qa-close-audit.sh"
check_file "scripts/v1430-delta-status-qa.py"
check_file "scripts/v1430-delta-status-qa-selftest.py"
check_file "scripts/v1430-delta-status-qa-close-audit.sh"
check_file "scripts/v1431-auth-ui-audit.sh"
check_file "scripts/v1432-dialog-first-frame-audit.sh"
check_file "scripts/v1433-dialog-unification-audit.sh"
check_file "scripts/v1434-back-navigation-audit.sh"
check_file "scripts/v1435-saf-navigation-audit.sh"
check_file "scripts/v1436-saf-file-save-audit.sh"
check_file "scripts/v1437-fullscreen-utility-ui-audit.sh"
check_file "scripts/v1438-selectors-safety-copy-audit.sh"
check_file "scripts/v1438-r1-audit.sh"
check_file "scripts/v1438-r2-audit.sh"
check_file "scripts/v1439-history-json-restore-audit.sh"
check_file "scripts/v1440-release-history-audit.sh"
check_file "scripts/v1441-auth-ui-consistency-audit.sh"
check_file "scripts/v1441-r1-audit.sh"
check_file "scripts/v1441-r2-audit.sh"
check_file "scripts/v1442-recent-file-selector-audit.sh"
check_file "scripts/v1442-r1-all-files-audit.sh"
check_file "scripts/v1443-auth-freshness-audit.sh"
check_file "scripts/v1444-adaptive-actions-audit.sh"
check_file "scripts/v1444-r1-audit.sh"
check_file "scripts/v1445-title-emphasis-audit.sh"
check_file "scripts/v1446-home-layout-audit.sh"
check_file "scripts/v1447-playlist-hub-audit.sh"
check_file "scripts/v1447-r1-audit.sh"
check_file "scripts/v1447-r2-audit.sh"
check_file "scripts/v1447-r3-lifecycle-wave1-audit.sh"
check_file "scripts/v1447-r3-oauth-retry-audit.sh"
check_file "scripts/v1447-r3-oauth-retry-selftest.py"
check_file "scripts/v1447-r3-history-semantics-audit.sh"
check_file "scripts/v1447-r3-history-semantics-selftest.py"
check_file "scripts/v1447-r3-consolidation-audit.sh"
check_file "scripts/v1447-r3-navigation-ownership-audit.sh"
check_file "scripts/v1447-r3-navigation-ownership-r4-fix2-audit.sh"
check_file "app/src/main/java/com/saney/ytmimporter/review/ReviewRemoteOperations.kt"
check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationRemoteOperations.kt"
check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationForwardedWritePlan.kt"
check_file "scripts/v1426-selective-export-audit.sh"
check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"
check_file "app/src/main/java/com/saney/ytmimporter/write/PlaylistWriteCoordinator.kt"
check_file "qa/TEST_DATA.md"
check_file "qa/TEST_RUN_TEMPLATE.md"
check_file "qa/MASTER_TEST_PLAN.md"
check_file "scripts/qa-plan-audit.sh"
check_file "scripts/result-modal-audit.sh"
check_file "scripts/auth-persistence-audit.sh"
check_file "app/src/main/java/com/saney/ytmimporter/auth/PersistentAuthStateStore.kt"

grep -q 'android:name=".ImportActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "ImportActivity is missing from manifest"

grep -Fq 'RecentFileChooserActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/ImportActivity.kt \
  || fail "ImportActivity recent-file selector path missing"

grep -Fq 'RecentFileChooserActivity.EXTRA_MIME_TYPE' \
  app/src/main/java/com/saney/ytmimporter/ImportActivity.kt \
  || fail "ImportActivity recent-file MIME contract missing"

grep -Fq '"*/*"' \
  app/src/main/java/com/saney/ytmimporter/ImportActivity.kt \
  || fail 'Import fallback picker must remain permissive "*/*"'

grep -Fq 'Intent.ACTION_OPEN_DOCUMENT' \
  app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt \
  || fail "Recent-file selector system fallback picker missing"

grep -q 'android:name=".ReviewActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "ReviewActivity is missing from manifest"

grep -q 'android:name=".DestinationActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "DestinationActivity is missing from manifest"

grep -q 'android:name=".ServiceActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "ServiceActivity is missing from manifest"

grep -q 'ServiceActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open ServiceActivity"

grep -q 'DestinationActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open DestinationActivity"

grep -q 'ACTION_CONFIRM_EXISTING' \
  app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt \
  || fail "Destination existing-playlist result contract is missing"

grep -q 'DUPLICATE_MODE_SKIP' \
  app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt \
  || fail "Destination duplicate choice contract is missing"

grep -q 'Q-001' OPEN_QUESTIONS.md \
  || fail "Deferred UX question Q-001 is not documented"

grep -q 'Q-002' OPEN_QUESTIONS.md \
  || fail "Dialog-motion issue Q-002 is not documented"

grep -Fq '| v1.4.12 | **NOT TESTED** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.12 must remain explicitly marked NOT TESTED"

grep -Fq '| v1.4.14 | **PARTIALLY PHONE-TESTED — FAIL** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.14 auth failure status missing"

grep -q 'ImportActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open ImportActivity"

grep -q 'ReviewActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open ReviewActivity"

grep -q 'EXTRA_MANUAL_VIDEO_ID' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review manual URL result contract is missing"

grep -q 'EXTRA_REPEAT_SEARCH' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Review repeat-search result contract is missing"

grep -q 'current_playlist_v1' \
  app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt \
  || fail "Current playlist persistence is missing"

grep -q 'exportWorkingPlaylist' \
  app/src/main/java/com/saney/ytmimporter/storage/PlaylistProjectCodec.kt \
  || fail "Working-list YTM Project export is missing"

grep -q 'resolveCurrentTrack' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "Manual URL canonical-track guard is missing"

grep -q 'track.manuallySelected &&' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "Manual selection sticky guard is missing"

grep -q 'current_playlist_v1' \
  app/src/main/java/com/saney/ytmimporter/storage/LocalBackupManager.kt \
  || fail "Full Backup does not include current workspace"

grep -q 'Ручний вибір:' \
  app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt \
  || fail "Clear manual-selection wording is missing from Main list"

grep -q 'Project «' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Project save confirmation does not include project name"

grep -q 'OpenableColumns.DISPLAY_NAME' \
  app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt \
  || fail "Project save confirmation does not read final document filename"

grep -q 'android:name=".PendingActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "PendingActivity is missing from manifest"

grep -q 'PendingActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open PendingActivity"

grep -q 'EXTRA_RESUME_JOB_ID' \
  app/src/main/java/com/saney/ytmimporter/PendingActivity.kt \
  || fail "PendingActivity resume result contract is missing"

grep -q 'android:name=".DataActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "DataActivity is missing from manifest"

grep -q 'DataActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open DataActivity"

grep -q 'android:name=".HistoryActivity"' \
  app/src/main/AndroidManifest.xml \
  || fail "HistoryActivity is missing from manifest"

grep -q 'HistoryActivity::class.java' \
  app/src/main/java/com/saney/ytmimporter/MainActivity.kt \
  || fail "MainActivity does not open HistoryActivity"

grep -q 'applicationId = "com.saney.ytmimporter"' app/build.gradle.kts \
  || fail "Unexpected applicationId"

grep -q 'versionCode = 90' app/build.gradle.kts \
  || fail "Expected versionCode = 90"

grep -q 'versionName = "1.4.47-R3"' app/build.gradle.kts \
  || fail 'Expected versionName = "1.4.47-R3"'

grep -q 'buildConfig = true' app/build.gradle.kts \
  || fail "BuildConfig generation is not enabled"

grep -q 'compileSdk = 36' app/build.gradle.kts \
  || fail "Expected compileSdk = 36"

grep -q 'targetSdk = 36' app/build.gradle.kts \
  || fail "Expected targetSdk = 36"

tracked_secrets="$(
  git ls-files |
    grep -E '(^|/)(release-signing\.properties|[^/]+\.(jks|keystore))$' ||
    true
)"

if [ -n "$tracked_secrets" ]; then
  echo "$tracked_secrets"
  fail "Signing material is tracked by Git"
fi

if grep -q 'uses:[[:space:]]*android-actions/setup-android@v3' \
  .github/workflows/build-apk.yml; then
  fail "Obsolete android-actions/setup-android@v3 is active again"
fi

grep -q 'Locate Android SDK' .github/workflows/build-apk.yml \
  || fail "Android SDK locator hotfix is missing"


grep -q 'title = "Повний backup"' \
  app/src/main/java/com/saney/ytmimporter/DataActivity.kt \
  || fail "Dedicated DataActivity full-backup UI is missing"

grep -q 'secondLabel = "History JSON"' \
  app/src/main/java/com/saney/ytmimporter/DataActivity.kt \
  || fail "Dedicated DataActivity History JSON action is missing"

grep -q 'uses:[[:space:]]*actions/setup-java@v5' \
  .github/workflows/build-apk.yml \
  || fail "Expected actions/setup-java@v5"

grep -q 'name:[[:space:]]*Verify signed APK' \
  .github/workflows/build-apk.yml \
  || fail "APK verification step is missing"

grep -q 'verify --verbose --print-certs' \
  .github/workflows/build-apk.yml \
  || fail "apksigner verification command is missing"

grep -q 'ZIPALIGN.*-c -v 4' \
  .github/workflows/build-apk.yml \
  || grep -q '"$ZIPALIGN" -c -v 4 "$APK"' \
  .github/workflows/build-apk.yml \
  || fail "zipalign verification is missing"

grep -q 'sha256sum' \
  .github/workflows/build-apk.yml \
  || fail "APK SHA-256 generation is missing"

echo
echo "PASS:"
echo "- package id"
echo "- release version"
echo "- BuildConfig"
echo "- SDK levels"
echo "- signing material not tracked"
echo "- Android SDK workflow hotfix"
echo "- Data dialog string guard"
echo "- setup-java v5"
echo "- APK signature verification step"
echo "- APK zipalign verification step"
echo "- APK SHA-256 generation"
echo "- release docs"
echo "- dedicated HistoryActivity navigation"
echo "- dedicated DataActivity navigation"
echo "- dedicated PendingActivity navigation"
echo "- Pending Queue resume result contract"
echo "- dedicated ImportActivity navigation"
echo "- dedicated ReviewActivity navigation"
echo "- dedicated DestinationActivity navigation"
echo "- legacy destination dialogs removed from MainActivity"
echo "- DestinationActivity bridge/write core retained"
echo "- safe-area insets on primary screens"
echo "- styled More/Import/Project dialogs"
echo "- destination duplicate/final-confirm result contract"
echo "- deferred UX question Q-001 documented"
echo "- persistent current playlist workspace"
echo "- Review manual URL result contract"
echo "- Review repeat-search result contract"
echo "- working-list YTM Project export"
echo "- manual URL canonical-track guard"
echo "- manual selections protected from cache overwrite"
echo "- Full Backup includes current workspace"
echo "- clear manual-selection wording"
echo "- Project save toast includes project name"
echo "- Project save toast includes actual document filename"

echo "- all dialogs routed through unified YTM theme"
echo "- all menu lists converted to card-button menus"
echo "- larger text padding in app buttons"

echo "- adaptive button layouts and filter grid"
echo "- state-aware main flow colors"

echo "- quota action hierarchy"
echo "- compact Review toolbar + filters"
echo "- playlist-based Project filename"

echo "- dialog action hierarchy standardized"
echo "- Service moved to dedicated styled screen"

echo "- Service nested navigation stays in ServiceActivity"
echo "- structured problem-track tiles"
echo "- Service diagnostics/about/searchcache structured screens"

echo "- custom dialog safe viewport / top-clipping guard"
echo "- tall custom dialogs scroll from visible top"
echo "- short custom dialogs remain centered"

echo "- auth session survives rotation in process memory"
echo "- OAuth token remains non-persistent"
echo "- custom dialogs reveal only after final insets"
echo "- no visible center-to-top dialog snap"

echo "- horizontal action rows ignore child text baselines"
echo "- Step 2 keeps the same vertical bounds after rotation"
echo "- custom dialogs are top anchored without center-to-top relayout"

echo "- all runtime modal paths converge on UiChrome stable Dialog pipeline"
echo "- attached Dialog decor stays hidden until inset + geometry stabilization"

echo "- Cleanup wave 2: legacy MainActivity UI flows removed"
echo "- dedicated Import/Review/History/Data/Pending/Service screens retained"
echo "- in-app recent-file selector added; permissive Android picker retained as fallback"
echo "- Q-002 unified modal fix implemented; representative phone retest still required"

echo "- v1.4.12 explicitly marked NOT TESTED"
echo "- SearchCoordinator owns track-search domain orchestration"
echo "- MainActivity search role reduced to auth/UI bridge"

echo "- silent Google/YTM recovery marker is non-secret and token-free"
echo "- playlist completion result is modal, not an inline Home frame"
echo "- global master QA plan is present"

grep -Fq '| v1.4.17 | **PARTIALLY PHONE-TESTED — PASS FOR TESTED PATH** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.17 tested-path status missing"
grep -Fq '| v1.4.18 | **PARTIALLY PHONE-TESTED — PASS FOR G01** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.18 G01 phone-test PASS status missing"
grep -Fq '| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.19 bulk-export phone-test PASS status missing"
grep -Fq '| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.20 UI-smoke PASS status missing"
grep -Fq '| v1.4.21 | **PARTIALLY PHONE-TESTED — PASS FOR HOME THEMES** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.21 Home-theme PASS status missing"
grep -Fq '| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.22 UI-fit phone status missing"
grep -Fq '| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.23 Home-fit PASS status missing"
grep -Fq '| v1.4.24 | **PARTIALLY PHONE-TESTED — PASS WITH UI POLISH FINDINGS** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.24 Wave 2 phone status missing"
grep -Fq '| v1.4.25 | **PARTIALLY PHONE-TESTED — PASS FOR ACCENT CARD TESTED PATHS** |' RELEASE_TEST_STATUS.md \
  || fail "v1.4.25 tested-path phone status missing"
grep -Fq '| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |' qa/BUG_REGISTER.md \
  || fail "BUG-003 closed phone-retest status missing"
echo "- BUG-003 in-place update recovery phone retest passed"
echo "- PlaylistWriteCoordinator extracted"
echo "- per-release QA snapshot included"

echo "- DestinationCoordinator extracted"

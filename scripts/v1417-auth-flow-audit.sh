#!/usr/bin/env bash
set -euo pipefail

fail() { echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"

grep -q 'versionCode = 51' app/build.gradle.kts || fail "versionCode"
grep -q 'versionName = "1.4.17"' app/build.gradle.kts || fail "versionName"
grep -q 'EXTRA_OPEN_DESTINATION' "$REVIEW" || fail "Review extra"
grep -Fq 'Далі → Створити / додати' "$REVIEW" || fail "Review CTA"
grep -q 'ReviewActivity.EXTRA_OPEN_DESTINATION' "$MAIN" || fail "Main bridge"
grep -q 'invalidateAuthorizationIfNeeded' "$MAIN" || fail "auth invalidation"
grep -q 'httpCode == 401' "$MAIN" || fail "401 guard"
grep -q 'DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE' "$MAIN" || fail "result layout"
grep -Fq 'APK_NAME=YTM-Importer-v${APP_VERSION}-release.apk' .github/workflows/build-apk.yml || fail "dynamic APK"

echo "PASS: v1.4.17 auth/flow guards"

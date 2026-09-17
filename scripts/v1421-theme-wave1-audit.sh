#!/usr/bin/env bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
grep -q 'versionCode = 55' app/build.gradle.kts || fail versionCode
grep -q 'versionName = "1.4.21"' app/build.gradle.kts || fail versionName
THEME=app/src/main/java/com/saney/ytmimporter/ui/AppThemeManager.kt
[ -f "$THEME" ] || fail theme-file
grep -q NEON_DARK "$THEME" || fail neon
grep -q BLUE_DARK "$THEME" || fail blue
grep -q GREEN_DARK "$THEME" || fail green
grep -q showThemePicker app/src/main/java/com/saney/ytmimporter/MainActivity.kt || fail picker
grep -q 'AppThemeManager.applyWindow(this)' app/src/main/java/com/saney/ytmimporter/MainActivity.kt || fail main-theme
grep -q 'AppThemeManager.applyWindow(this)' app/src/main/java/com/saney/ytmimporter/ImportActivity.kt || fail import-theme
grep -q 'AppThemeManager.applyWindow(this)' app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt || fail review-theme
grep -q 'AppThemeManager.palette(context)' app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt || fail adapter-theme
echo 'PASS: v1.4.21 Theme System Wave 1 guards'

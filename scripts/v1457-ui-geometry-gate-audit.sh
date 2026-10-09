#!/usr/bin/env bash
set -euo pipefail

fail() { echo "FAIL: $1" >&2; exit 1; }

UI="app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt"
TEST="app/src/androidTest/java/com/saney/ytmimporter/ui/AdaptiveActionsGeometryTest.kt"
FIXTURE="app/src/debug/java/com/saney/ytmimporter/ui/UiGeometryTestActivity.kt"
MANIFEST="app/src/debug/AndroidManifest.xml"
CI=".github/workflows/validate.yml"

for file in "$UI" "$TEST" "$FIXTURE" "$MANIFEST" "$CI" app/build.gradle.kts; do
  test -s "$file" || fail "missing UI geometry gate owner: $file"
done

grep -Fq 'container.addOnLayoutChangeListener(measureListener)' "$UI" ||
  fail 'the shared renderer no longer measures actual container bounds'
grep -Fq 'button.paint.measureText(button.text.toString())' "$UI" ||
  fail 'adaptive actions no longer measure current caption font width'
grep -Fq 'actionWidthsDp.maxOrNull()' "$UI" ||
  fail 'equal-weight rows no longer reserve the widest peer caption'
grep -Fq 'ViewGroup.LayoutParams.WRAP_CONTENT' "$UI" ||
  fail 'adaptive actions no longer support accessible wrapped height'
grep -Fq 'ui_chrome_action_layout_listener' "$UI" ||
  fail 'adaptive action observer lifecycle tag missing'
grep -Fq 'fun previewQuickActions_showCompleteCaptionsInsidePaddedCard()' "$TEST" ||
  fail 'Bulk preview clipping regression assertion missing'
grep -Fq 'fun sessionFooter_staysVisibleAndKeepsCompleteLongActionAtLargeTextSize()' "$TEST" ||
  fail 'Bulk Session clipping/viewport assertion missing'
grep -Fq 'fun rotationRebuildsSafeActionLayoutWithoutClickingAnything()' "$TEST" ||
  fail 'rotation with no action execution assertion missing'
grep -Fq 'never open Bulk Sync, read an account or contact YouTube' "$TEST" ||
  fail 'isolated synthetic test fixture contract missing'
grep -Fq 'androidTestImplementation("androidx.test.ext:junit:' app/build.gradle.kts ||
  fail 'Android instrumented JUnit not configured'
grep -Fq 'ui-geometry:' "$CI" ||
  fail 'Android emulator UI gate missing from Validate workflow'
grep -Fq 'connectedDebugAndroidTest' "$CI" ||
  fail 'Validate does not run real Android instrumentation tests'
grep -Fq 'UiGeometryTestActivity' "$MANIFEST" ||
  fail 'test-only activity is not registered for debug builds'

REAL_TEST="app/src/androidTest/java/com/saney/ytmimporter/ui/BulkRealScreenGeometryTest.kt"
REAL_PREVIEW="app/src/main/java/com/saney/ytmimporter/BulkSyncPreviewActivity.kt"
test -s "$REAL_TEST" || fail 'actual Bulk screen instrumented tests missing'
grep -Fq 'fun actualPreview_showsBothQuickSelectionLabelsInItsNestedCard()' "$REAL_TEST" ||
  fail 'actual Preview nested-card caption assertion missing'
grep -Fq 'fun actualSession_readyActionsStayFullyVisibleAboveLongPlan()' "$REAL_TEST" ||
  fail 'actual Session footer geometry assertion missing'
grep -Fq 'fun actualSession_landscapeAndPortrait_keepFooterVisibleWithoutWrite()' "$REAL_TEST" ||
  fail 'actual Session rotation and zero-mutation assertion missing'
grep -Fq 'if (BuildConfig.DEBUG)' "$REAL_PREVIEW" ||
  fail 'read-only preview fixture is not guarded as debug-only'
grep -Fq 'EXTRA_DEBUG_READ_ONLY_PLAN' "$REAL_PREVIEW" ||
  fail 'actual Preview cannot bypass remote preflight for instrumented geometry'
grep -Fq 'assertEquals(0, stored?.mutationLedger?.size)' "$REAL_TEST" ||
  fail 'actual screen fixture lost its no-remote-mutation assertion'

echo "PASS: measured adaptive actions + blocking emulator + real Bulk Activity UI assertions"

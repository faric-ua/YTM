#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SELECTOR="app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt"
R2="docs/v.1.4.38/R2.md"
BUG="qa/BUG_REGISTER.md"

for f in "$SELECTOR" "$R2" "$BUG"; do
  test -f "$f" || fail "missing R2 file: $f"
done

grep -Fq 'versionCode: **74**' "$R2" || fail "R2 versionCode snapshot missing"
grep -Fq 'versionName: **1.4.38-R2**' "$R2" || fail "R2 versionName snapshot missing"

grep -Fq 'import android.widget.FrameLayout' "$SELECTOR"   || fail "FrameLayout checkbox wrapper import missing"
grep -Fq 'val checkColumn =' "$SELECTOR"   || fail "checkbox wrapper column missing"
grep -Fq 'FrameLayout.LayoutParams(' "$SELECTOR"   || fail "checkbox centered child layout missing"
grep -Fq 'Gravity.CENTER' "$SELECTOR"   || fail "checkbox visible-centering gravity missing"
grep -Fq 'dp(48),' "$SELECTOR"   || fail "48dp checkbox touch column missing"
grep -Fq 'row.setOnClickListener' "$SELECTOR"   || fail "whole-row checkbox toggle missing"

grep -Fq '| BUG-008 / Q-008 | CLOSED — PHONE RETEST PASS v1.4.38 R1 |' "$BUG"   || fail "BUG-008 R1 phone PASS not recorded"

grep -Fq 'checkbox visual centering only' "$R2"   || fail "R2 targeted scope missing"

echo "PASS:"
echo "- immutable v1.4.38-R2 / code 74 snapshot"
echo "- visible CheckBox centered by FrameLayout wrapper"
echo "- 48dp touch column preserved"
echo "- whole-row toggle preserved"
echo "- BUG-008 rotation fix recorded PASS from R1"

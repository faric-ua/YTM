#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"

for old in resultPanel resultTitleText resultDetailsText resultLinkText; do
  if grep -q "$old" "$MAIN"; then
    fail "old inline result frame still exists: $old"
  fi
done

grep -A100 'private fun showPlaylistResult' "$MAIN" |
  grep -q 'UiChrome.showMessageDialog' \
  || fail "playlist result is not a modal UiChrome dialog"

grep -A100 'private fun showPlaylistResult' "$MAIN" |
  grep -q 'label = "Відкрити в YTM"' \
  || fail "Open in YTM result action missing"

grep -A100 'private fun showPlaylistResult' "$MAIN" |
  grep -q 'label = "Копіювати"' \
  || fail "Copy result action missing"

grep -A100 'private fun showPlaylistResult' "$MAIN" |
  grep -q 'label = "Закрити"' \
  || fail "Close result action missing"

echo "PASS:"
echo "- old inline result frame removed"
echo "- create/append result uses modal dialog"
echo "- open/copy/close actions are present"

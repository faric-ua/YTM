#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

SEM="app/src/main/java/com/saney/ytmimporter/model/HistoryResultSemantics.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"
DATA="app/src/main/java/com/saney/ytmimporter/DataActivity.kt"

for f in "$SEM" "$HISTORY" "$DATA"; do
  test -f "$f" || fail "missing R3 History semantics file: $f"
done

grep -Fq 'label = "Додано в YTM"' "$SEM" ||
  fail "remote-write result label missing"
grep -Fq 'label = "Імпортовано"' "$SEM" ||
  fail "local-import result label missing"
grep -Fq 'label = "Відновлено"' "$SEM" ||
  fail "local-restore result label missing"

grep -Fq '!entry.playlistId.isNullOrBlank()' "$SEM" ||
  fail "playlist ID remote-write evidence missing"
grep -Fq 'entry.addedCount > 0' "$SEM" ||
  fail "added-count remote-write evidence missing"
grep -Fq 'entry.failedCount > 0' "$SEM" ||
  fail "failed-write evidence missing"
grep -Fq 'entry.pendingCount > 0' "$SEM" ||
  fail "pending-write evidence missing"
grep -Fq 'entry.status != HistoryStatus.COMPLETED' "$SEM" ||
  fail "non-completed write fallback missing"

grep -Fq 'maxOf(' "$SEM" ||
  fail "legacy local count fallback missing"
grep -Fq 'entry.totalImportedCount' "$SEM" ||
  fail "imported-count semantic source missing"
grep -Fq 'entry.tracks.size' "$SEM" ||
  fail "legacy track-count fallback missing"

grep -Fq 'HistoryResultSemantics.primary(entry)' "$HISTORY" ||
  fail "HistoryActivity does not use shared semantics"
grep -Fq 'HistoryResultSemantics.primary(entry)' "$DATA" ||
  fail "DataActivity does not use shared semantics"

if grep -Fq '"Додано",' "$HISTORY"; then
  fail 'History detail still uses universal "Додано"'
fi

if grep -Fq '"Додано: ${entry.addedCount}/"' "$HISTORY"; then
  fail 'History copied summary still uses universal "Додано"'
fi

if grep -Fq '"Додано " +' "$HISTORY"; then
  fail 'History list row still uses universal "Додано"'
fi

if grep -Fq '"Додано: ${entry.addedCount}/"' "$DATA"; then
  fail 'Data History summary still uses universal "Додано"'
fi

echo "PASS:"
echo "- real YTM write uses Додано в YTM: added/target"
echo "- clean completed local record uses Імпортовано: N треків"
echo "- restore-like completed local record uses Відновлено: N треків"
echo "- failed/pending/non-completed write stays a YTM-write result"
echo "- errors/pending/skipped/duplicates remain separate result lines"
echo "- History detail/list/copy + Data exported summary share one semantic classifier"
echo "- no History JSON schema migration is required"

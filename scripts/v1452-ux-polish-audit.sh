#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

URL="app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt"
COMMITTER="app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotLocalCommitter.kt"
IMPORT="app/src/main/java/com/saney/ytmimporter/ImportActivity.kt"
MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
HOME_LINK="app/src/main/java/com/saney/ytmimporter/ui/HomeHistoryDetailLink.kt"
HISTORY="app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt"

for path in "$URL" "$COMMITTER" "$IMPORT" "$MAIN" "$HOME_LINK" "$HISTORY"; do
  test -f "$path" || fail "missing source: $path"
done

grep -Fq '"Всі (' "$URL" ||
  fail "compact All label missing"

grep -Fq '"Унікальні (' "$URL" ||
  fail "compact Unique label missing"

grep -Fq '"Скасувати"' "$URL" ||
  fail "duplicate chooser Cancel label missing"

if grep -Fq '"Зберегти всі (' "$URL"; then
  fail "old long All label still present"
fi

if grep -Fq '"Без повторів (' "$URL"; then
  fail "old long unique label still present"
fi

grep -Fq 'orientation =' "$URL" &&
grep -Fq 'LinearLayout.HORIZONTAL' "$URL" ||
  fail "horizontal duplicate chooser row missing"

grep -Fq 'val historyEntryId: String' "$COMMITTER" ||
  fail "commit receipt History id missing"

grep -Fq 'EXTRA_HISTORY_ENTRY_ID' "$URL" ||
  fail "UrlSnapshotActivity does not return History id"

grep -Fq 'EXTRA_IMPORT_HISTORY_ENTRY_ID' "$IMPORT" ||
  fail "ImportActivity History-id relay missing"

grep -Fq 'EXTRA_IMPORT_HISTORY_ENTRY_ID' "$MAIN" ||
  fail "MainActivity History-id receive path missing"

grep -Fq 'HomeHistoryDetailLink' "$MAIN" ||
  fail "MainActivity Home History-link bridge missing"

grep -Fq 'EXTRA_OPEN_ENTRY_ID' "$HOME_LINK" ||
  fail "Home History-link exact detail navigation missing"

grep -Fq 'EXTRA_OPEN_ENTRY_ID' "$HISTORY" ||
  fail "HistoryActivity exact-entry launch contract missing"

grep -Fq 'STATE_HISTORY_ENTRY_ID' "$HOME_LINK" ||
  fail "Home status History-id recreation state missing"

grep -Fq 'Деталі в Історії →' "$HOME_LINK" ||
  fail "Home detail affordance missing"

grep -Fq 'UX-027' docs/v.1.4.52/RELEASE.md ||
  fail "UX-027 release contract missing"

grep -Fq 'UX-028' docs/v.1.4.52/RELEASE.md ||
  fail "UX-028 release contract missing"

echo "PASS:"
echo "- UX-027 one-row compact duplicate chooser"
echo "- UX-028 exact History-entry relay"
echo "- Home detail affordance"
echo "- recreation state"
echo "- release contracts"

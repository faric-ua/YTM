#!/usr/bin/env bash
set -euo pipefail

fail(){ echo "FAIL: $1" >&2; exit 1; }

MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
PLAYLIST="app/src/main/java/com/saney/ytmimporter/PlaylistActivity.kt"
REVIEW="app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt"
STORE="app/src/main/java/com/saney/ytmimporter/storage/CurrentPlaylistStore.kt"
MANIFEST="app/src/main/AndroidManifest.xml"
RELEASE="docs/v.1.4.47/RELEASE.md"
PHONE="docs/v.1.4.47/qa/PHONE_TEST.md"

for f in "$MAIN" "$PLAYLIST" "$REVIEW" "$STORE" "$MANIFEST" "$RELEASE" "$PHONE"; do
  test -f "$f" || fail "missing v1.4.47 file: $f"
done

grep -Fq 'versionCode: **87**' "$RELEASE" ||
  fail "historical v1.4.47 versionCode evidence missing"
grep -Fq 'versionName: **1.4.47**' "$RELEASE" ||
  fail "historical v1.4.47 versionName evidence missing"

grep -Fq 'android:name=".PlaylistActivity"' "$MANIFEST" ||
  fail "PlaylistActivity missing from manifest"

grep -Fq 'setOnClickListener {' "$MAIN" ||
  fail "interactive Home card handlers missing"
grep -Fq 'showAccountDialog()' "$MAIN" ||
  fail "Home account card does not route to account dialog"
grep -Fq 'openPlaylistHub()' "$MAIN" ||
  fail "Home current-playlist card does not route to Playlist Hub"

if grep -Fq 'listView = ListView(this)' "$MAIN"; then
  fail "Home track ListView returned"
fi

grep -Fq 'Track rows live behind PlaylistActivity' "$MAIN" ||
  fail "clean-Home track-list migration marker missing"

for label in   'Треки / перевірка'   'Знайти / перевірити'   'Створити / додати в YTM'   'YTM Project / export'   'Заміни / проблемні треки'
do
  grep -Fq "$label" "$PLAYLIST" ||
    fail "Playlist Hub action missing: $label"
done

grep -Fq 'ReviewActivity::class.java' "$PLAYLIST" ||
  fail "Playlist Hub does not reuse ReviewActivity"
grep -Fq 'PlaylistActivity.ACTION_SEARCH' "$MAIN" ||
  fail "Playlist Hub search bridge missing"
grep -Fq 'PlaylistActivity.ACTION_CREATE' "$MAIN" ||
  fail "Playlist Hub create bridge missing"
grep -Fq 'Заміни / проблемні треки' "$PLAYLIST" ||
  fail "Playlist Hub replacements action missing"

grep -Fq 'EXTRA_OPEN_PROJECT_ACTIONS' "$REVIEW" ||
  fail "Review project-action bridge missing"

grep -Fq 'val destinationPlaylistId: String? = null' "$STORE" ||
  fail "destination playlist ID missing from snapshot"
grep -Fq 'destinationPlaylistId: String? = null' "$STORE" ||
  fail "destination playlist ID save contract missing"
grep -Fq 'private const val SCHEMA_VERSION =' "$STORE" ||
  fail "CurrentPlaylistStore schema marker missing"
grep -A1 -F 'private const val SCHEMA_VERSION =' "$STORE" |
  grep -Fq '2' ||
  fail "CurrentPlaylistStore schema is not v2"
grep -Fq 'in 1..SCHEMA_VERSION' "$STORE" ||
  fail "schema v1 backward-read contract missing"

grep -Fq 'destinationPlaylistId =' "$MAIN" ||
  fail "Main does not persist/restore destination playlist ID"
grep -Fq 'snapshot.destinationPlaylistId' "$REVIEW" ||
  fail "Review can erase persisted destination playlist ID"

grep -Fq 'OAuth token у цьому вікні не показується' "$MAIN" ||
  fail "account dialog token-privacy copy missing"
grep -Fq 'no OAuth access token is shown' "$PHONE" ||
  fail "phone plan does not protect token privacy"

echo "PASS:"
echo "- historical v1.4.47 / code 87 evidence"
echo "- interactive Home account/current-playlist cards"
echo "- Home track list removed"
echo "- dedicated Playlist Hub registered"
echo "- Review/Search/Create/Project/replacements feature surface retained"
echo "- CurrentPlaylistStore schema v2 + v1 compatibility"
echo "- destination YTM playlist ID persistence"
echo "- OAuth token remains hidden"

#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
fail(){ echo "FAIL: $1" >&2; exit 1; }
FILES=(
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolution.kt
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolver.kt
 app/src/main/java/com/saney/ytmimporter/youtube/YouTubeApi.kt
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCache.kt
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotRemoteOperations.kt
 app/src/main/java/com/saney/ytmimporter/UrlSnapshotActivity.kt
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCommitPolicy.kt
 app/src/main/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotTitleBackfill.kt
 app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotResolverTest.kt
 app/src/test/java/com/saney/ytmimporter/urlsnapshot/UrlSnapshotCommitPolicyTest.kt
 docs/v.1.4.51/TITLE_METADATA_CONTRACT.md
)
for f in "${FILES[@]}"; do test -f "$f" || fail "missing title metadata file: $f"; done
python - "${FILES[@]}" <<'AUDITPY'
from pathlib import Path
import sys
resolution,resolver,api,cache,remote,activity,commit,backfill,resolver_test,commit_test,contract=[Path(x).read_text(encoding='utf-8') for x in sys.argv[1:]]
checks=[
 (resolution,['val playlistTitle: String? = null']),
 (resolver,['api.getPlaylistSnapshotTitle(','result.requestCount + 1']),
 (api,['fun getPlaylistSnapshotTitle(','?part=snippet','onListRequest()','Завантаження назви плейлиста']),
 (cache,['"playlistTitle"','resolved.playlistTitle','private const val SCHEMA_VERSION =\n            1']),
 (remote,['fun loadMissingPlaylistTitle(','QuotaTracker.SIMPLE_LIST_COST','requestCountNow = 1']),
 (activity,['Плейлист:','Отримати назву плейлиста • 1 API','.loadMissingPlaylistTitle(']),
 (commit,['resolved.playlistTitle','snapshotName(']),
 (backfill,['CurrentPlaylistStore','HistoryStore','entry.copy(','playlistName =']),
 (resolver_test,['concretePlaylist_carriesRemotePlaylistTitle','Remote playlist title']),
 (commit_test,['remotePlaylistTitleBecomesLocalPlaylistName','Human playlist name']),
]
for text,needles in checks:
    for needle in needles:
        if needle not in text: raise SystemExit('FAIL: missing marker: '+needle)
start=api.index('fun getPlaylistSnapshotTitle('); end=api.index('fun getGoogleAccountInfo(',start); meta=api[start:end]
for x in ('playlistItems','createPlaylist(','addVideo(','updatePlaylist','deletePlaylist('):
    if x in meta: raise SystemExit('FAIL: title-only API boundary violation: '+x)
start=remote.index('fun loadMissingPlaylistTitle('); end=remote.index('fun clearTerminal()',start); owner=remote[start:end]
if 'listPlaylistSnapshotItems(' in owner: raise SystemExit('FAIL: metadata-only owner re-reads items')
normalized_cache = ' '.join(cache.split())
normalized_owner = ' '.join(owner.split())
for x in (
    'fun putPreservingCachedAt(',
    'cachedAt > 0L',
    'encode( resolved = resolved, cachedAt = cachedAt',
):
    if ' '.join(x.split()) not in normalized_cache:
        raise SystemExit('FAIL: preserved cachedAt cache-write contract missing: '+x)
for x in (
    'if (!current.fromCache) return false',
    'val snapshotCachedAt = current.cachedAt ?: return false',
    '.putPreservingCachedAt(',
    'cachedAt = snapshotCachedAt',
):
    if ' '.join(x.split()) not in normalized_owner:
        raise SystemExit('FAIL: metadata-only cachedAt preservation missing: '+x)
if '.put(updated)' in owner:
    raise SystemExit('FAIL: metadata-only title enrichment still uses fresh-timestamp put(updated)')
if 'System.currentTimeMillis()' in owner:
    raise SystemExit('FAIL: metadata-only title enrichment changed cachedAt')
preserve_start = cache.index('fun putPreservingCachedAt(')
preserve_end = cache.index('fun stats()', preserve_start)
preserve_method = cache[preserve_start:preserve_end]
if 'System.currentTimeMillis()' in preserve_method:
    raise SystemExit('FAIL: preserved cachedAt writer creates a fresh timestamp')
for x in ('YouTubeApi','HttpURLConnection','SearchCoordinator','PlaylistWriteCoordinator'):
    if x in backfill: raise SystemExit('FAIL: backfill must remain local-only: '+x)
normalized_contract = ' '.join(contract.split())
for x in ('one additional official','Отримати назву плейлиста • 1 API','does not call','no new History entry is created'):
    if ' '.join(x.split()) not in normalized_contract:
        raise SystemExit('FAIL: contract marker missing: '+x)
print('PASS: playlist title metadata static contract')
AUDITPY

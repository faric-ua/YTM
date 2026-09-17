#!/usr/bin/env python3
from __future__ import annotations

import sys
sys.dont_write_bytecode = True

from pathlib import Path
import tempfile
import shutil
import subprocess
import hashlib

SCRIPT_DIR = Path(__file__).resolve().parent
PACKAGE_ROOT = SCRIPT_DIR.parent
sys.path.insert(0, str(SCRIPT_DIR))

from v1427_patchlib import (
    OPS,
    PatchError,
    apply_one,
    run,
    validate_ops,
)

FIXTURE_FILES = {'app/build.gradle.kts': 'android {\n'
                         '    defaultConfig {\n'
                         '        versionCode = 60\n'
                         '        versionName = "1.4.26"\n'
                         '    }\n'
                         '}\n',
 'app/src/main/java/com/saney/ytmimporter/MainActivity.kt': 'package com.saney.ytmimporter\n'
                                                            '\n'
                                                            'class MainActivity {\n'
                                                            '    private fun onReviewResult(data: Intent) {\n'
                                                            '                if (\n'
                                                            '                    data.getBooleanExtra(\n'
                                                            '                        '
                                                            'ReviewActivity.EXTRA_REPEAT_SEARCH,\n'
                                                            '                        false\n'
                                                            '                    )\n'
                                                            '                ) {\n'
                                                            '                    searchAll(\n'
                                                            '                        openReviewAfter = true\n'
                                                            '                    )\n'
                                                            '                    return\n'
                                                            '                }\n'
                                                            '    }\n'
                                                            '\n'
                                                            '    private fun searchAll(\n'
                                                            '        openReviewAfter: Boolean = false,\n'
                                                            '        preserveExistingExact: Boolean = false\n'
                                                            '    ) {\n'
                                                            '        val p = playlist ?: return\n'
                                                            '        val plan =\n'
                                                            '            searchCoordinator.plan(\n'
                                                            '                playlist = p,\n'
                                                            '                preserveExistingExact =\n'
                                                            '                    preserveExistingExact\n'
                                                            '            )\n'
                                                            '    }\n'
                                                            '\n'
                                                            '    private fun bridge() {\n'
                                                            '        when (progress.preservedSelection) {\n'
                                                            '            '
                                                            'SearchCoordinator.PreservedSelection.MANUAL -> '
                                                            'Unit\n'
                                                            '            '
                                                            'SearchCoordinator.PreservedSelection.PROJECT_EXACT '
                                                            '-> Unit\n'
                                                            '            null -> Unit\n'
                                                            '        }\n'
                                                            '        searchCoordinator.run(\n'
                                                            '            accessToken = token,\n'
                                                            '            playlist = p,\n'
                                                            '            preserveExistingExact = '
                                                            'preserveExistingExact\n'
                                                            '        )\n'
                                                            '    }\n'
                                                            '}\n',
 'app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt': 'package com.saney.ytmimporter\n'
                                                              '\n'
                                                              'class ReviewActivity {\n'
                                                              '    private fun requestRepeatSearch() {\n'
                                                              '        UiChrome.alertBuilder(this)\n'
                                                              '            .setTitle("Повторити пошук?")\n'
                                                              '            .setMessage(\n'
                                                              '                "YTM Importer повернеться на '
                                                              'головний екран і знову " +\n'
                                                              '                    "пройде всі треки через '
                                                              'SearchCache / YouTube search. " +\n'
                                                              '                    "Кешовані результати не '
                                                              'витрачають search.list quota."\n'
                                                              '            )\n'
                                                              '    }\n'
                                                              '}\n',
 'app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt': 'package '
                                                                        'com.saney.ytmimporter.search\n'
                                                                        '\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.model.ImportedPlaylist\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.model.SearchCandidate\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.model.Track\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.model.TrackStatus\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.storage.QuotaSnapshot\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.storage.QuotaTracker\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.util.ErrorMessages\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.youtube.SearchCache\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.youtube.YouTubeApi\n'
                                                                        'import '
                                                                        'com.saney.ytmimporter.youtube.YouTubeApiException\n'
                                                                        '\n'
                                                                        'class SearchCoordinator(\n'
                                                                        '    private val api: YouTubeApi,\n'
                                                                        '    private val searchCache: '
                                                                        'SearchCache,\n'
                                                                        '    private val quotaTracker: '
                                                                        'QuotaTracker\n'
                                                                        ') {\n'
                                                                        '    data class SearchPlan(\n'
                                                                        '        val totalTracks: Int,\n'
                                                                        '        val tracksToSearch: Int,\n'
                                                                        '        val cachedCount: Int,\n'
                                                                        '        val apiNeeded: Int,\n'
                                                                        '        val quota: QuotaSnapshot\n'
                                                                        '    )\n'
                                                                        '\n'
                                                                        '    enum class PreservedSelection '
                                                                        '{\n'
                                                                        '        MANUAL,\n'
                                                                        '        PROJECT_EXACT\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    data class SearchProgress(\n'
                                                                        '        val processed: Int,\n'
                                                                        '        val total: Int,\n'
                                                                        '        val cacheHits: Int,\n'
                                                                        '        val apiSearches: Int,\n'
                                                                        '        val preservedSelection: '
                                                                        'PreservedSelection? = null\n'
                                                                        '    )\n'
                                                                        '\n'
                                                                        '    data class SearchResult(\n'
                                                                        '        val cacheHits: Int,\n'
                                                                        '        val apiSearches: Int,\n'
                                                                        '        val quotaBlocked: Boolean\n'
                                                                        '    )\n'
                                                                        '\n'
                                                                        '    fun plan(\n'
                                                                        '        playlist: '
                                                                        'ImportedPlaylist,\n'
                                                                        '        preserveExistingExact: '
                                                                        'Boolean\n'
                                                                        '    ): SearchPlan {\n'
                                                                        '        val tracksToSearch =\n'
                                                                        '            playlist.tracks.filter '
                                                                        '{ track ->\n'
                                                                        '                shouldSearch(\n'
                                                                        '                    track = track,\n'
                                                                        '                    '
                                                                        'preserveExistingExact = '
                                                                        'preserveExistingExact\n'
                                                                        '                )\n'
                                                                        '            }\n'
                                                                        '\n'
                                                                        '        val cachedCount =\n'
                                                                        '            tracksToSearch.count { '
                                                                        'track ->\n'
                                                                        '                '
                                                                        'searchCache.get(track) != null\n'
                                                                        '            }\n'
                                                                        '\n'
                                                                        '        return SearchPlan(\n'
                                                                        '            totalTracks = '
                                                                        'playlist.tracks.size,\n'
                                                                        '            tracksToSearch = '
                                                                        'tracksToSearch.size,\n'
                                                                        '            cachedCount = '
                                                                        'cachedCount,\n'
                                                                        '            apiNeeded = '
                                                                        'tracksToSearch.size - cachedCount,\n'
                                                                        '            quota = '
                                                                        'quotaTracker.snapshot()\n'
                                                                        '        )\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    fun run(\n'
                                                                        '        accessToken: String,\n'
                                                                        '        playlist: '
                                                                        'ImportedPlaylist,\n'
                                                                        '        preserveExistingExact: '
                                                                        'Boolean,\n'
                                                                        '        onTrackStateChanged: '
                                                                        '(index: Int) -> Unit = {},\n'
                                                                        '        onProgress: '
                                                                        '(SearchProgress) -> Unit = {},\n'
                                                                        '        onQuotaBlocked: () -> Unit '
                                                                        '= {}\n'
                                                                        '    ): SearchResult {\n'
                                                                        '        var cacheHits = 0\n'
                                                                        '        var apiSearches = 0\n'
                                                                        '        var quotaBlocked = false\n'
                                                                        '        var quotaCallbackSent = '
                                                                        'false\n'
                                                                        '\n'
                                                                        '        for ((index, track) in '
                                                                        'playlist.tracks.withIndex()) {\n'
                                                                        '            if '
                                                                        '(Thread.currentThread().isInterrupted) '
                                                                        'break\n'
                                                                        '\n'
                                                                        '            val preservedSelection '
                                                                        '=\n'
                                                                        '                '
                                                                        'preservedSelection(\n'
                                                                        '                    track = track,\n'
                                                                        '                    '
                                                                        'preserveExistingExact = '
                                                                        'preserveExistingExact\n'
                                                                        '                )\n'
                                                                        '\n'
                                                                        '            if (preservedSelection '
                                                                        '!= null) {\n'
                                                                        '                onProgress(\n'
                                                                        '                    '
                                                                        'SearchProgress(\n'
                                                                        '                        processed = '
                                                                        'index + 1,\n'
                                                                        '                        total = '
                                                                        'playlist.tracks.size,\n'
                                                                        '                        cacheHits = '
                                                                        'cacheHits,\n'
                                                                        '                        apiSearches '
                                                                        '= apiSearches,\n'
                                                                        '                        '
                                                                        'preservedSelection = '
                                                                        'preservedSelection\n'
                                                                        '                    )\n'
                                                                        '                )\n'
                                                                        '                continue\n'
                                                                        '            }\n'
                                                                        '\n'
                                                                        '            track.status = '
                                                                        'TrackStatus.SEARCHING\n'
                                                                        '            track.error = null\n'
                                                                        '            '
                                                                        'onTrackStateChanged(index)\n'
                                                                        '\n'
                                                                        '            try {\n'
                                                                        '                val '
                                                                        'cachedCandidates = '
                                                                        'searchCache.get(track)\n'
                                                                        '                val candidates =\n'
                                                                        '                    if '
                                                                        '(cachedCandidates != null) {\n'
                                                                        '                        cacheHits '
                                                                        '+= 1\n'
                                                                        '                        '
                                                                        'quotaTracker.recordCacheHit()\n'
                                                                        '                        '
                                                                        'cachedCandidates\n'
                                                                        '                    } else if '
                                                                        '(quotaBlocked) {\n'
                                                                        '                        '
                                                                        'track.status = TrackStatus.FAILED\n'
                                                                        '                        track.error '
                                                                        '= "quota"\n'
                                                                        '                        '
                                                                        'emptyList()\n'
                                                                        '                    } else {\n'
                                                                        '                        apiSearches '
                                                                        '+= 1\n'
                                                                        '                        '
                                                                        'quotaTracker.recordSearchCall()\n'
                                                                        '                        val '
                                                                        'freshCandidates = '
                                                                        'api.search(accessToken, track)\n'
                                                                        '                        '
                                                                        'searchCache.put(track, '
                                                                        'freshCandidates)\n'
                                                                        '                        '
                                                                        'freshCandidates\n'
                                                                        '                    }\n'
                                                                        '\n'
                                                                        '                if (track.status != '
                                                                        'TrackStatus.FAILED) {\n'
                                                                        '                    '
                                                                        'applySearchCandidates(track, '
                                                                        'candidates)\n'
                                                                        '                }\n'
                                                                        '            } catch (error: '
                                                                        'Exception) {\n'
                                                                        '                track.status = '
                                                                        'TrackStatus.FAILED\n'
                                                                        '                track.error = '
                                                                        'ErrorMessages.userMessage(error, '
                                                                        '"search failed")\n'
                                                                        '                if '
                                                                        '(isQuotaError(error)) {\n'
                                                                        '                    quotaBlocked = '
                                                                        'true\n'
                                                                        '                    '
                                                                        'quotaTracker.recordQuotaError(error.message '
                                                                        '?: "quota")\n'
                                                                        '                    if '
                                                                        '(!quotaCallbackSent) {\n'
                                                                        '                        '
                                                                        'quotaCallbackSent = true\n'
                                                                        '                        '
                                                                        'onQuotaBlocked()\n'
                                                                        '                    }\n'
                                                                        '                }\n'
                                                                        '            }\n'
                                                                        '\n'
                                                                        '            onProgress(\n'
                                                                        '                SearchProgress(\n'
                                                                        '                    processed = '
                                                                        'index + 1,\n'
                                                                        '                    total = '
                                                                        'playlist.tracks.size,\n'
                                                                        '                    cacheHits = '
                                                                        'cacheHits,\n'
                                                                        '                    apiSearches = '
                                                                        'apiSearches\n'
                                                                        '                )\n'
                                                                        '            )\n'
                                                                        '        }\n'
                                                                        '\n'
                                                                        '        return '
                                                                        'SearchResult(cacheHits, '
                                                                        'apiSearches, quotaBlocked)\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    private fun shouldSearch(\n'
                                                                        '        track: Track,\n'
                                                                        '        preserveExistingExact: '
                                                                        'Boolean\n'
                                                                        '    ): Boolean {\n'
                                                                        '        val manualExact =\n'
                                                                        '            track.manuallySelected '
                                                                        '&&\n'
                                                                        '                '
                                                                        '!track.selectedVideoId.isNullOrBlank()\n'
                                                                        '\n'
                                                                        '        if (manualExact) {\n'
                                                                        '            return false\n'
                                                                        '        }\n'
                                                                        '\n'
                                                                        '        return if '
                                                                        '(preserveExistingExact) {\n'
                                                                        '            '
                                                                        'track.selectedVideoId.isNullOrBlank() '
                                                                        '||\n'
                                                                        '                track.status != '
                                                                        'TrackStatus.MATCHED ||\n'
                                                                        '                '
                                                                        'track.candidates.isNotEmpty()\n'
                                                                        '        } else {\n'
                                                                        '            true\n'
                                                                        '        }\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    private fun '
                                                                        'preservedSelection(\n'
                                                                        '        track: Track,\n'
                                                                        '        preserveExistingExact: '
                                                                        'Boolean\n'
                                                                        '    ): PreservedSelection? {\n'
                                                                        '        val keepManualSelection =\n'
                                                                        '            track.manuallySelected '
                                                                        '&&\n'
                                                                        '                '
                                                                        '!track.selectedVideoId.isNullOrBlank()\n'
                                                                        '\n'
                                                                        '        if (keepManualSelection) {\n'
                                                                        '            return '
                                                                        'PreservedSelection.MANUAL\n'
                                                                        '        }\n'
                                                                        '\n'
                                                                        '        val keepExactSelection =\n'
                                                                        '            preserveExistingExact '
                                                                        '&&\n'
                                                                        '                '
                                                                        '!track.selectedVideoId.isNullOrBlank() '
                                                                        '&&\n'
                                                                        '                track.status == '
                                                                        'TrackStatus.MATCHED &&\n'
                                                                        '                '
                                                                        'track.candidates.isEmpty()\n'
                                                                        '\n'
                                                                        '        return if '
                                                                        '(keepExactSelection) {\n'
                                                                        '            '
                                                                        'PreservedSelection.PROJECT_EXACT\n'
                                                                        '        } else {\n'
                                                                        '            null\n'
                                                                        '        }\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    private fun '
                                                                        'applySearchCandidates(\n'
                                                                        '        track: Track,\n'
                                                                        '        candidates: '
                                                                        'List<SearchCandidate>\n'
                                                                        '    ) {\n'
                                                                        '        track.candidates = '
                                                                        'candidates\n'
                                                                        '\n'
                                                                        '        if (\n'
                                                                        '            track.manuallySelected '
                                                                        '&&\n'
                                                                        '            '
                                                                        '!track.selectedVideoId.isNullOrBlank()\n'
                                                                        '        ) {\n'
                                                                        '            track.status = '
                                                                        'TrackStatus.MATCHED\n'
                                                                        '            track.error = null\n'
                                                                        '            return\n'
                                                                        '        }\n'
                                                                        '\n'
                                                                        '        val best = '
                                                                        'candidates.firstOrNull()\n'
                                                                        '\n'
                                                                        '        if (best == null) {\n'
                                                                        '            track.status = '
                                                                        'TrackStatus.MISSING\n'
                                                                        '            track.selectedVideoId = '
                                                                        'null\n'
                                                                        '            track.selectedTitle = '
                                                                        'null\n'
                                                                        '            track.selectedChannel = '
                                                                        'null\n'
                                                                        '            return\n'
                                                                        '        }\n'
                                                                        '\n'
                                                                        '        track.selectedVideoId = '
                                                                        'best.videoId\n'
                                                                        '        track.selectedTitle = '
                                                                        'best.title\n'
                                                                        '        track.selectedChannel = '
                                                                        'best.channelTitle\n'
                                                                        '        track.manuallySelected = '
                                                                        'false\n'
                                                                        '        track.error = null\n'
                                                                        '\n'
                                                                        '        track.status =\n'
                                                                        '            if (best.score >= '
                                                                        'AUTO_MATCH_THRESHOLD) {\n'
                                                                        '                '
                                                                        'TrackStatus.MATCHED\n'
                                                                        '            } else {\n'
                                                                        '                TrackStatus.REVIEW\n'
                                                                        '            }\n'
                                                                        '    }\n'
                                                                        '\n'
                                                                        '    private fun isQuotaError(\n'
                                                                        '        error: Throwable\n'
                                                                        '    ): Boolean =\n'
                                                                        '        (error as? '
                                                                        'YouTubeApiException)\n'
                                                                        '            ?.isQuotaError == true '
                                                                        '||\n'
                                                                        '            '
                                                                        'error.message.orEmpty().contains("quota", '
                                                                        'ignoreCase = true)\n'
                                                                        '\n'
                                                                        '    companion object {\n'
                                                                        '        private const val '
                                                                        'AUTO_MATCH_THRESHOLD = 0.72\n'
                                                                        '    }\n'
                                                                        '}\n',
 'RELEASE_TEST_STATUS.md': '# status\n'
                           '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT PATH; BUG-005 '
                           'FOUND** | Two-playlist selective export passed: 2 projects + manifest, schema '
                           'v2/SELECTED, exact-videoId round trip 3/3. Manual Search then proposed redundant '
                           'search.list for exact tracks (BUG-005). |\n',
 'PROJECT_STATUS.txt': 'YTM Importer\n'
                       'Version: 1.4.26\n'
                       'Version code: 60\n'
                       '\n'
                       'v1.4.26 PARTIALLY PHONE-TESTED — SELECTIVE EXPORT PASS / BUG-005 FOUND\n'
                       '\n'
                       'Known:\n'
                       'BUG-005/Q-005 OPEN — redundant manual search for exact videoId tracks\n'
                       '\n'
                       'QA:\n'
                       'snapshot\n',
 'BACKLOG.md': '# YTM Importer — Roadmap\n'
               '\n'
               '## Current\n'
               'v1.4.26 — Selective Account Export\n'
               '\n'
               '## Known\n'
               '- BUG-005/Q-005 redundant manual search for exact videoId tracks — OPEN, v1.4.27\n'
               '\n'
               '## v1.4.27 — Exact-ID Search Guard\n'
               '- [ ] exclude tracks with exact/canonical videoId from ordinary search planning\n'
               '- [ ] when all tracks are exact, show 0 required searches / no quota work\n'
               '- [ ] preserve explicit manual candidate selections\n'
               '- [ ] keep an intentional future re-search path separate from normal search\n'
               '- [ ] add BUG-005 static regression audit\n'
               '- [ ] add release docs + phone-test plan\n'
               '- [ ] GitHub build\n'
               '- [ ] phone retest: reopen exact `top 3` project\n'
               '- [ ] phone retest: Search plan = 0 new search.list\n'
               '- [ ] confirm Review remains 3/3 ready\n'
               '\n'
               '## Next\n'
               'next\n',
 'CHANGELOG.md': '# Журнал змін (Changelog)\n\n## v1.4.26\n- existing\n',
 'qa/BUG_REGISTER.md': '# bugs\n'
                       '| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 | P2 | Manual Search plans new search.list '
                       'requests for tracks that already have exact videoId. | v1.4.26 round-trip / manual '
                       'Search |\n',
 'OPEN_QUESTIONS.md': '# questions\n'
                      '\n'
                      '## Q-005 — Manual Search should respect exact videoId\n'
                      '\n'
                      'Status: **OPEN — BUG-005, planned for v1.4.27.**\n'
                      '\n'
                      'This separates "find missing matches" from "force a new search".\n',
 'scripts/search-coordinator-audit.sh': '#!/usr/bin/env bash\n'
                                        'set -euo pipefail\n'
                                        'fail() { echo "FAIL: $1" >&2; exit 1; }\n'
                                        'MAIN="app/src/main/java/com/saney/ytmimporter/MainActivity.kt"\n'
                                        'COORD="app/src/main/java/com/saney/ytmimporter/search/SearchCoordinator.kt"\n'
                                        'grep -q \'class SearchCoordinator\' "$COORD" || fail '
                                        '"SearchCoordinator class missing"\n'
                                        'grep -q \'fun plan(\' "$COORD" || fail "SearchCoordinator.plan '
                                        'missing"\n'
                                        'grep -q \'fun run(\' "$COORD" || fail "SearchCoordinator.run '
                                        'missing"\n'
                                        'grep -q \'api.search(\' "$COORD" || fail "api search missing"\n'
                                        'grep -q \'searchCoordinator.plan(\' "$MAIN" || fail "plan '
                                        'delegation missing"\n'
                                        'grep -q \'searchCoordinator.run(\' "$MAIN" || fail "run delegation '
                                        'missing"\n'
                                        'grep -q \'SearchCoordinator.PreservedSelection.MANUAL\' "$MAIN" || '
                                        'fail "manual bridge missing"\n'
                                        "grep -q 'SearchCoordinator.PreservedSelection.PROJECT_EXACT' "
                                        '"$MAIN" || fail "project bridge missing"\n'
                                        'echo "PASS:"\n'
                                        'echo "- coordinator baseline"\n',
 'scripts/release-preflight.sh': '#!/usr/bin/env bash\n'
                                 'set -euo pipefail\n'
                                 'fail() { echo "ERROR: $*" >&2; exit 1; }\n'
                                 'check_file() { [ -f "$1" ] || fail "Missing required file: $1"; }\n'
                                 'check_file "docs/v.1.4.26/RELEASE.md"\n'
                                 'check_file "docs/v.1.4.26/REGRESSION_CHECKLIST.md"\n'
                                 'python -B scripts/v1426-apply-selftest.py\n'
                                 'bash scripts/v1426-qa-close-audit.sh\n'
                                 'bash scripts/v1426-selective-export-audit.sh\n'
                                 'check_file "scripts/v1426-selective-export-audit.sh"\n'
                                 "grep -q 'versionCode = 60' app/build.gradle.kts \\\n"
                                 '  || fail "Expected versionCode = 60"\n'
                                 'grep -q \'versionName = "1.4.26"\' app/build.gradle.kts \\\n'
                                 '  || fail \'Expected versionName = "1.4.26"\'\n',
 'scripts/qa-plan-audit.sh': '#!/usr/bin/env bash\n'
                             'STATUS="RELEASE_TEST_STATUS.md"\n'
                             'BUG="qa/BUG_REGISTER.md"\n'
                             "grep -Fq '| v1.4.26 | **PARTIALLY PHONE-TESTED — PASS FOR SELECTIVE EXPORT "
                             'PATH; BUG-005 FOUND** |\' "$STATUS" \\\n'
                             '  || fail "v1.4.26 selective-export phone status missing"\n'
                             'grep -Fq \'| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 |\' "$BUG" \\\n'
                             '  || fail "BUG-005 phone finding missing"\n'
                             'grep -Fq \'| BUG-003 / Q-003 | CLOSED — PHONE RETEST PASS v1.4.20 |\' "$BUG" '
                             '\\\n'
                             '  || fail "BUG-003 closed phone-retest status missing"\n',
 'scripts/v1426-qa-close-audit.sh': '#!/usr/bin/env bash\n'
                                    'set -euo pipefail\n'
                                    'fail() { echo "FAIL: $1" >&2; exit 1; }\n'
                                    'STATUS="RELEASE_TEST_STATUS.md"\n'
                                    'BUG="qa/BUG_REGISTER.md"\n'
                                    'BACKLOG="BACKLOG.md"\n'
                                    'RELEASE="docs/v.1.4.26/RELEASE.md"\n'
                                    'CHECKLIST="docs/v.1.4.26/REGRESSION_CHECKLIST.md"\n'
                                    'grep -Fq \'| BUG-005 / Q-005 | OPEN — FOUND v1.4.26 |\' "$BUG" \\\n'
                                    '  || fail "BUG-005 missing from root bug register"\n',
 'docs/tutorial/06_ACCOUNT_LIBRARY_EXPORT.md': '# tutorial\n'
                                               '\n'
                                               'successful persistence не гарантує correct downstream '
                                               'behavior.\n',
 'docs/tutorial/ROADMAP.md': '# roadmap\n- `11_SEARCH_AND_EXACT_VIDEO_ID.md` — пошук vs exact identifiers;\n'}

PACKAGE_OWNED = [
    "docs/v.1.4.27/RELEASE.md",
    "docs/v.1.4.27/REGRESSION_CHECKLIST.md",
    "docs/v.1.4.27/qa/PHONE_TEST.md",
    "docs/v.1.4.27/qa/BUG_REGISTER.md",
    "docs/v.1.4.27/qa/PACKAGE_NOTES.md",
    "docs/v.1.4.27/qa/PACKAGE_SELFTEST.md",
    "docs/v.1.4.27/diagrams/EXACT_ID_SEARCH_GUARD.md",
    "scripts/apply-v1.4.27.py",
    "scripts/v1427_patchlib.py",
    "scripts/v1427-exact-id-search-audit.py",
    "scripts/v1427-apply-selftest.py",
]

def write(root: Path, rel: str, text: str) -> None:
    path = root / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")

def build_fixture(root: Path) -> None:
    for rel, text in FIXTURE_FILES.items():
        write(root, rel, text)

def overlay_package(root: Path) -> None:
    for rel in PACKAGE_OWNED:
        src = PACKAGE_ROOT / rel
        if not src.is_file():
            raise SystemExit(
                f"SELFTEST FAIL: package-owned file missing: {rel}"
            )
        dst = root / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)

def tree_hash(root: Path) -> str:
    h = hashlib.sha256()
    for path in sorted(p for p in root.rglob("*") if p.is_file() and ".git" not in p.parts):
        h.update(str(path.relative_to(root)).encode("utf-8"))
        h.update(b"\0")
        h.update(path.read_bytes())
        h.update(b"\0")
    return h.hexdigest()

def init_git(root: Path) -> None:
    subprocess.run(["git", "init", "-q"], cwd=root, check=True)
    subprocess.run(["git", "config", "user.email", "selftest@example.invalid"], cwd=root, check=True)
    subprocess.run(["git", "config", "user.name", "YTM Selftest"], cwd=root, check=True)
    subprocess.run(["git", "add", "."], cwd=root, check=True)
    subprocess.run(["git", "commit", "-qm", "fixture"], cwd=root, check=True)

def expect_patch_error(fn, label: str) -> None:
    try:
        fn()
    except PatchError:
        print(f"PASS: {label}")
        return
    raise SystemExit(
        f"SELFTEST FAIL: expected PatchError: {label}"
    )

def scan_package_text() -> None:
    for rel in PACKAGE_OWNED:
        path = PACKAGE_ROOT / rel
        if not path.is_file():
            raise SystemExit(
                f"SELFTEST FAIL: missing package file: {rel}"
            )
        if path.suffix.lower() not in {".md", ".py", ".sh", ".txt", ".csv"}:
            continue
        data = path.read_bytes()
        if b"\r" in data:
            raise SystemExit(
                f"SELFTEST FAIL: CR found in {rel}"
            )
        text = data.decode("utf-8")
        if text.endswith("\n\n"):
            raise SystemExit(
                f"SELFTEST FAIL: blank line at EOF in {rel}"
            )
        for line_no, line in enumerate(text.split("\n"), 1):
            if line.endswith(" ") or line.endswith("\t"):
                raise SystemExit(
                    f"SELFTEST FAIL: trailing whitespace {rel}:{line_no}"
                )
    print(
        "PASS: package-owned text LF-only / no trailing whitespace / clean EOF"
    )

def run_static_audits(root: Path) -> None:
    audit = subprocess.run(
        [sys.executable, "-B", "scripts/v1427-exact-id-search-audit.py"],
        cwd=root,
        text=True,
        capture_output=True
    )
    if audit.returncode != 0:
        print(audit.stdout)
        print(audit.stderr)
        raise SystemExit(
            "SELFTEST FAIL: v1.4.27 exact-ID audit failed"
        )
    print("PASS: v1.4.27 exact-ID audit on applied fixture")

    search_audit = subprocess.run(
        ["bash", "scripts/search-coordinator-audit.sh"],
        cwd=root,
        text=True,
        capture_output=True
    )
    if search_audit.returncode != 0:
        print(search_audit.stdout)
        print(search_audit.stderr)
        raise SystemExit(
            "SELFTEST FAIL: search-coordinator audit failed"
        )
    print("PASS: strengthened search-coordinator audit")

def diff_check(root: Path) -> None:
    proc = subprocess.run(
        ["git", "diff", "--check"],
        cwd=root,
        text=True,
        capture_output=True
    )
    if proc.returncode != 0:
        print(proc.stdout)
        print(proc.stderr)
        raise SystemExit(
            "SELFTEST FAIL: git diff --check"
        )
    print("PASS: git diff --check")

def main() -> int:
    validate_ops()
    print("PASS: production v1.4.27 operations validated")

    scan_package_text()

    for rel in PACKAGE_OWNED:
        path = PACKAGE_ROOT / rel
        if path.suffix == ".py":
            compile(
                path.read_text(encoding="utf-8"),
                str(path),
                "exec"
            )
    print("PASS: Python syntax")

    bad_ops = [
        (
            "replace",
            "x.txt",
            r"line1\nline2",
            "new",
            "literal-newline regression",
        )
    ]
    expect_patch_error(
        lambda: validate_ops(bad_ops),
        "literal \\n anchor rejected"
    )

    with tempfile.TemporaryDirectory(
        prefix="ytm-v1427-selftest-"
    ) as td:
        base = Path(td)

        # Clean current-state check and first apply.
        repo = base / "clean"
        repo.mkdir()
        build_fixture(repo)
        init_git(repo)
        overlay_package(repo)

        run(repo, dry_run=True)
        print("PASS: clean current-state --check")

        run(repo, dry_run=False)
        print("PASS: clean first apply")

        diff_check(repo)
        run_static_audits(repo)

        # Real MainActivity also has startSearch(... = false).
        # That local execution helper is allowed; only ordinary searchAll()
        # must default to preserving exact selections.
        main_path = (
            repo /
            "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"
        )
        main_text = main_path.read_text(encoding="utf-8")
        realistic_helper = """
    private fun startSearch(
        p: ImportedPlaylist,
        openReviewAfter: Boolean = false,
        preserveExistingExact: Boolean = false
    ) {
        searchCoordinator.run(
            accessToken = token,
            playlist = p,
            preserveExistingExact = preserveExistingExact
        )
    }
"""
        main_path.write_text(
            main_text.rstrip()[:-1] +
            realistic_helper +
            "\n}\n",
            encoding="utf-8",
            newline="\n"
        )

        audit_realistic = subprocess.run(
            [
                sys.executable,
                "-B",
                "scripts/v1427-exact-id-search-audit.py",
            ],
            cwd=repo,
            text=True,
            capture_output=True
        )
        if audit_realistic.returncode != 0:
            print(audit_realistic.stdout)
            print(audit_realistic.stderr)
            raise SystemExit(
                "SELFTEST FAIL: realistic startSearch false default "
                "triggered exact-ID audit"
            )
        print(
            "PASS: audit ignores allowed startSearch false default"
        )

        # Restore the fixture before idempotence hashing.
        main_path.write_text(
            main_text,
            encoding="utf-8",
            newline="\n"
        )

        first_hash = tree_hash(repo)

        run(repo, dry_run=True)
        print("PASS: post-apply --check")

        run(repo, dry_run=False)
        second_hash = tree_hash(repo)

        if first_hash != second_hash:
            raise SystemExit(
                "SELFTEST FAIL: repeated apply changed files"
            )
        print("PASS: repeat apply idempotent")

        # Partial state: apply one production op, then full apply.
        partial = base / "partial"
        partial.mkdir()
        build_fixture(partial)
        overlay_package(partial)

        apply_one(
            partial,
            OPS[0],
            dry_run=False
        )
        run(partial, dry_run=False)

        if 'versionName = "1.4.27"' not in (
            partial / "app/build.gradle.kts"
        ).read_text(encoding="utf-8"):
            raise SystemExit(
                "SELFTEST FAIL: partial-state completion failed"
            )
        print("PASS: partially applied state completes safely")

        # Generic duplicate anchor must fail closed.
        dup = base / "duplicate"
        dup.mkdir()
        write(dup, "x.txt", "ANCHOR\nANCHOR\n")
        expect_patch_error(
            lambda: apply_one(
                dup,
                (
                    "replace",
                    "x.txt",
                    "ANCHOR\n",
                    "NEW\n",
                    "duplicate-anchor test",
                ),
                False
            ),
            "duplicate anchor fails closed"
        )

        # Generic missing anchor must fail closed.
        missing = base / "missing"
        missing.mkdir()
        write(missing, "x.txt", "other\n")
        expect_patch_error(
            lambda: apply_one(
                missing,
                (
                    "replace",
                    "x.txt",
                    "ANCHOR\n",
                    "NEW\n",
                    "missing-anchor test",
                ),
                False
            ),
            "missing anchor fails closed"
        )

    pycache = list(PACKAGE_ROOT.rglob("__pycache__"))
    pyc = list(PACKAGE_ROOT.rglob("*.pyc"))
    if pycache or pyc:
        raise SystemExit(
            "SELFTEST FAIL: bytecode/cache artifacts present"
        )
    print("PASS: no __pycache__ / .pyc")

    print("PASS: v1.4.27 PACKAGE SELFTEST COMPLETE")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())

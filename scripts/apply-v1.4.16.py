#!/usr/bin/env python3
from pathlib import Path
import hashlib
import re
import shutil
import subprocess
import sys

ROOT = Path.cwd()
MAIN = ROOT / "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    sys.exit(1)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        fail(f"MainActivity patch point not found: {label}")
    return text.replace(old, new, 1)


def regex_once(text: str, pattern: str, replacement: str, label: str) -> str:
    updated, count = re.subn(pattern, replacement, text, count=1, flags=re.S)
    if count != 1:
        fail(f"MainActivity regex patch failed ({count} matches): {label}")
    return updated


def patch_main_activity() -> None:
    if not MAIN.exists():
        fail(f"Missing {MAIN}")

    text = MAIN.read_text(encoding="utf-8")

    if "DestinationCoordinator" in text and "private lateinit var destinationCoordinator" in text:
        print("MainActivity: DestinationCoordinator bridge already applied")
        return

    if "private data class DuplicateAnalysis" not in text:
        fail("Expected v1.4.15 destination duplicate classes are missing")

    text = replace_once(
        text,
        "import com.saney.ytmimporter.search.SearchCoordinator\n",
        "import com.saney.ytmimporter.destination.DestinationCoordinator\n"
        "import com.saney.ytmimporter.search.SearchCoordinator\n",
        "DestinationCoordinator import",
    )
    text = text.replace(
        "import com.saney.ytmimporter.youtube.YouTubeApiException\n",
        "",
        1,
    )

    text = regex_once(
        text,
        r"class MainActivity : Activity\(\) \{\n"
        r"    private data class DuplicateAnalysis\(.*?\n"
        r"    private val pendingQueueRequestCode",
        "class MainActivity : Activity() {\n"
        "    private val pendingQueueRequestCode",
        "remove destination domain data classes",
    )

    text = replace_once(
        text,
        "    private lateinit var searchCoordinator: SearchCoordinator\n"
        "    private lateinit var playlistWriteCoordinator: PlaylistWriteCoordinator\n",
        "    private lateinit var searchCoordinator: SearchCoordinator\n"
        "    private lateinit var destinationCoordinator: DestinationCoordinator\n"
        "    private lateinit var playlistWriteCoordinator: PlaylistWriteCoordinator\n",
        "destination coordinator field",
    )

    text = regex_once(
        text,
        r"    private var destinationPlaylists: List<YouTubePlaylistInfo> = emptyList\(\)\n"
        r"    private var pendingDestinationTarget: YouTubePlaylistInfo\? = null\n"
        r"    private var pendingDestinationAnalysis: DuplicateAnalysis\? = null\n"
        r"    private var pendingDestinationScanRequestCount: Int = 0\n",
        "",
        "remove destination flow state from MainActivity",
    )

    text = replace_once(
        text,
        "        quotaTracker =\n"
        "            QuotaTracker(this)\n"
        "        searchCoordinator =\n",
        "        quotaTracker =\n"
        "            QuotaTracker(this)\n"
        "        destinationCoordinator =\n"
        "            DestinationCoordinator(\n"
        "                api = api,\n"
        "                quotaTracker = quotaTracker\n"
        "            )\n"
        "        searchCoordinator =\n",
        "destination coordinator initialization",
    )

    text = text.replace(
        "currentTracksForDestination(p)",
        "destinationCoordinator.currentTracksForDestination(p)",
    )

    text = regex_once(
        text,
        r"\n    private fun currentTracksForDestination\(\n.*?"
        r"\n    private fun openDestinationStart\(",
        "\n    private fun openDestinationStart(",
        "remove destination track selector from MainActivity",
    )

    text = regex_once(
        text,
        r"        destinationPlaylists = emptyList\(\)\n"
        r"        pendingDestinationTarget = null\n"
        r"        pendingDestinationAnalysis = null\n"
        r"        pendingDestinationScanRequestCount = 0",
        "        destinationCoordinator.reset()",
        "reset destination flow through coordinator",
    )

    old_back = '''            DestinationActivity.ACTION_BACK_TO_EXISTING_LIST -> {
                if (destinationPlaylists.isEmpty()) {
                    loadExistingPlaylistsForDestination(
                        p = p,
                        selected = selected
                    )
                } else {
                    openDestinationExistingList(
                        p = p,
                        selected = selected,
                        playlists = destinationPlaylists
                    )
                }
            }
'''
    new_back = '''            DestinationActivity.ACTION_BACK_TO_EXISTING_LIST -> {
                val cachedPlaylists =
                    destinationCoordinator.cachedPlaylists()

                if (cachedPlaylists.isEmpty()) {
                    loadExistingPlaylistsForDestination(
                        p = p,
                        selected = selected
                    )
                } else {
                    openDestinationExistingList(
                        p = p,
                        selected = selected,
                        playlists = cachedPlaylists
                    )
                }
            }
'''
    text = replace_once(text, old_back, new_back, "cached destination list")

    text = regex_once(
        text,
        r"                val target =\n"
        r"                    destinationPlaylists\n.*?"
        r"\n                pendingDestinationTarget = target\n",
        '''                val target =
                    destinationCoordinator.selectExistingTarget(
                        id = id,
                        title =
                            data.getStringExtra(
                                DestinationActivity.EXTRA_TARGET_TITLE
                            ),
                        privacyStatus =
                            data.getStringExtra(
                                DestinationActivity.EXTRA_TARGET_PRIVACY
                            ),
                        itemCount =
                            data.getLongExtra(
                                DestinationActivity.EXTRA_TARGET_COUNT,
                                0L
                            )
                    )
                        ?: return toast(
                            "Не вдалося визначити вибраний плейлист"
                        )
''',
        "selected destination target resolution",
    )

    old_load = '''                quotaTracker.recordGeneralUnits(
                    QuotaTracker.SIMPLE_LIST_COST
                )

                val result =
                    runCatching {
                        api.listMyPlaylists(token)
                    }
'''
    new_load = '''                val result =
                    runCatching {
                        destinationCoordinator.loadExistingPlaylists(
                            accessToken = token
                        )
                    }
'''
    text = replace_once(text, old_load, new_load, "existing playlist loading")
    text = text.replace(
        "                        destinationPlaylists = playlists\n\n",
        "",
        1,
    )

    new_scan_method = '''    private fun checkDuplicatesForDestination(
        p: ImportedPlaylist,
        selected: List<Track>,
        target: YouTubePlaylistInfo
    ) {
        authorize {
            val token =
                accessToken
                    ?: return@authorize

            progress.visibility = View.VISIBLE
            progress.isIndeterminate = true
            status(
                "Перевіряю дублікати у «${target.title}»…"
            )

            executor.execute {
                val result =
                    runCatching {
                        destinationCoordinator.scanDuplicates(
                            accessToken = token,
                            selected = selected,
                            target = target
                        )
                    }

                runOnUiThread {
                    progress.isIndeterminate = false
                    progress.visibility = View.GONE
                    updateQuotaPanel()

                    result.onSuccess { scan ->
                        openDestinationExistingConfirm(
                            p = p,
                            selected = selected,
                            target = target,
                            analysis = scan.analysis,
                            scanRequestCount = scan.requestCount
                        )
                    }.onFailure { error ->
                        openDestinationScanFailed(
                            p = p,
                            selected = selected,
                            target = target,
                            error = error
                        )
                    }
                }
            }
        }
    }

'''
    text = regex_once(
        text,
        r"    private fun checkDuplicatesForDestination\(.*?"
        r"\n    private fun openDestinationExistingConfirm\(",
        new_scan_method + "    private fun openDestinationExistingConfirm(",
        "duplicate scan orchestration",
    )

    text = text.replace(
        "        analysis: DuplicateAnalysis,\n",
        "        analysis: DestinationCoordinator.DuplicateAnalysis,\n",
        1,
    )

    new_finish = '''    private fun finishExistingDestination(
        p: ImportedPlaylist,
        selected: List<Track>,
        duplicateMode: String
    ) {
        val mode =
            when (duplicateMode) {
                DestinationActivity.DUPLICATE_MODE_NO_SCAN ->
                    DestinationCoordinator.DuplicateMode.NO_SCAN

                DestinationActivity.DUPLICATE_MODE_ALL ->
                    DestinationCoordinator.DuplicateMode.ADD_ALL

                else ->
                    DestinationCoordinator.DuplicateMode.SKIP
            }

        val plan =
            runCatching {
                destinationCoordinator.buildExistingWritePlan(
                    selected = selected,
                    mode = mode
                )
            }.getOrElse { error ->
                return toast(
                    error.message
                        ?: "Не вдалося підготувати запис у вибраний плейлист"
                )
            }

        actuallyAppendToExisting(
            p = p,
            selected = plan.tracksToWrite,
            target = plan.target,
            duplicateTracksToSkip = plan.tracksToSkip
        )
    }

'''
    text = regex_once(
        text,
        r"    private fun finishExistingDestination\(.*?"
        r"\n    private fun actuallyAppendToExisting\(",
        new_finish + "    private fun actuallyAppendToExisting(",
        "duplicate write-plan orchestration",
    )

    text = regex_once(
        text,
        r"\n    private fun isQuotaError\(error: Throwable\): Boolean =.*?"
        r"\n(?:\s*\n)+    private fun maybeShowWelcome\(",
        "\n\n    private fun maybeShowWelcome(",
        "remove MainActivity destination quota helper",
    )

    MAIN.write_text(text, encoding="utf-8")
    print("MainActivity: v1.4.16 destination refactor applied")


def patch_release_preflight() -> None:
    path = ROOT / "scripts/release-preflight.sh"
    text = path.read_text(encoding="utf-8")
    text = text.replace("docs/v.1.4.15/", "docs/v.1.4.16/")
    text = text.replace("versionCode = 49", "versionCode = 50")
    text = text.replace('versionName = "1.4.15"', 'versionName = "1.4.16"')
    text = text.replace("| v1.4.15 | **NOT TESTED YET** |", "| v1.4.16 | **NOT TESTED YET** |")
    text = text.replace("v1.4.15 must start NOT TESTED YET", "v1.4.16 must start NOT TESTED YET")

    check_anchor = 'check_file "scripts/playlist-write-coordinator-audit.sh"\n'
    check_insert = (
        'check_file "scripts/destination-coordinator-audit.sh"\n'
        'check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"\n'
    )
    if "destination-coordinator-audit.sh" not in text:
        text = text.replace(check_anchor, check_anchor + check_insert, 1)

    run_anchor = "bash scripts/playlist-write-coordinator-audit.sh\n"
    if "bash scripts/destination-coordinator-audit.sh" not in text:
        text = text.replace(
            run_anchor,
            run_anchor + "bash scripts/destination-coordinator-audit.sh\n",
            1,
        )

    if 'echo "- DestinationCoordinator extracted"' not in text:
        text += '\necho "- DestinationCoordinator extracted"\n'

    path.write_text(text, encoding="utf-8")
    print("release-preflight: current release guards updated")


def patch_qa_audit() -> None:
    path = ROOT / "scripts/qa-plan-audit.sh"
    text = path.read_text(encoding="utf-8")
    text = text.replace(
        "| v1.4.15 | **NOT TESTED YET** |",
        "| v1.4.16 | **NOT TESTED YET** |",
    )
    text = text.replace(
        "v1.4.15 must start NOT TESTED YET",
        "v1.4.16 must start NOT TESTED YET",
    )
    path.write_text(text, encoding="utf-8")
    print("qa-plan-audit: current release guard updated")


def patch_changelog() -> None:
    path = ROOT / "CHANGELOG.md"
    text = path.read_text(encoding="utf-8")
    if "## v1.4.16" in text:
        return
    section = '''## v1.4.16
- Cleanup Wave 5: extracted destination playlist / duplicate orchestration into `DestinationCoordinator`.
- DestinationCoordinator now owns eligible-track selection, destination playlist caching/selection,
  existing-playlist scan quota accounting, exact-videoId duplicate analysis and duplicate write planning.
- MainActivity remains the auth/UI/executor bridge and hands the final write plan to PlaylistWriteCoordinator.
- Added `scripts/destination-coordinator-audit.sh`.
- Added immutable `docs/v.1.4.16/` release documentation, diagrams and QA snapshot.
- BUG-003/Q-003 auth recovery failure remains deferred and is not claimed fixed.
- v1.4.16 = NOT TESTED YET.
- versionCode 50 / versionName 1.4.16.

'''
    heading = "# Журнал змін (Changelog)\n\n"
    if heading not in text:
        fail("CHANGELOG heading not found")
    path.write_text(text.replace(heading, heading + section, 1), encoding="utf-8")
    print("CHANGELOG: v1.4.16 section added")


def patch_versioned_docs_index() -> None:
    path = ROOT / "TERMINAL_APPLY_VERSIONED_DOCS.md"
    text = path.read_text(encoding="utf-8")
    entry = "- `TERMINAL_APPLY_v1.4.16.md` — DestinationCoordinator extraction + destination/duplicate orchestration\n"
    if entry not in text:
        if not text.endswith("\n"):
            text += "\n"
        text += "\n" + entry
        path.write_text(text, encoding="utf-8")
        print("versioned docs index: v1.4.16 added")


def snapshot_qa() -> None:
    src = ROOT / "qa"
    dst = ROOT / "docs/v.1.4.16/qa"
    dst.mkdir(parents=True, exist_ok=True)
    if not src.exists():
        fail("qa/ directory is missing")
    for source in src.iterdir():
        if source.is_file():
            shutil.copy2(source, dst / source.name)
    print("QA: current global plans snapshotted to docs/v.1.4.16/qa/")


def generate_manifest() -> None:
    try:
        raw = subprocess.check_output(
            ["git", "ls-files", "--cached", "--others", "--exclude-standard"],
            cwd=ROOT,
            text=True,
        )
    except Exception as error:
        fail(f"git ls-files failed: {error}")

    files = sorted(
        p for p in raw.splitlines()
        if p and p != "FILE_MANIFEST.txt" and (ROOT / p).is_file()
    )
    lines = ["YTM Importer v1.4.16 FULL — FILE MANIFEST", ""]
    for relative in files:
        data = (ROOT / relative).read_bytes()
        digest = hashlib.sha256(data).hexdigest()[:16]
        lines.append(f"{digest}  {relative}")
    (ROOT / "FILE_MANIFEST.txt").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"FILE_MANIFEST: {len(files)} files recorded")


def main() -> None:
    if not (ROOT / ".git").exists():
        fail("Run this script from the root of the YTM Git repository")

    patch_main_activity()
    patch_release_preflight()
    patch_qa_audit()
    patch_changelog()
    patch_versioned_docs_index()
    snapshot_qa()
    generate_manifest()
    print("\nOK: YTM Importer v1.4.16 patch stage completed")
    print("Next: run destination-coordinator-audit.sh and release-preflight.sh")


if __name__ == "__main__":
    main()

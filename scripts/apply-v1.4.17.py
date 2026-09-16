from pathlib import Path
import shutil

ROOT = Path('.')

def replace_once(path, old, new):
    p = ROOT / path
    s = p.read_text(encoding='utf-8')
    if new in s:
        print(f'{path}: already patched')
        return
    if old not in s:
        raise SystemExit(f'PATCH FAILED: marker not found in {path}')
    p.write_text(s.replace(old, new, 1), encoding='utf-8')
    print(f'{path}: patched')

replace_once('app/build.gradle.kts', 'versionCode = 50\n        versionName = "1.4.16"', 'versionCode = 51\n        versionName = "1.4.17"')
replace_once('app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', '        root.addView(\n            list,\n            LinearLayout.LayoutParams(\n                ViewGroup.LayoutParams.MATCH_PARENT,\n                0,\n                1f\n            )\n        )\n\n        list.setOnItemClickListener {', '        root.addView(\n            list,\n            LinearLayout.LayoutParams(\n                ViewGroup.LayoutParams.MATCH_PARENT,\n                0,\n                1f\n            )\n        )\n\n        val canOpenDestination =\n            snapshot.playlist.tracks.any { track ->\n                !track.selectedVideoId.isNullOrBlank() &&\n                    track.status != TrackStatus.SKIPPED\n            }\n\n        if (canOpenDestination) {\n            root.addView(\n                actionButton(\n                    label = "Далі → Створити / додати",\n                    primary = true\n                ) {\n                    saveSnapshot()\n                    setResult(\n                        RESULT_OK,\n                        Intent().putExtra(\n                            EXTRA_OPEN_DESTINATION,\n                            true\n                        )\n                    )\n                    finish()\n                },\n                LinearLayout.LayoutParams(\n                    ViewGroup.LayoutParams.MATCH_PARENT,\n                    dp(56)\n                ).apply {\n                    setMargins(dp(12), 0, dp(12), dp(10))\n                }\n            )\n        }\n\n        list.setOnItemClickListener {')
replace_once('app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', '        const val EXTRA_REPEAT_SEARCH =\n            "review_repeat_search"\n\n        const val EXTRA_MANUAL_VIDEO_ID =', '        const val EXTRA_REPEAT_SEARCH =\n            "review_repeat_search"\n\n        const val EXTRA_OPEN_DESTINATION =\n            "review_open_destination"\n\n        const val EXTRA_MANUAL_VIDEO_ID =')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '                val videoId =\n                    data.getStringExtra(', '                if (\n                    data.getBooleanExtra(\n                        ReviewActivity.EXTRA_OPEN_DESTINATION,\n                        false\n                    )\n                ) {\n                    createPlaylist()\n                    return\n                }\n\n                val videoId =\n                    data.getStringExtra(')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', 'import com.saney.ytmimporter.youtube.YouTubeApi\nimport java.util.concurrent.Executors', 'import com.saney.ytmimporter.youtube.YouTubeApi\nimport com.saney.ytmimporter.youtube.YouTubeApiException\nimport java.util.concurrent.Executors')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '        updatePrimaryActions()\n    }\n\n    private fun searchAll(', '        updatePrimaryActions()\n    }\n\n    private fun isAuthorizationFailure(\n        error: Throwable\n    ): Boolean {\n        var current: Throwable? = error\n\n        while (current != null) {\n            if (\n                current is YouTubeApiException &&\n                current.httpCode == 401\n            ) {\n                return true\n            }\n            current = current.cause\n        }\n\n        return false\n    }\n\n    private fun invalidateAuthorizationIfNeeded(\n        error: Throwable\n    ): Boolean {\n        if (!isAuthorizationFailure(error)) return false\n\n        accessToken = null\n        googleAccountInfo = null\n        youtubeChannelInfo = null\n        restoringPriorAuthorization = false\n        pendingAfterAuth = null\n        AuthSessionStore.clear()\n\n        updateAccountPanel()\n        status(\n            "Авторизація Google/YTM більше не дійсна. " +\n                "Натисніть «2. Google / YTM» і увійдіть знову."\n        )\n        toast("Сесію Google/YTM потрібно відновити")\n        return true\n    }\n\n    private fun searchAll(')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '                    }.onFailure { error ->\n                        toast(\n                            ErrorMessages.userMessage(\n                                error,\n                                "Не вдалося завантажити плейлисти"\n                            )\n                        )\n                    }', '                    }.onFailure { error ->\n                        if (invalidateAuthorizationIfNeeded(error)) {\n                            return@onFailure\n                        }\n\n                        toast(\n                            ErrorMessages.userMessage(\n                                error,\n                                "Не вдалося завантажити плейлисти"\n                            )\n                        )\n                    }')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '                    }.onFailure { error ->\n                        openDestinationScanFailed(\n                            p = p,\n                            selected = selected,\n                            target = target,\n                            error = error\n                        )\n                    }', '                    }.onFailure { error ->\n                        if (invalidateAuthorizationIfNeeded(error)) {\n                            return@onFailure\n                        }\n\n                        openDestinationScanFailed(\n                            p = p,\n                            selected = selected,\n                            target = target,\n                            error = error\n                        )\n                    }')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '                    UiChrome.DialogAction(\n                        label = "Копіювати"\n                    ) {\n                        copyPlaylistLink()\n                    },', '                    UiChrome.DialogAction(\n                        label = "Копіювати посилання"\n                    ) {\n                        copyPlaylistLink()\n                    },')
replace_once('app/src/main/java/com/saney/ytmimporter/MainActivity.kt', '            actionLayout =\n                UiChrome.DialogActionLayout.AUTO\n        )\n    }\n\n    private fun playlistUrl(): String? {', '            actionLayout =\n                UiChrome.DialogActionLayout.VERTICAL_WITH_TEXT_CLOSE\n        )\n    }\n\n    private fun playlistUrl(): String? {')

# Update release guards
p = ROOT / 'scripts/release-preflight.sh'
s = p.read_text(encoding='utf-8')
s = s.replace('docs/v.1.4.16/', 'docs/v.1.4.17/')
s = s.replace('versionCode = 50', 'versionCode = 51')
s = s.replace('Expected versionCode = 50', 'Expected versionCode = 51')
s = s.replace('versionName = "1.4.16"', 'versionName = "1.4.17"')
s = s.replace("grep -Fq '| v1.4.16 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md", "grep -Fq '| v1.4.17 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md")
s = s.replace('v1.4.16 must start NOT TESTED YET', 'v1.4.17 must start NOT TESTED YET')
if 'scripts/v1417-auth-flow-audit.sh' not in s:
    s = s.replace('check_file "scripts/destination-coordinator-audit.sh"\n', 'check_file "scripts/destination-coordinator-audit.sh"\ncheck_file "scripts/v1417-auth-flow-audit.sh"\n', 1)
    s = s.replace('bash scripts/destination-coordinator-audit.sh\n', 'bash scripts/destination-coordinator-audit.sh\nbash scripts/v1417-auth-flow-audit.sh\n', 1)
p.write_text(s, encoding='utf-8')

p = ROOT / 'scripts/qa-plan-audit.sh'
s = p.read_text(encoding='utf-8')
s = s.replace("grep -Fq '| v1.4.16 | **NOT TESTED YET** |' \"$STATUS\"", "grep -Fq '| v1.4.17 | **NOT TESTED YET** |' \"$STATUS\"")
s = s.replace('v1.4.16 must start NOT TESTED YET', 'v1.4.17 must start NOT TESTED YET')
p.write_text(s, encoding='utf-8')

# Snapshot global QA into this release without overwriting release-specific files
qa_src = ROOT / 'qa'
qa_dst = ROOT / 'docs/v.1.4.17/qa'
qa_dst.mkdir(parents=True, exist_ok=True)
for item in qa_src.iterdir():
    if item.is_file():
        target = qa_dst / item.name
        if not target.exists():
            shutil.copy2(item, target)

print('OK: v1.4.17 patch applied')

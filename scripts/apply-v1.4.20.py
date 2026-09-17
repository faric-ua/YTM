#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def replace_once(rel, old, new):
    text = read(rel)
    if new in text:
        print(f"SKIP already applied: {rel}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(
            f"STOP: expected exactly one anchor in {rel}; found {count}\n"
            f"ANCHOR:\n{old[:500]}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {rel}")

def replace_two_line_guard(
    rel,
    old_marker,
    old_fail_marker,
    new_lines,
    already_marker
):
    text = read(rel)
    if already_marker in text:
        print(f"SKIP guard already updated: {rel}")
        return

    lines = text.splitlines()
    match_indexes = [
        i for i, line in enumerate(lines)
        if old_marker in line
    ]

    if len(match_indexes) != 1:
        raise SystemExit(
            f"STOP: expected exactly one guard marker in {rel}; "
            f"found {len(match_indexes)}: {old_marker}"
        )

    i = match_indexes[0]

    if i + 1 >= len(lines) or old_fail_marker not in lines[i + 1]:
        raise SystemExit(
            f"STOP: guard fail-line mismatch in {rel}: "
            f"{old_fail_marker}"
        )

    lines[i:i + 2] = new_lines
    write(rel, "\n".join(lines) + "\n")
    print(f"PATCH guard: {rel}")

replace_once(
    "app/build.gradle.kts",
    '        versionCode = 53\n        versionName = "1.4.19"',
    '        versionCode = 54\n        versionName = "1.4.20"',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''                    actionButton(
                        label =
                            "Експортувати всі плейлисти в папку",
                        primary = false
                    ) {
''',
    '''                    actionButton(
                        label =
                            "Експортувати всі плейлисти в папку",
                        primary = false,
                        topMarginDp = 10
                    ) {
''',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''    private fun actionButton(
        label: String,
        primary: Boolean,
        action: () -> Unit
    ): Button =
''',
    '''    private fun actionButton(
        label: String,
        primary: Boolean,
        topMarginDp: Int = 0,
        action: () -> Unit
    ): Button =
''',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            setPadding(dp(16), dp(7), dp(16), dp(7))
''',
    '''            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            minimumHeight = dp(58)
            minHeight = dp(58)
            setPadding(
                dp(16),
                dp(10),
                dp(16),
                dp(10)
            )
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 14
            )
''',
)

replace_once(
    "app/src/main/java/com/saney/ytmimporter/ImportActivity.kt",
    '''            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(54)
                )
''',
    '''            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin =
                        dp(topMarginDp)
                }
''',
)

replace_once(
    "RELEASE_TEST_STATUS.md",
    '| v1.4.19 | **NOT TESTED YET** | Read-only bulk export of all account playlists to a chosen device folder. |',
    '| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** | 21/21 playlists exported, manifest counts matched, and an exported project reopened with exact videoId preserved. Source before/after refresh was not separately phone-verified. |\n'
    '| v1.4.20 | **NOT TESTED YET** | Import-screen account action button height/spacing fix; requires short phone UI smoke. |',
)

replace_once(
    "PROJECT_STATUS.txt",
    "Version: 1.4.19\nVersion code: 53",
    "Version: 1.4.20\nVersion code: 54",
)

replace_once(
    "PROJECT_STATUS.txt",
    "v1.4.19 NOT TESTED YET",
    "v1.4.19 PARTIALLY PHONE-TESTED — BULK EXPORT PASS\nv1.4.20 NOT TESTED YET",
)

status = read("PROJECT_STATUS.txt")
if "v1.4.19 phone QA confirmed:" not in status:
    anchor = "\nKnown:\n"
    summary = '''
v1.4.19 phone QA confirmed:
- bulk export found 21 account playlists
- 21 YTM Projects exported
- skipped 0 / failed 0
- playlistItems.list = 33 requests
- export session contained 22 objects = 21 projects + manifest
- manifest counts matched the result dialog
- exported `mylist` reopened with 2/2 exact videoId and 0 missing
- Review opened with both tracks ready
- UI observation: bulk-export button text clipped and button spacing too tight

v1.4.20 focus:
- flexible Import action-button height
- full two-line bulk-export label
- 10dp spacing between the two account actions
- short phone UI smoke
'''
    if anchor not in status:
        raise SystemExit("STOP: PROJECT_STATUS Known anchor missing")
    write("PROJECT_STATUS.txt", status.replace(anchor, summary + anchor, 1))
    print("PATCH: PROJECT_STATUS.txt (phone QA + v1.4.20 focus)")

backlog = read("BACKLOG.md")
backlog = backlog.replace(
    "## Current\nv1.4.19 — export all connected-account playlists to a device folder",
    "## Current\nv1.4.20 — Import account-action button layout fix",
    1
)
for old, new in [
    ("- [ ] GitHub build\n- [ ] phone test: choose export folder",
     "- [x] GitHub build\n- [x] phone test: choose export folder"),
    ("- [ ] phone test: export small account library",
     "- [x] phone test: export account library (21 playlists)"),
    ("- [ ] verify project-file count and manifest",
     "- [x] verify project-file count and manifest"),
    ("- [ ] reopen one exported YTM Project",
     "- [x] reopen one exported YTM Project"),
]:
    if old in backlog:
        backlog = backlog.replace(old, new, 1)

section20 = '''## v1.4.20
- [x] replace fixed 54dp Import action height with WRAP_CONTENT + minimum height
- [x] keep long account-action labels readable on up to two lines
- [x] add 10dp spacing before bulk-export action
- [x] add static UI-layout audit
- [x] add release docs + phone UI-smoke plan
- [ ] GitHub build
- [ ] phone test: Import button layout screenshot
- [ ] phone smoke: bulk-export folder picker still opens

'''
if "## v1.4.20\n" not in backlog:
    marker = "## Next\n"
    if marker not in backlog:
        raise SystemExit("STOP: BACKLOG Next anchor missing")
    backlog = backlog.replace(marker, section20 + marker, 1)

old_next = '''## Next
Phone-test v1.4.19 bulk account export, then decide whether to add:
- selective multi-playlist export;
- import of a bulk-export manifest;
- incremental/sync-style account backup.
'''
new_next = '''## Next
Phone-test the v1.4.20 UI fix. After that, continue account-library work only if useful:
- selective multi-playlist export;
- import of a bulk-export manifest;
- incremental/sync-style account backup.
'''
if old_next in backlog:
    backlog = backlog.replace(old_next, new_next, 1)

write("BACKLOG.md", backlog)
print("PATCH: BACKLOG.md")

entry20 = '''## v1.4.20
- Fixed clipped text on long Import action buttons by replacing fixed height with WRAP_CONTENT + minimum height.
- Added controlled autosizing and comfortable vertical padding for Import action buttons.
- Added 10dp spacing between `Вибрати плейлист з YTM` and `Експортувати всі плейлисти в папку`.
- Recorded v1.4.19 bulk-export phone test: 21/21 projects exported, manifest matched, round-trip project reopen passed.
- versionCode 54 / versionName 1.4.20.
- v1.4.20 = NOT PHONE-TESTED YET.

'''
replace_once(
    "CHANGELOG.md",
    "# Журнал змін (Changelog)\n\n",
    "# Журнал змін (Changelog)\n\n" + entry20,
)

replace_once(
    "CHANGELOG.md",
    "- v1.4.19 = NOT PHONE-TESTED YET.",
    "- v1.4.19 phone test PASS for tested bulk-export path: 21/21 projects exported, manifest counts matched, and one exported project reopened with exact videoId preserved.",
)

checklist_path = "docs/v.1.4.19/REGRESSION_CHECKLIST.md"
updates = [
    ("- [ ] v1.4.19 installs successfully.", "- [x] v1.4.19 installs successfully."),
    ("- [ ] Step 2 Google/YTM is connected.", "- [x] Step 2 Google/YTM is connected."),
    ("- [ ] Import screen shows **Експортувати всі плейлисти в папку**.", "- [x] Import screen shows **Експортувати всі плейлисти в папку**."),
    ("- [ ] Android folder picker opens.", "- [x] Android folder picker opens."),
    ("- [ ] Selected folder receives one timestamped export session folder.", "- [x] Selected folder receives one timestamped export session folder."),
    ("- [ ] Session folder contains `manifest.json`.", "- [x] Session folder contains `manifest.json`."),
    ("- [ ] Non-empty accessible playlists produce YTM Project files.", "- [x] Non-empty accessible playlists produce YTM Project files."),
    ("- [ ] Exported project-file count matches manifest `EXPORTED` count.", "- [x] Exported project-file count matches manifest `EXPORTED` count."),
    ("- [ ] Result dialog reports total/exported/skipped/failed.", "- [x] Result dialog reports total/exported/skipped/failed."),
    ("- [ ] Result dialog reports playlistItems.list request count.", "- [x] Result dialog reports playlistItems.list request count."),
    ("- [ ] One exported YTM Project can be reopened.", "- [x] One exported YTM Project can be reopened."),
    ("- [ ] Reopened project preserves exact videoId values.", "- [x] Reopened project preserves exact videoId values."),
    ("- [ ] Step 3 exact-selection path still opens Review without automatic search.", "- [x] Step 3 exact-selection path still opens Review without automatic search."),
    ("- [ ] Existing YTM Project import still works.", "- [x] Existing YTM Project import still works."),
]
for old, new in updates:
    text = read(checklist_path)
    if new not in text:
        if old not in text:
            raise SystemExit(f"STOP: v1.4.19 checklist anchor missing: {old}")
        write(checklist_path, text.replace(old, new, 1))
        print(f"PATCH: {checklist_path}: {new}")

replace_two_line_guard(
    rel="scripts/qa-plan-audit.sh",
    old_marker="| v1.4.19 | **NOT TESTED YET** |",
    old_fail_marker="v1.4.19 must start NOT TESTED YET",
    new_lines=[
        'grep -Fq \'| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** |\' "$STATUS" \\',
        '  || fail "v1.4.19 bulk-export phone-test PASS status missing"',
        'grep -Fq \'| v1.4.20 | **NOT TESTED YET** |\' "$STATUS" \\',
        '  || fail "v1.4.20 must start NOT TESTED YET"',
    ],
    already_marker="v1.4.19 bulk-export phone-test PASS status missing"
)

replace_once(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.19/RELEASE.md"',
    'check_file "docs/v.1.4.20/RELEASE.md"',
)
replace_once(
    "scripts/release-preflight.sh",
    'check_file "docs/v.1.4.19/REGRESSION_CHECKLIST.md"',
    'check_file "docs/v.1.4.20/REGRESSION_CHECKLIST.md"',
)
replace_once(
    "scripts/release-preflight.sh",
    'check_file "scripts/v1419-account-library-export-audit.sh"\n',
    'check_file "scripts/v1419-account-library-export-audit.sh"\n'
    'check_file "scripts/v1420-import-button-layout-audit.sh"\n',
)
replace_once(
    "scripts/release-preflight.sh",
    "bash scripts/v1419-account-library-export-audit.sh\n",
    "bash scripts/v1420-import-button-layout-audit.sh\n",
)
replace_once(
    "scripts/release-preflight.sh",
    "grep -q 'versionCode = 53' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 53\"",
    "grep -q 'versionCode = 54' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 54\"",
)
replace_once(
    "scripts/release-preflight.sh",
    "grep -q 'versionName = \"1.4.19\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.19\"'",
    "grep -q 'versionName = \"1.4.20\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.20\"'",
)

replace_two_line_guard(
    rel="scripts/release-preflight.sh",
    old_marker="| v1.4.19 | **NOT TESTED YET** |",
    old_fail_marker="v1.4.19 must start NOT TESTED YET",
    new_lines=[
        "grep -Fq '| v1.4.19 | **PARTIALLY PHONE-TESTED — PASS FOR BULK EXPORT PATH** |' RELEASE_TEST_STATUS.md \\",
        '  || fail "v1.4.19 bulk-export phone-test PASS status missing"',
        "grep -Fq '| v1.4.20 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\",
        '  || fail "v1.4.20 must start NOT TESTED YET"',
    ],
    already_marker="v1.4.19 bulk-export phone-test PASS status missing"
)

print()
print("PASS: v1.4.19 phone QA + v1.4.20 UI fix applied")

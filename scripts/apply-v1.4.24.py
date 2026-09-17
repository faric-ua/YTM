#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def replace_once(rel, old, new, label):
    text = read(rel)
    if new in text:
        print(f"SKIP: {label}")
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(
            f"STOP: {label}: expected exactly 1 anchor; found {count}\n"
            f"ANCHOR:\n{old[:1000]}"
        )
    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {label}")

def ensure_import(rel):
    text = read(rel)
    line = "import com.saney.ytmimporter.ui.AppThemeManager\n"
    if line in text:
        print(f"SKIP import: {rel}")
        return
    package = "package com.saney.ytmimporter\n"
    if package not in text:
        raise SystemExit(f"STOP: package anchor missing: {rel}")
    write(rel, text.replace(package, package + line, 1))
    print(f"PATCH import: {rel}")

def ensure_apply_window(rel):
    text = read(rel)
    if "AppThemeManager.applyWindow(this)" in text:
        print(f"SKIP applyWindow: {rel}")
        return
    anchor = "        super.onCreate(savedInstanceState)\n"
    if anchor not in text:
        raise SystemExit(f"STOP: onCreate super anchor missing: {rel}")
    write(
        rel,
        text.replace(
            anchor,
            anchor + "        AppThemeManager.applyWindow(this)\n",
            1
        )
    )
    print(f"PATCH applyWindow: {rel}")

def replace_background_constant(rel, activity_name):
    text = read(rel)
    pattern = re.compile(r"setBackgroundColor\(\s*BACKGROUND\s*\)")
    replacement = (
        "setBackgroundColor("
        f"AppThemeManager.palette(this@{activity_name}).background"
        ")"
    )
    text2, count = pattern.subn(replacement, text)
    if count == 0:
        if replacement in text:
            print(f"SKIP background: {rel}")
            return
        raise SystemExit(f"STOP: BACKGROUND usage missing: {rel}")
    write(rel, text2)
    print(f"PATCH background: {rel} ({count})")

def replace_between(rel, start_marker, end_marker, new_block, label):
    text = read(rel)
    if new_block in text:
        print(f"SKIP: {label}")
        return
    start = text.find(start_marker)
    if start < 0:
        raise SystemExit(f"STOP: start marker missing for {label}")
    end = text.find(end_marker, start + len(start_marker))
    if end < 0:
        raise SystemExit(f"STOP: end marker missing for {label}")
    write(rel, text[:start] + new_block + text[end:])
    print(f"PATCH: {label}")

def replace_in_section(rel, heading, old, new, label):
    text = read(rel)
    start = text.find(heading)
    if start < 0:
        raise SystemExit(f"STOP: section missing for {label}")
    end = text.find("\n## ", start + len(heading))
    if end < 0:
        end = len(text)
    section = text[start:end]
    if new in section:
        print(f"SKIP: {label}")
        return
    if old not in section:
        raise SystemExit(f"STOP: section anchor missing for {label}: {old}")
    section = section.replace(old, new, 1)
    write(rel, text[:start] + section + text[end:])
    print(f"PATCH: {label}")

def append_to_section_once(rel, heading, snippet, token, label):
    text = read(rel)
    if token in text:
        print(f"SKIP: {label}")
        return
    start = text.find(heading)
    if start < 0:
        raise SystemExit(f"STOP: section missing for {label}")
    end = text.find("\n## ", start + len(heading))
    if end < 0:
        end = len(text)
    text = text[:end] + snippet + text[end:]
    write(rel, text)
    print(f"PATCH: {label}")

def insert_before_once(rel, marker, snippet, token, label):
    text = read(rel)
    if token in text:
        print(f"SKIP: {label}")
        return
    if marker not in text:
        raise SystemExit(f"STOP: marker missing for {label}")
    write(rel, text.replace(marker, snippet + marker, 1))
    print(f"PATCH: {label}")
replace_once('app/build.gradle.kts', '        versionCode = 57\n        versionName = "1.4.23"', '        versionCode = 58\n        versionName = "1.4.24"', 'release version')
ensure_import('app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt')
ensure_apply_window('app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt')
replace_background_constant('app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', 'DestinationActivity')
ensure_import('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt')
ensure_apply_window('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt')
replace_background_constant('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt', 'HistoryActivity')
ensure_import('app/src/main/java/com/saney/ytmimporter/PendingActivity.kt')
ensure_apply_window('app/src/main/java/com/saney/ytmimporter/PendingActivity.kt')
replace_background_constant('app/src/main/java/com/saney/ytmimporter/PendingActivity.kt', 'PendingActivity')
ensure_import('app/src/main/java/com/saney/ytmimporter/DataActivity.kt')
ensure_apply_window('app/src/main/java/com/saney/ytmimporter/DataActivity.kt')
replace_background_constant('app/src/main/java/com/saney/ytmimporter/DataActivity.kt', 'DataActivity')
ensure_import('app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt')
ensure_apply_window('app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt')
replace_background_constant('app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt', 'ServiceActivity')
replace_between('app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt', '    private fun roundedBackground(', '    private fun dp(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/DestinationActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt', '    private fun roundedBackground(', '    private fun toast(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/PendingActivity.kt', '    private fun roundedBackground(', '    private fun toast(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/PendingActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/DataActivity.kt', '    private fun roundedBackground(', '    private fun exportTimestamp(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/DataActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt', '    private fun roundedBackground(', '    private fun toast(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/ImportActivity.kt', '    private fun roundedBackground(', '    private fun toast(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/ImportActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', '    private fun roundedBackground(', '    private fun toast(', '    private fun roundedBackground(\n        color: Int,\n        radiusDp: Int,\n        strokeColor: Int? = null\n    ): android.graphics.drawable.Drawable {\n        val palette =\n            AppThemeManager.palette(this)\n\n        val mappedFill =\n            when (color) {\n                Color.rgb(15, 16, 19) ->\n                    palette.background\n\n                Color.rgb(25, 27, 32) ->\n                    palette.surface\n\n                Color.rgb(31, 33, 39),\n                Color.rgb(37, 39, 46) ->\n                    palette.surfaceAlt\n\n                Color.rgb(196, 0, 42) ->\n                    palette.accentFill\n\n                Color.rgb(39, 25, 27),\n                Color.rgb(31, 29, 24) ->\n                    palette.surface\n\n                else ->\n                    color\n            }\n\n        val accentOverride =\n            when (strokeColor) {\n                Color.rgb(95, 48, 52) ->\n                    palette.danger\n\n                Color.rgb(83, 68, 37) ->\n                    palette.warning\n\n                else ->\n                    null\n            }\n\n        val useAccentStroke =\n            color == Color.rgb(196, 0, 42) ||\n                accentOverride != null\n\n        return AppThemeManager.surfaceDrawable(\n            context = this,\n            fill = mappedFill,\n            radiusDp = radiusDp,\n            accentStroke = useAccentStroke,\n            accentOverride = accentOverride\n        )\n    }\n\n', 'app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt themed roundedBackground')
replace_between('app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt', '    private fun statusColor(', '    private fun topBar(', '    private fun statusColor(\n        status: TrackStatus\n    ): Int {\n        val palette =\n            AppThemeManager.palette(this)\n\n        return when (status) {\n            TrackStatus.MATCHED,\n            TrackStatus.ADDED ->\n                palette.success\n\n            TrackStatus.REVIEW,\n            TrackStatus.PENDING ->\n                palette.warning\n\n            TrackStatus.DUPLICATE ->\n                palette.duplicate\n\n            TrackStatus.MISSING,\n            TrackStatus.FAILED ->\n                palette.danger\n\n            else ->\n                palette.muted\n        }\n    }\n\n', 'Review semantic status colors')
replace_between('app/src/main/java/com/saney/ytmimporter/HistoryActivity.kt', '    private fun historyStatusColor(', '    private fun formatHistoryDate(', '    private fun historyStatusColor(\n        status: HistoryStatus\n    ): Int {\n        val palette =\n            AppThemeManager.palette(this)\n\n        return when (status) {\n            HistoryStatus.COMPLETED ->\n                palette.success\n\n            HistoryStatus.RUNNING,\n            HistoryStatus.PARTIAL,\n            HistoryStatus.PENDING_QUOTA ->\n                palette.warning\n\n            HistoryStatus.FAILED ->\n                palette.danger\n        }\n    }\n\n', 'History semantic status colors')
replace_once('app/src/main/java/com/saney/ytmimporter/ServiceActivity.kt', '    private fun buildUi() {\n        when (page) {', '    private fun buildUi() {\n        AppThemeManager.applyWindow(this)\n\n        when (page) {', 'Service nested-page window theme')
replace_once('RELEASE_TEST_STATUS.md', '| v1.4.23 | **NOT TESTED YET** | Button Fit + Home Polish: single-line utility actions, smaller vector icons and tighter adaptive workflow-button typography. |', '| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** | Blue Dark Home: utility row stayed single-line, workflow labels fit, vector icons and current-playlist card remained readable. |\n| v1.4.24 | **NOT TESTED YET** | Theme Wave 2: extend selected palette to Import, Review, Destination, History, Queue, Data and Service. |', 'release test status')
replace_once('PROJECT_STATUS.txt', 'Version: 1.4.23\nVersion code: 57', 'Version: 1.4.24\nVersion code: 58', 'project version')
replace_once('PROJECT_STATUS.txt', 'v1.4.23 NOT TESTED YET', 'v1.4.23 PARTIALLY PHONE-TESTED — HOME FIT PASS\nv1.4.24 NOT TESTED YET', 'project release status')
insert_before_once('PROJECT_STATUS.txt', '\nKnown:\n', '\nv1.4.24 focus:\n- Theme Wave 2\n- Import / Review / Destination / History / Queue / Data / Service\n- screen and system-bar background follows selected palette\n- legacy rounded surfaces map to theme surfaces/accent\n- semantic Review/History status colors use shared palette\n- functional auth/search/write behavior remains unchanged\n', 'v1.4.24 focus:', 'project v1.4.24 focus')
replace_once('BACKLOG.md', '## Current\nv1.4.23 — Button Fit + Home Polish', '## Current\nv1.4.24 — Theme Wave 2', 'BACKLOG current')
replace_in_section('BACKLOG.md', '## v1.4.23\n', '- [ ] GitHub build', '- [x] GitHub build', 'v1.4.23 build PASS')
replace_in_section('BACKLOG.md', '## v1.4.23\n', '- [ ] phone test: utility row all one line', '- [x] phone test: utility row all one line', 'v1.4.23 utility fit PASS')
replace_in_section('BACKLOG.md', '## v1.4.23\n', '- [ ] phone test: top four actions fit', '- [x] phone test: top four actions fit', 'v1.4.23 workflow fit PASS')
append_to_section_once('BACKLOG.md', '## v1.4.23\n', '\n- [x] Blue Dark Home fit PASS on real phone\n', 'Blue Dark Home fit PASS', 'v1.4.23 phone evidence note')
insert_before_once('BACKLOG.md', '## Next\n', '## v1.4.24\n- [x] apply selected theme to Destination\n- [x] apply selected theme to History\n- [x] apply selected theme to Pending Queue\n- [x] apply selected theme to Data / Backup\n- [x] apply selected theme to Service and nested pages\n- [x] finish themed legacy surfaces in Import / Review\n- [x] use shared semantic colors in Review / History\n- [x] record v1.4.23 Home-fit phone PASS\n- [x] add Theme Wave 2 audit\n- [ ] GitHub build\n- [ ] phone test: Import\n- [ ] phone test: Review\n- [ ] phone test: Destination\n- [ ] phone test: History / Queue / Data / Service\n- [ ] phone theme spot-check on one utility screen\n\n', '## v1.4.24\n', 'BACKLOG v1.4.24 section')
insert_before_once('CHANGELOG.md', '## v1.4.23\n', '## v1.4.24\n- Theme Wave 2.\n- Extended the selected Neon / Blue / Green palette to Destination, History, Pending Queue, Data and Service.\n- Finished theme mapping for remaining legacy rounded surfaces in Import and Review.\n- Service nested pages now refresh system-bar colors from the active theme.\n- Review and History status colors now use shared semantic palette colors.\n- Recorded v1.4.23 real-phone Home button-fit PASS.\n- No intended auth/search/write domain behavior changes.\n- versionCode 58 / versionName 1.4.24.\n- v1.4.24 = NOT PHONE-TESTED YET.\n\n', '## v1.4.24\n', 'CHANGELOG v1.4.24')
replace_once('scripts/qa-plan-audit.sh', 'grep -Fq \'| v1.4.23 | **NOT TESTED YET** |\' "$STATUS" \\\n  || fail "v1.4.23 must start NOT TESTED YET"', 'grep -Fq \'| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** |\' "$STATUS" \\\n  || fail "v1.4.23 Home-fit phone PASS status missing"\ngrep -Fq \'| v1.4.24 | **NOT TESTED YET** |\' "$STATUS" \\\n  || fail "v1.4.24 must start NOT TESTED YET"', 'qa-plan v1.4.24 guards')
replace_once('scripts/release-preflight.sh', 'check_file "docs/v.1.4.23/RELEASE.md"', 'check_file "docs/v.1.4.24/RELEASE.md"', 'preflight release doc')
replace_once('scripts/release-preflight.sh', 'check_file "docs/v.1.4.23/REGRESSION_CHECKLIST.md"', 'check_file "docs/v.1.4.24/REGRESSION_CHECKLIST.md"', 'preflight regression doc')
replace_once('scripts/release-preflight.sh', 'bash scripts/v1423-button-fit-audit.sh', 'bash scripts/v1424-theme-wave2-audit.sh', 'active release audit')
replace_once('scripts/release-preflight.sh', 'grep -q \'versionCode = 57\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 57"', 'grep -q \'versionCode = 58\' app/build.gradle.kts \\\n  || fail "Expected versionCode = 58"', 'preflight versionCode')
replace_once('scripts/release-preflight.sh', 'grep -q \'versionName = "1.4.23"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.23"\'', 'grep -q \'versionName = "1.4.24"\' app/build.gradle.kts \\\n  || fail \'Expected versionName = "1.4.24"\'', 'preflight versionName')
replace_once('scripts/release-preflight.sh', 'grep -Fq \'| v1.4.23 | **NOT TESTED YET** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.23 must start NOT TESTED YET"', 'grep -Fq \'| v1.4.23 | **PARTIALLY PHONE-TESTED — PASS FOR HOME FIT** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.23 Home-fit PASS status missing"\ngrep -Fq \'| v1.4.24 | **NOT TESTED YET** |\' RELEASE_TEST_STATUS.md \\\n  || fail "v1.4.24 must start NOT TESTED YET"', 'preflight release-status guards')
insert_before_once('scripts/release-preflight.sh', 'check_file "app/src/main/java/com/saney/ytmimporter/destination/DestinationCoordinator.kt"\n', 'check_file "scripts/v1424-theme-wave2-audit.sh"\n', 'scripts/v1424-theme-wave2-audit.sh', 'preflight v1424 audit file guard')
print()
print('PASS: v1.4.24 Theme Wave 2 applied')

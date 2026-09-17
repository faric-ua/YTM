#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def read(rel):
    return (ROOT / rel).read_text(encoding="utf-8")

def write(rel, text):
    (ROOT / rel).write_text(text, encoding="utf-8")

def ensure_replace(rel, old, new, label):
    text = read(rel)

    if new in text:
        print(f"SKIP: {label}")
        return

    count = text.count(old)

    if count != 1:
        raise SystemExit(
            f"STOP: {label}: expected exactly 1 old anchor, found {count}\n"
            f"ANCHOR:\n{old[:1200]}"
        )

    write(rel, text.replace(old, new, 1))
    print(f"PATCH: {label}")

# ------------------------------------------------------------
# Version / already-applied early patches
# ------------------------------------------------------------

ensure_replace(
    "app/build.gradle.kts",
    '        versionCode = 56\n        versionName = "1.4.22"',
    '        versionCode = 57\n        versionName = "1.4.23"',
    "version"
)

MAIN = "app/src/main/java/com/saney/ytmimporter/MainActivity.kt"

ensure_replace(
    MAIN,
    """        val utilityRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), dp(8))
        }""",
    """        val utilityRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, dp(10), dp(8))
        }""",
    "utility row outer padding"
)

ensure_replace(
    MAIN,
    """                    if (index > 0) {
                        marginStart = dp(6)
                    }""",
    """                    if (index > 0) {
                        marginStart = dp(5)
                    }""",
    "utility row gap"
)

# ------------------------------------------------------------
# Fix the duplicate-anchor bug by patching whole functions.
# ------------------------------------------------------------

old_button = """    private fun button(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            applyButtonIcon(
                button = this,
                label = label,
                tint =
                    AppThemeManager
                        .palette(
                            this@MainActivity
                        )
                        .accent
            )
            setPadding(dp(14), dp(10), dp(14), dp(10))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 15
            )
            background =
                AppThemeManager.neutralButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )
            setOnClickListener {
                action()
            }
        }
"""

new_button = """    private fun button(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            applyButtonIcon(
                button = this,
                label = label,
                tint =
                    AppThemeManager
                        .palette(
                            this@MainActivity
                        )
                        .accent
            )
            setPadding(dp(9), dp(9), dp(9), dp(9))
            compoundDrawablePadding =
                dp(6)
            UiChrome.autoSizeButton(
                this,
                minSp = 9,
                maxSp = 13
            )
            background =
                AppThemeManager.neutralButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )
            setOnClickListener {
                action()
            }
        }
"""

ensure_replace(
    MAIN,
    old_button,
    new_button,
    "normal Home button fit"
)

old_primary = """    private fun primaryButton(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            applyButtonIcon(
                button = this,
                label = label,
                tint =
                    AppThemeManager
                        .palette(
                            this@MainActivity
                        )
                        .accent
            )
            setPadding(dp(14), dp(10), dp(14), dp(10))
            UiChrome.autoSizeButton(
                this,
                minSp = 11,
                maxSp = 15
            )
            background =
                AppThemeManager.accentButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )
            setOnClickListener {
                action()
            }
        }
"""

new_primary = """    private fun primaryButton(
        label: String,
        action: () -> Unit
    ): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 13.5f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            maxLines = 2
            applyButtonIcon(
                button = this,
                label = label,
                tint =
                    AppThemeManager
                        .palette(
                            this@MainActivity
                        )
                        .accent
            )
            setPadding(dp(9), dp(9), dp(9), dp(9))
            compoundDrawablePadding =
                dp(6)
            UiChrome.autoSizeButton(
                this,
                minSp = 9,
                maxSp = 13
            )
            background =
                AppThemeManager.accentButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )
            setOnClickListener {
                action()
            }
        }
"""

ensure_replace(
    MAIN,
    old_primary,
    new_primary,
    "primary Home button fit"
)

old_compact = """    private fun compactButton(
        label: String,
        action: () -> Unit
    ): Button =
        button(
            label = label,
            action = action
        ).apply {
            textSize = 12f
            setPadding(dp(9), dp(7), dp(9), dp(7))
            UiChrome.autoSizeButton(
                this,
                minSp = 10,
                maxSp = 13
            )
        }
"""

new_compact = """    private fun compactButton(
        label: String,
        action: () -> Unit
    ): Button =
        button(
            label = label,
            action = action
        ).apply {
            textSize = 11f
            maxLines = 1
            minLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(false)
            setPadding(
                dp(5),
                dp(6),
                dp(5),
                dp(6)
            )
            compoundDrawablePadding =
                dp(4)
            resizeButtonStartIcon(
                button = this,
                sizeDp = 17
            )
            UiChrome.autoSizeButton(
                this,
                minSp = 8,
                maxSp = 11
            )
        }
"""

ensure_replace(
    MAIN,
    old_compact,
    new_compact,
    "compact utility button fit"
)

old_icon_tail = """        button.compoundDrawablePadding =
            dp(8)

        button.compoundDrawableTintList =
            ColorStateList.valueOf(
                tint
            )
    }

    private fun equalButtonsRow(
"""

new_icon_tail = """        resizeButtonStartIcon(
            button = button,
            sizeDp = 20
        )

        button.compoundDrawablePadding =
            dp(6)

        button.compoundDrawableTintList =
            ColorStateList.valueOf(
                tint
            )
    }

    private fun resizeButtonStartIcon(
        button: Button,
        sizeDp: Int
    ) {
        val drawables =
            button.compoundDrawablesRelative

        val start =
            drawables[0]
                ?: return

        val size =
            dp(sizeDp)

        start.setBounds(
            0,
            0,
            size,
            size
        )

        button.setCompoundDrawablesRelative(
            start,
            drawables[1],
            drawables[2],
            drawables[3]
        )
    }

    private fun equalButtonsRow(
"""

ensure_replace(
    MAIN,
    old_icon_tail,
    new_icon_tail,
    "Home vector icon sizing helper"
)

old_pair = """            addView(
                second,
                LinearLayout.LayoutParams(
                    0,
                    dp(70),
                    1f
                ).apply {
                    marginStart = dp(8)
                }
            )
"""

new_pair = """            addView(
                second,
                LinearLayout.LayoutParams(
                    0,
                    dp(70),
                    1f
                ).apply {
                    marginStart = dp(6)
                }
            )
"""

ensure_replace(
    MAIN,
    old_pair,
    new_pair,
    "workflow pair gap"
)

# ------------------------------------------------------------
# Release status
# ------------------------------------------------------------

ensure_replace(
    "RELEASE_TEST_STATUS.md",
    '| v1.4.22 | **NOT TESTED YET** | Visual Structure Polish: vector icons, compact logo header, calmer contour strokes, concept-style current-playlist card and non-solid READY states. |',
    '| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** | Home launched with vector icons, compact logo, calmer contours and current-playlist card; `Історія` wrapped and top labels were tight. |\n'
    '| v1.4.23 | **NOT TESTED YET** | Button Fit + Home Polish: single-line utility actions, smaller vector icons and tighter adaptive workflow-button typography. |',
    "release test status"
)

# ------------------------------------------------------------
# Project status
# ------------------------------------------------------------

ensure_replace(
    "PROJECT_STATUS.txt",
    "Version: 1.4.22\nVersion code: 56",
    "Version: 1.4.23\nVersion code: 57",
    "project version"
)

ensure_replace(
    "PROJECT_STATUS.txt",
    "v1.4.22 NOT TESTED YET",
    "v1.4.22 PARTIALLY PHONE-TESTED — HOME UI FIT ISSUE\n"
    "v1.4.23 NOT TESTED YET",
    "project release status"
)

project = read("PROJECT_STATUS.txt")

if "v1.4.23 focus:" not in project:
    anchor = "\nKnown:\n"

    if anchor not in project:
        raise SystemExit(
            "STOP: PROJECT_STATUS Known anchor missing"
        )

    focus = """
v1.4.23 focus:
- compact utility actions must stay on one line
- shrink compact icons from 20dp to 17dp
- shrink utility adaptive text range to 8–11sp
- workflow actions use 20dp icons and 9–13sp adaptive text
- reduce icon/text and horizontal button padding
- preserve current theme/state behavior and app functionality
"""

    project = project.replace(
        anchor,
        focus + anchor,
        1
    )

    write(
        "PROJECT_STATUS.txt",
        project
    )

    print("PATCH: project v1.4.23 focus")
else:
    print("SKIP: project v1.4.23 focus")

# ------------------------------------------------------------
# Backlog
# ------------------------------------------------------------

backlog = read("BACKLOG.md")

if "## Current\nv1.4.23 — Button Fit + Home Polish" not in backlog:
    old = "## Current\nv1.4.22 — Visual Structure Polish"

    if old not in backlog:
        raise SystemExit(
            "STOP: BACKLOG current v1.4.22 anchor missing"
        )

    backlog = backlog.replace(
        old,
        "## Current\nv1.4.23 — Button Fit + Home Polish",
        1
    )

start = backlog.find("## v1.4.22\n")

if start < 0:
    raise SystemExit(
        "STOP: BACKLOG v1.4.22 section missing"
    )

end = backlog.find(
    "\n## ",
    start + 1
)

if end < 0:
    end = len(backlog)

section = backlog[start:end]

section = section.replace(
    "- [ ] GitHub build",
    "- [x] GitHub build",
    1
)

section = section.replace(
    "- [ ] phone test: icons render correctly",
    "- [x] phone test: icons render correctly",
    1
)

section = section.replace(
    "- [ ] phone test: current-playlist card",
    "- [x] phone test: current-playlist card",
    1
)

if "phone observation: utility text fit issue" not in section:
    section += (
        "- [x] phone observation: utility text fit issue reproduced "
        "(`Історія` wraps; top labels tight)\n"
    )

backlog = (
    backlog[:start] +
    section +
    backlog[end:]
)

if "## v1.4.23\n" not in backlog:
    marker = "## Next\n"

    if marker not in backlog:
        raise SystemExit(
            "STOP: BACKLOG Next anchor missing"
        )

    section23 = """## v1.4.23
- [x] force compact utility actions to one line
- [x] shrink compact vector icons to 17dp
- [x] reduce utility icon/text gap and horizontal padding
- [x] use 8–11sp adaptive utility text
- [x] shrink normal Home icons to 20dp
- [x] use 9–13sp adaptive workflow text
- [x] reduce workflow icon/text gap and side padding
- [x] record v1.4.22 real-phone fit issue evidence
- [x] add static button-fit audit
- [ ] GitHub build
- [ ] phone test: utility row all one line
- [ ] phone test: top four actions fit
- [ ] phone test: Neon / Blue / Green geometry
- [ ] navigation smoke: Import + Review

"""

    backlog = backlog.replace(
        marker,
        section23 + marker,
        1
    )

backlog = backlog.replace(
    "## Next\nPhone-test the v1.4.20 UI fix. After that, continue account-library work only if useful:",
    "## Next\nPhone-test v1.4.23 button fit. After the Home geometry is stable, continue the visual/theme rollout and account-library work only if useful:",
    1
)

write(
    "BACKLOG.md",
    backlog
)

print("PATCH: BACKLOG.md")

# ------------------------------------------------------------
# Changelog
# ------------------------------------------------------------

changelog = read("CHANGELOG.md")

entry = """## v1.4.23
- Button Fit + Home Polish based on real-phone v1.4.22 evidence.
- Forced Home utility actions to a single line.
- Reduced compact utility icon size to 17dp and adaptive text to 8–11sp.
- Reduced compact button horizontal padding and icon/text gap.
- Reduced normal Home action icons to 20dp.
- Tightened workflow-button typography to 9–13sp with reduced side padding.
- Preserved existing theme/state semantics and functional flows.
- versionCode 57 / versionName 1.4.23.
- v1.4.23 = NOT PHONE-TESTED YET.

"""

if entry not in changelog:
    anchor = "# Журнал змін (Changelog)\n\n"

    if anchor not in changelog:
        raise SystemExit(
            "STOP: CHANGELOG heading missing"
        )

    changelog = changelog.replace(
        anchor,
        anchor + entry,
        1
    )

    write(
        "CHANGELOG.md",
        changelog
    )

    print("PATCH: CHANGELOG.md")
else:
    print("SKIP: CHANGELOG v1.4.23")

# ------------------------------------------------------------
# QA status guard
# ------------------------------------------------------------

ensure_replace(
    "scripts/qa-plan-audit.sh",
    """grep -Fq '| v1.4.22 | **NOT TESTED YET** |' "$STATUS" \\
  || fail "v1.4.22 must start NOT TESTED YET" """.rstrip(),
    """grep -Fq '| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** |' "$STATUS" \\
  || fail "v1.4.22 UI-fit phone status missing"
grep -Fq '| v1.4.23 | **NOT TESTED YET** |' "$STATUS" \\
  || fail "v1.4.23 must start NOT TESTED YET" """.rstrip(),
    "qa-plan v1.4.23 guard"
)

# ------------------------------------------------------------
# Release preflight
# ------------------------------------------------------------

PF = "scripts/release-preflight.sh"

ensure_replace(
    PF,
    'check_file "docs/v.1.4.22/RELEASE.md"',
    'check_file "docs/v.1.4.23/RELEASE.md"',
    "preflight release doc"
)

ensure_replace(
    PF,
    'check_file "docs/v.1.4.22/REGRESSION_CHECKLIST.md"',
    'check_file "docs/v.1.4.23/REGRESSION_CHECKLIST.md"',
    "preflight regression doc"
)

ensure_replace(
    PF,
    "bash scripts/v1422-visual-structure-audit.sh",
    "bash scripts/v1423-button-fit-audit.sh",
    "active v1.4.23 audit"
)

ensure_replace(
    PF,
    "grep -q 'versionCode = 56' app/build.gradle.kts \\\n"
    '  || fail "Expected versionCode = 56"',
    "grep -q 'versionCode = 57' app/build.gradle.kts \\\n"
    '  || fail "Expected versionCode = 57"',
    "preflight versionCode"
)

ensure_replace(
    PF,
    'grep -q \'versionName = "1.4.22"\' app/build.gradle.kts \\\n'
    '  || fail \'Expected versionName = "1.4.22"\'',
    'grep -q \'versionName = "1.4.23"\' app/build.gradle.kts \\\n'
    '  || fail \'Expected versionName = "1.4.23"\'',
    "preflight versionName"
)

ensure_replace(
    PF,
    """grep -Fq '| v1.4.22 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.22 must start NOT TESTED YET" """.rstrip(),
    """grep -Fq '| v1.4.22 | **PARTIALLY PHONE-TESTED — UI FIT ISSUE FOUND** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.22 UI-fit phone status missing"
grep -Fq '| v1.4.23 | **NOT TESTED YET** |' RELEASE_TEST_STATUS.md \\
  || fail "v1.4.23 must start NOT TESTED YET" """.rstrip(),
    "preflight release-status guards"
)

preflight = read(PF)
new_line = 'check_file "scripts/v1423-button-fit-audit.sh"\n'

if new_line not in preflight:
    anchor = 'check_file "scripts/v1422-visual-structure-audit.sh"\n'

    if anchor not in preflight:
        raise SystemExit(
            "STOP: preflight v1422 audit check_file anchor missing"
        )

    preflight = preflight.replace(
        anchor,
        anchor + new_line,
        1
    )

    write(
        PF,
        preflight
    )

    print("PATCH: preflight v1423 audit file guard")
else:
    print("SKIP: preflight v1423 audit file guard")

print()
print("PASS: v1.4.23 interrupted apply recovered")

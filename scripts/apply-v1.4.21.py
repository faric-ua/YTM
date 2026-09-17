#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

def read(rel): return (ROOT / rel).read_text(encoding='utf-8')
def write(rel, text): (ROOT / rel).write_text(text, encoding='utf-8')

def rep(rel, old, new, count=1):
    text = read(rel)
    if new in text:
        print(f'SKIP: {rel}')
        return
    n = text.count(old)
    if n < count:
        raise SystemExit(f'STOP: anchor missing in {rel}: expected >= {count}, found {n}\n{old[:500]}')
    write(rel, text.replace(old, new, count))
    print(f'PATCH: {rel}')

# version
rep('app/build.gradle.kts', '        versionCode = 54\n        versionName = "1.4.20"', '        versionCode = 55\n        versionName = "1.4.21"')

M='app/src/main/java/com/saney/ytmimporter/MainActivity.kt'
rep(M, 'import com.saney.ytmimporter.ui.TrackAdapter\nimport com.saney.ytmimporter.ui.UiChrome\n', 'import com.saney.ytmimporter.ui.AppThemeManager\nimport com.saney.ytmimporter.ui.TrackAdapter\nimport com.saney.ytmimporter.ui.UiChrome\n')
rep(M, '        super.onCreate(savedInstanceState)\n        val searchCache =', '        super.onCreate(savedInstanceState)\n        AppThemeManager.applyWindow(this)\n        val searchCache =')
text = read(M)
clean_old = '''    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 16, 19))
        }'''
partial_old = '''    private fun buildUi() {
        val palette = AppThemeManager.palette(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 16, 19))
        }'''
fixed_new = '''    private fun buildUi() {
        val palette = AppThemeManager.palette(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(palette.background)
        }'''
if fixed_new in text:
    print(f'SKIP: {M} buildUi')
elif partial_old in text:
    write(M, text.replace(partial_old, fixed_new, 1))
    print(f'PATCH: {M} buildUi partial recovery')
elif clean_old in text:
    write(M, text.replace(clean_old, fixed_new, 1))
    print(f'PATCH: {M} buildUi')
else:
    raise SystemExit('STOP: MainActivity buildUi anchor missing')
rep(M,
'''        listView = ListView(this).apply {
            divider = null
            dividerHeight = dp(1)
            setBackgroundColor(Color.rgb(15, 16, 19))
            clipToPadding = false''',
'''        listView = ListView(this).apply {
            divider = null
            dividerHeight = dp(1)
            setBackgroundColor(palette.background)
            clipToPadding = false''')
rep(M,
'''                background =
                    roundedBackground(
                        color = Color.rgb(31, 33, 39),
                        radiusDp = 18,
                        strokeColor = Color.rgb(55, 58, 66)
                    )''',
'''                background =
                    AppThemeManager.surfaceDrawable(
                        context = this@MainActivity,
                        fill = palette.surfaceAlt,
                        radiusDp = 18,
                        accentStroke = false
                    )''')
rep(M,
'''            background =
                roundedBackground(
                    color = Color.rgb(25, 27, 32),
                    radiusDp = 16,
                    strokeColor = Color.rgb(48, 51, 59)
                )''',
'''            background =
                AppThemeManager.surfaceDrawable(
                    context = this@MainActivity,
                    fill = palette.surface,
                    radiusDp = 16,
                    accentStroke = true
                )''')
for old,new in [
('button("1. Імпорт")','button("⇩  1. Імпорт")'),
('button("2. Google / YTM")','button("⛓  2. Google / YTM")'),
('primaryButton("3. Знайти / перевірити")','primaryButton("⌕  3. Знайти / перевірити")'),
('primaryButton("4. Створити / додати")','primaryButton("☷  4. Створити / додати")'),
('compactButton("Історія")','compactButton("◷ Історія")'),
('compactButton("Черга")','compactButton("≡ Черга")'),
('compactButton("Квота")','compactButton("▥ Квота")'),
('compactButton("Ще")','compactButton("••• Ще")')]: rep(M,old,new)
rep(M,
'''            background =
                roundedBackground(
                    color = Color.rgb(34, 36, 42),
                    radiusDp = 12,
                    strokeColor = Color.rgb(58, 61, 70)
                )''',
'''            background =
                AppThemeManager.neutralButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )''')
rep(M,
'''            background =
                roundedBackground(
                    color = Color.rgb(196, 0, 42),
                    radiusDp = 12
                )''',
'''            background =
                AppThemeManager.accentButtonDrawable(
                    context = this@MainActivity,
                    radiusDp = 12
                )''')
rep(M,
'''        val color =
            when (state) {
                StepState.READY ->
                    Color.rgb(31, 122, 77)

                StepState.ATTENTION ->
                    Color.rgb(157, 105, 15)

                StepState.REQUIRED ->
                    Color.rgb(176, 0, 32)
            }

        button.background =
            roundedBackground(
                color = color,
                radiusDp = 12
            )''',
'''        val palette =
            AppThemeManager.palette(this)

        val color =
            when (state) {
                StepState.READY -> palette.successFill
                StepState.ATTENTION -> palette.warningFill
                StepState.REQUIRED -> palette.accentFill
            }

        button.background =
            AppThemeManager.stateButtonDrawable(
                context = this,
                fillColor = color,
                radiusDp = 12
            )''')
rep(M,
'''            actions = listOf(
                UiChrome.MenuAction(
                    "Поточний проект — review / save / share"
                ) {''',
'''            actions = listOf(
                UiChrome.MenuAction(
                    "🎨 Тема — Neon / Blue / Green"
                ) { showThemePicker() },
                UiChrome.MenuAction(
                    "Поточний проект — review / save / share"
                ) {''')
rep(M, '    private fun showServiceTools() {\n', '''    private fun showThemePicker() {
        val active = AppThemeManager.currentStyle(this)
        UiChrome.showMenuDialog(
            activity = this,
            title = "Тема оформлення",
            subtitle = "Один інтерфейс — три палітри. Тема зберігається на пристрої.",
            actions = AppThemeManager.ThemeStyle.values().map { style ->
                UiChrome.MenuAction(
                    label = (if (style == active) "✓ " else "") + style.marker + "  " + style.label,
                    onClick = {
                        if (style != active) {
                            AppThemeManager.setStyle(this, style)
                            recreate()
                        }
                    }
                )
            }
        )
    }

    private fun showServiceTools() {
''')
text=read(M)
text=text.replace('"2. Google / YTM ✓"','"⛓  2. Google / YTM ✓"').replace('"2. Google / YTM …"','"⛓  2. Google / YTM …"').replace('"2. Google / YTM"','"⛓  2. Google / YTM"')
write(M,text)

I='app/src/main/java/com/saney/ytmimporter/ImportActivity.kt'
rep(I,'import com.saney.ytmimporter.ui.UiChrome\n','import com.saney.ytmimporter.ui.AppThemeManager\nimport com.saney.ytmimporter.ui.UiChrome\n')
rep(I,'''        super.onCreate(
            savedInstanceState
        )

        currentPlaylistStore =''','''        super.onCreate(
            savedInstanceState
        )

        AppThemeManager.applyWindow(this)

        currentPlaylistStore =''')
rep(I,'''    private fun buildUi() {
        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    BACKGROUND
                )
            }''','''    private fun buildUi() {
        val palette = AppThemeManager.palette(this)

        val root =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL
                setBackgroundColor(
                    palette.background
                )
            }''')
rep(I,'''            background =
                roundedBackground(
                    color = SURFACE,
                    radiusDp = 14,
                    strokeColor = BORDER
                )
            layoutParams =''','''            background =
                AppThemeManager.surfaceDrawable(
                    context = this@ImportActivity,
                    fill = AppThemeManager.palette(this@ImportActivity).surface,
                    radiusDp = 14,
                    accentStroke = true
                )
            layoutParams =''')
rep(I,'''            background =
                roundedBackground(
                    color =
                        if (primary) {
                            Color.rgb(
                                196,
                                0,
                                42
                            )
                        } else {
                            Color.rgb(
                                37,
                                39,
                                46
                            )
                        },
                    radiusDp = 11,
                    strokeColor =
                        if (primary) {
                            null
                        } else {
                            Color.rgb(
                                63,
                                66,
                                76
                            )
                        }
                )''','''            background =
                if (primary) {
                    AppThemeManager.accentButtonDrawable(this@ImportActivity, 11)
                } else {
                    AppThemeManager.neutralButtonDrawable(this@ImportActivity, 11)
                }''')

R='app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt'
rep(R,'import com.saney.ytmimporter.ui.UiChrome\n','import com.saney.ytmimporter.ui.AppThemeManager\nimport com.saney.ytmimporter.ui.UiChrome\n')
rep(R,'        super.onCreate(savedInstanceState)\n\n        currentPlaylistStore =','        super.onCreate(savedInstanceState)\n        AppThemeManager.applyWindow(this)\n\n        currentPlaylistStore =')
text=read(R)
text,n=re.subn(r'setBackgroundColor\(\s*BACKGROUND\s*\)','setBackgroundColor(AppThemeManager.palette(this@ReviewActivity).background)',text)
if n < 2: raise SystemExit(f'STOP: Review background anchors found {n}')
write(R,text); print(f'PATCH: {R} backgrounds')

A='app/src/main/java/com/saney/ytmimporter/ui/TrackAdapter.kt'
rep(A,'    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {\n        val holder: Holder','    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {\n        val palette = AppThemeManager.palette(context)\n\n        val holder: Holder')
rep(A,'''                        setColor(
                            Color.rgb(
                                25,
                                27,
                                32
                            )
                        )
                        setStroke(
                            dp(1),
                            Color.rgb(
                                44,
                                47,
                                54
                            )
                        )''','''                        setColor(palette.surface)
                        setStroke(dp(1), palette.border)''')
rep(A,'                setTextColor(Color.WHITE)\n                textSize = 16f','                setTextColor(palette.text)\n                textSize = 16f')
rep(A,'                setTextColor(Color.rgb(170, 172, 178))\n                textSize = 14f','                setTextColor(palette.muted)\n                textSize = 14f')

rep('RELEASE_TEST_STATUS.md','| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** | Import account-action layout passed; bulk-export folder picker still opens; BUG-003 in-place update recovery retest passed. |','| v1.4.20 | **PARTIALLY PHONE-TESTED — PASS FOR UI SMOKE** | Import account-action layout passed; bulk-export folder picker still opens; BUG-003 in-place update recovery retest passed. |\n| v1.4.21 | **NOT TESTED YET** | Theme System Wave 1: Neon/Blue/Green palettes and themed Home/Import/Review surfaces. |')
rep('PROJECT_STATUS.txt','Version: 1.4.20\nVersion code: 54','Version: 1.4.21\nVersion code: 55')

# minimal current-release preflight bump
PF='scripts/release-preflight.sh'
for old,new in [
('check_file "docs/v.1.4.20/RELEASE.md"','check_file "docs/v.1.4.21/RELEASE.md"'),
('check_file "docs/v.1.4.20/REGRESSION_CHECKLIST.md"','check_file "docs/v.1.4.21/REGRESSION_CHECKLIST.md"'),
('bash scripts/v1420-import-button-layout-audit.sh\n','bash scripts/v1421-theme-wave1-audit.sh\n'),
("grep -q 'versionCode = 54' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 54\"","grep -q 'versionCode = 55' app/build.gradle.kts \\\n  || fail \"Expected versionCode = 55\""),
("grep -q 'versionName = \"1.4.20\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.20\"'","grep -q 'versionName = \"1.4.21\"' app/build.gradle.kts \\\n  || fail 'Expected versionName = \"1.4.21\"'")]: rep(PF,old,new)

print('\nPASS: v1.4.21 R2 applied')

#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

EXPECTED_BASE_SHA = "6608a1fce6f557882ec69af95cb5c01e8d43e72a"

@dataclass(frozen=True)
class Op:
    path: str
    label: str
    old: str
    new: str


def fail(message: str) -> None:
    raise SystemExit(f"FAIL: {message}")


def git_head(root: Path) -> str | None:
    if not (root / ".git").exists():
        return None
    try:
        return subprocess.check_output(
            ["git", "rev-parse", "HEAD"],
            cwd=root,
            text=True,
            stderr=subprocess.DEVNULL,
        ).strip()
    except Exception:
        return None


def apply_op(root: Path, op: Op, check_only: bool) -> str:
    path = root / op.path
    if not path.is_file():
        fail(f"missing file for {op.label}: {op.path}")

    text = path.read_text(encoding="utf-8")
    old_count = text.count(op.old)
    new_count = text.count(op.new)
    old_inside_new = op.new.count(op.old)

    if new_count == 1:
        if old_count == old_inside_new:
            return f"SKIP {op.label}"
        fail(
            f"anchor mismatch for {op.label}: old={old_count}, new={new_count}, file={op.path}"
        )

    if new_count != 0 or old_count != 1:
        fail(
            f"anchor mismatch for {op.label}: old={old_count}, new={new_count}, file={op.path}"
        )

    updated = text.replace(op.old, op.new, 1)


    if not check_only:
        path.write_text(updated, encoding="utf-8", newline="\n")

    return f"PASS {op.label}" if check_only else f"APPLY {op.label}"


# FIX1: raw anchor literals preserve Kotlin backslash escapes such as \n.
OPS = [
    Op(
        "app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt",
        "UiChrome showMenuDialog returns Dialog",
        r'''    fun showMenuDialog(
        activity: Activity,
        title: String,
        actions: List<MenuAction>,
        negativeLabel: String = "Закрити",
        subtitle: String? = null,
        onNegative: (() -> Unit)? = null
    ) {''',
        r'''    fun showMenuDialog(
        activity: Activity,
        title: String,
        actions: List<MenuAction>,
        negativeLabel: String = "Закрити",
        subtitle: String? = null,
        onNegative: (() -> Unit)? = null
    ): Dialog {''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ui/UiChrome.kt",
        "UiChrome returns shown menu Dialog",
        r'''        showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun showRecordDialog(''',
        r'''        return showCustomDialog(
            activity = activity,
            dialog = dialog,
            card = card
        )
    }

    fun showRecordDialog(''',
    ),

    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector Dialog import",
        r'''import android.app.Activity
import android.content.Intent''',
        r'''import android.app.Activity
import android.app.Dialog
import android.content.Intent''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector Help dialog fields",
        r'''    private lateinit var confirmButton: Button

    private var titleText: String = "Вибір"''',
        r'''    private lateinit var confirmButton: Button

    private var helpDialogOpen = false
    private var helpDialog: Dialog? = null

    private var titleText: String = "Вибір"''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector restores Help state",
        r'''        render()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {''',
        r'''        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

        render()

        if (
            helpDialogOpen &&
            helpMessage.isNotBlank()
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector saves Help state and detaches listener",
        r'''        outState.putStringArrayList(
            STATE_SELECTED_VALUES,
            selectedValues
        )

        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")''',
        r'''        outState.putStringArrayList(
            STATE_SELECTED_VALUES,
            selectedValues
        )

        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )

        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector lifecycle-safe Help window",
        r'''    private fun showHelp() {
        UiChrome.showMessageDialog(
            activity = this,
            title = helpTitle,
            message = helpMessage,
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone = UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )
    }''',
        r'''    private fun showHelp() {
        if (
            helpMessage.isBlank() ||
            helpDialog?.isShowing == true
        ) {
            return
        }

        helpDialogOpen = true

        helpDialog =
            UiChrome.showMessageDialog(
                activity = this,
                title = helpTitle,
                message = helpMessage,
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Зрозуміло",
                            tone = UiChrome.ActionTone.ACCENT
                        ) {}
                    )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    helpDialogOpen = false
                    helpDialog = null
                }
            }
    }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ListSelectorActivity.kt",
        "ListSelector Help state key",
        r'''        private const val STATE_SELECTED_VALUES =
            "selector_state_selected_values"

        private const val EXTRA_TITLE =''',
        r'''        private const val STATE_SELECTED_VALUES =
            "selector_state_selected_values"

        private const val STATE_HELP_DIALOG_OPEN =
            "selector_state_help_dialog_open"

        private const val EXTRA_TITLE =''',
    ),

    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser Dialog import",
        r'''import android.app.Activity
import android.content.Intent''',
        r'''import android.app.Activity
import android.app.Dialog
import android.content.Intent''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser Help fields",
        r'''    private var refreshAfterSettings =
        false

    override fun onCreate(''',
        r'''    private var refreshAfterSettings =
        false

    private var helpDialogOpen =
        false

    private var helpDialog: Dialog? =
        null

    override fun onCreate(''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser restores Help flag",
        r'''        AppThemeManager.applyWindow(this)

        titleText =''',
        r'''        AppThemeManager.applyWindow(this)

        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

        titleText =''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser reopens Help after render",
        r'''        render()
    }

    override fun onResume() {''',
        r'''        render()

        if (helpDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        super.onDestroy()
    }

    override fun onResume() {''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser lifecycle-safe Help",
        r'''    private fun showHelp() {
        UiChrome.showMessageDialog(
            activity = this,
            title = "Останні файли",
            message =
                "На Android 11+ YTM Importer може напряму читати Download після того, " +
                    "як ви вручну увімкнете спеціальний системний дозвіл «Доступ до всіх файлів».\n\n" +
                    "Після цього файли з Download показуються автоматично й сортуються " +
                    "за часом останньої зміни: найсвіжіші зверху.\n\n" +
                    "«Додати SAF-папку…» лишається додатковим способом підключити іншу папку. " +
                    "Корінь Download Android через SAF не дозволяє — для нього використовується " +
                    "саме All files access.\n\n" +
                    "«Системний вибір файла…» залишає стандартний Android picker як запасний варіант.\n\n" +
                    "Справжній creation time доступний не у всіх файлових системах, " +
                    "тому сортування використовує last modified.",
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )
    }''',
        r'''    private fun showHelp() {
        if (helpDialog?.isShowing == true) {
            return
        }

        helpDialogOpen = true

        helpDialog =
            UiChrome.showMessageDialog(
                activity = this,
                title = "Останні файли",
                message =
                    "На Android 11+ YTM Importer може напряму читати Download після того, " +
                        "як ви вручну увімкнете спеціальний системний дозвіл «Доступ до всіх файлів».\n\n" +
                        "Після цього файли з Download показуються автоматично й сортуються " +
                        "за часом останньої зміни: найсвіжіші зверху.\n\n" +
                        "«Додати SAF-папку…» лишається додатковим способом підключити іншу папку. " +
                        "Корінь Download Android через SAF не дозволяє — для нього використовується " +
                        "саме All files access.\n\n" +
                        "«Системний вибір файла…» залишає стандартний Android picker як запасний варіант.\n\n" +
                        "Справжній creation time доступний не у всіх файлових системах, " +
                        "тому сортування використовує last modified.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Зрозуміло",
                            tone =
                                UiChrome.ActionTone.ACCENT
                        ) {}
                    )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    helpDialogOpen = false
                    helpDialog = null
                }
            }
    }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/RecentFileChooserActivity.kt",
        "RecentFileChooser Help state key",
        r'''        private const val REQUEST_SYSTEM_TREE =
            8801''',
        r'''        private const val STATE_HELP_DIALOG_OPEN =
            "recent_file_chooser_help_dialog_open"

        private const val REQUEST_SYSTEM_TREE =
            8801''',
    ),

    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser Dialog import",
        r'''import android.app.Activity
import android.content.Intent''',
        r'''import android.app.Activity
import android.app.Dialog
import android.content.Intent''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser Help fields",
        r'''    private var mimeType: String = "text/plain"

    override fun onCreate(savedInstanceState: Bundle?) {''',
        r'''    private var mimeType: String = "text/plain"

    private var helpDialogOpen = false
    private var helpDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser restores Help flag",
        r'''        AppThemeManager.applyWindow(this)

        access =''',
        r'''        AppThemeManager.applyWindow(this)

        helpDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_HELP_DIALOG_OPEN,
                    false
                )
                ?: false

        access =''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser reopens Help after render",
        r'''        render()
    }

    @Deprecated("Deprecated in Java")''',
        r'''        render()

        if (helpDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showHelp()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_HELP_DIALOG_OPEN,
            helpDialogOpen
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        helpDialog
            ?.setOnDismissListener(null)
        helpDialog = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser lifecycle-safe Help",
        r'''        UiChrome.showMessageDialog(
            activity = this,
            title = "Що це за список?",
            message =
                "Це папки, до яких ви раніше надали YTM Importer доступ " +
                    "через системний Android picker. Android зберігає ці SAF-дозволи, " +
                    "тому застосунок може повторно використовувати папку без нового " +
                    "переходу в системний файловий провідник.\n\n" +
                    accessDescription +
                    "\n\n«Читання» означає, що застосунок може відкрити дані. " +
                    "«Читання і запис» також дозволяє створювати файли в папці. " +
                    "Якщо потрібної папки немає, скористайтеся кнопкою внизу.",
            actions =
                listOf(
                    UiChrome.DialogAction(
                        label = "Зрозуміло",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {}
                )
        )''',
        r'''        if (helpDialog?.isShowing == true) {
            return
        }

        helpDialogOpen = true

        helpDialog =
            UiChrome.showMessageDialog(
                activity = this,
                title = "Що це за список?",
                message =
                    "Це папки, до яких ви раніше надали YTM Importer доступ " +
                        "через системний Android picker. Android зберігає ці SAF-дозволи, " +
                        "тому застосунок може повторно використовувати папку без нового " +
                        "переходу в системний файловий провідник.\n\n" +
                        accessDescription +
                        "\n\n«Читання» означає, що застосунок може відкрити дані. " +
                        "«Читання і запис» також дозволяє створювати файли в папці. " +
                        "Якщо потрібної папки немає, скористайтеся кнопкою внизу.",
                actions =
                    listOf(
                        UiChrome.DialogAction(
                            label = "Зрозуміло",
                            tone =
                                UiChrome.ActionTone.ACCENT
                        ) {}
                    )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    helpDialogOpen = false
                    helpDialog = null
                }
            }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/StorageChooserActivity.kt",
        "StorageChooser Help state key",
        r'''        private const val REQUEST_SYSTEM_TREE =
            8701''',
        r'''        private const val STATE_HELP_DIALOG_OPEN =
            "storage_chooser_help_dialog_open"

        private const val REQUEST_SYSTEM_TREE =
            8701''',
    ),

    Op(
        "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt",
        "Menu Dialog import",
        r'''import android.app.Activity
import android.content.Intent''',
        r'''import android.app.Activity
import android.app.Dialog
import android.content.Intent''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt",
        "Menu Theme dialog lifecycle state",
        r'''class MenuActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        render()
    }

    private fun render() {''',
        r'''class MenuActivity : Activity() {
    private var themeDialogOpen = false
    private var themeDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)

        themeDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_THEME_DIALOG_OPEN,
                    false
                )
                ?: false

        render()

        if (themeDialogOpen) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showThemePicker()
                }
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        outState.putBoolean(
            STATE_THEME_DIALOG_OPEN,
            themeDialogOpen
        )
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        themeDialog
            ?.setOnDismissListener(null)
        themeDialog = null
        super.onDestroy()
    }

    private fun render() {''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt",
        "Menu lifecycle-safe Theme picker",
        r'''    private fun showThemePicker() {
        val active =
            AppThemeManager.currentStyle(this)

        UiChrome.showMenuDialog(
            activity = this,
            title = "Тема оформлення",
            subtitle =
                "Один інтерфейс — три палітри. Тема зберігається на пристрої.",
            actions =
                AppThemeManager.ThemeStyle
                    .values()
                    .map { style ->
                        UiChrome.MenuAction(
                            label =
                                (
                                    if (style == active) {
                                        "✓ "
                                    } else {
                                        ""
                                    }
                                ) +
                                    style.marker +
                                    "  " +
                                    style.label,
                            onClick = {
                                if (style != active) {
                                    AppThemeManager
                                        .setStyle(
                                            this,
                                            style
                                        )
                                    recreate()
                                }
                            }
                        )
                    }
        )
    }''',
        r'''    private fun showThemePicker() {
        if (themeDialog?.isShowing == true) {
            return
        }

        val active =
            AppThemeManager.currentStyle(this)

        themeDialogOpen = true

        themeDialog =
            UiChrome.showMenuDialog(
                activity = this,
                title = "Тема оформлення",
                subtitle =
                    "Один інтерфейс — три палітри. Тема зберігається на пристрої.",
                actions =
                    AppThemeManager.ThemeStyle
                        .values()
                        .map { style ->
                            UiChrome.MenuAction(
                                label =
                                    (
                                        if (style == active) {
                                            "✓ "
                                        } else {
                                            ""
                                        }
                                    ) +
                                        style.marker +
                                        "  " +
                                        style.label,
                                onClick = {
                                    if (style != active) {
                                        AppThemeManager
                                            .setStyle(
                                                this,
                                                style
                                            )
                                        recreate()
                                    }
                                }
                            )
                        }
            ).also { dialog ->
                dialog.setOnDismissListener {
                    themeDialogOpen = false
                    themeDialog = null
                }
            }
    }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/MenuActivity.kt",
        "Menu Theme state key",
        r'''    companion object {
        const val EXTRA_ACTION =''',
        r'''    companion object {
        private const val STATE_THEME_DIALOG_OPEN =
            "menu_theme_dialog_open"

        const val EXTRA_ACTION =''',
    ),

    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review Dialog import",
        r'''import android.app.Activity
import android.content.ClipData''',
        r'''import android.app.Activity
import android.app.Dialog
import android.content.ClipData''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review Project dialog fields",
        r'''    private val saveProjectFolderRequestCode =
        3302

    override fun onCreate(''',
        r'''    private val saveProjectFolderRequestCode =
        3302

    private var projectDialogOpen =
        false

    private var projectDialog: Dialog? =
        null

    override fun onCreate(''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review restores Project dialog flag",
        r'''        AppThemeManager.applyWindow(this)

        currentPlaylistStore =''',
        r'''        AppThemeManager.applyWindow(this)

        projectDialogOpen =
            savedInstanceState
                ?.getBoolean(
                    STATE_PROJECT_DIALOG_OPEN,
                    false
                )
                ?: false

        currentPlaylistStore =''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review restores parent screen then Project modal",
        r'''        if (focus != null) {
            findTrackByHistoryIndex(
                focus
            )?.let { track ->
                showTrackScreen(track)
                return
            }
        }

        showListScreen()

        if (
            intent.getBooleanExtra(
                EXTRA_OPEN_PROJECT_ACTIONS,
                false
            )
        ) {
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showProjectActions()
                }
            }
        }''',
        r'''        var restoredTrackScreen = false

        if (focus != null) {
            findTrackByHistoryIndex(
                focus
            )?.let { track ->
                showTrackScreen(track)
                restoredTrackScreen = true
            }
        }

        if (!restoredTrackScreen) {
            showListScreen()
        }

        val shouldOpenProjectActions =
            projectDialogOpen ||
                (
                    savedInstanceState == null &&
                        intent.getBooleanExtra(
                            EXTRA_OPEN_PROJECT_ACTIONS,
                            false
                        )
                )

        if (shouldOpenProjectActions) {
            projectDialogOpen = true
            window.decorView.post {
                if (!isFinishing && !isDestroyed) {
                    showProjectActions()
                }
            }
        }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review saves Project modal state",
        r'''        currentTrackHistoryIndex
            ?.let { value ->
                outState.putInt(
                    KEY_TRACK_HISTORY_INDEX,
                    value
                )
            }

        super.onSaveInstanceState(
            outState
        )
    }

    @Deprecated("Deprecated in Java")''',
        r'''        currentTrackHistoryIndex
            ?.let { value ->
                outState.putInt(
                    KEY_TRACK_HISTORY_INDEX,
                    value
                )
            }

        outState.putBoolean(
            STATE_PROJECT_DIALOG_OPEN,
            projectDialogOpen
        )

        super.onSaveInstanceState(
            outState
        )
    }

    override fun onDestroy() {
        projectDialog
            ?.setOnDismissListener(null)
        projectDialog = null
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review lifecycle-safe Current YTM Project modal",
        r'''    private fun showProjectActions() {
        UiChrome.showMenuDialog(
            activity = this,
            title = "Поточний YTM Project",
            subtitle = "Збереження та обмін робочим проектом.",
            actions = listOf(
                UiChrome.MenuAction(
                    "Зберегти YTM Project"
                ) {
                    saveCurrentProject()
                },
                UiChrome.MenuAction(
                    "Поділитися YTM Project"
                ) {
                    shareCurrentProject()
                }
            )
        )
    }''',
        r'''    private fun showProjectActions() {
        if (projectDialog?.isShowing == true) {
            return
        }

        projectDialogOpen = true

        projectDialog =
            UiChrome.showMenuDialog(
                activity = this,
                title = "Поточний YTM Project",
                subtitle = "Збереження та обмін робочим проектом.",
                actions = listOf(
                    UiChrome.MenuAction(
                        "Зберегти YTM Project"
                    ) {
                        saveCurrentProject()
                    },
                    UiChrome.MenuAction(
                        "Поділитися YTM Project"
                    ) {
                        shareCurrentProject()
                    }
                )
            ).also { dialog ->
                dialog.setOnDismissListener {
                    projectDialogOpen = false
                    projectDialog = null
                }
            }
    }''',
    ),
    Op(
        "app/src/main/java/com/saney/ytmimporter/ReviewActivity.kt",
        "Review Project state key",
        r'''        private const val KEY_TRACK_HISTORY_INDEX =
            "review_current_track_history_index"

        private val BACKGROUND =''',
        r'''        private const val KEY_TRACK_HISTORY_INDEX =
            "review_current_track_history_index"

        private const val STATE_PROJECT_DIALOG_OPEN =
            "review_project_dialog_open"

        private val BACKGROUND =''',
    ),
]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="validate anchors without writing")
    parser.add_argument("--root", default=".", help="repository root")
    args = parser.parse_args()

    root = Path(args.root).resolve()

    head = git_head(root)
    if head and os.environ.get("YTM_PATCH_SKIP_HEAD_CHECK") != "1":
        if head != EXPECTED_BASE_SHA:
            fail(
                "unexpected HEAD; expected "
                f"{EXPECTED_BASE_SHA}, got {head}. Start from the audited R2 head."
            )

    results: list[str] = []
    for op in OPS:
        results.append(apply_op(root, op, args.check))

    for line in results:
        print(line)

    mode = "CHECK" if args.check else "APPLY"
    print(f"{mode} PASS: {len(OPS)} guarded lifecycle edits")


if __name__ == "__main__":
    main()

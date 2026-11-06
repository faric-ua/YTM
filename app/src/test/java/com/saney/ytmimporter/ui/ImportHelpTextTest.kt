package com.saney.ytmimporter.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ImportHelpTextTest {
    /** The buttons and fields of the Import screen (batch 17). */
    private val importButtons = listOf(
        "Очистити поточний список",
        "Вибрати плейлист з YTM",
        "Імпорт за URL",
        "Вибрати плейлисти для експорту",
        "Експортувати всі плейлисти в папку",
        "Відкрити backup / manifest.json",
        "Оновити backup (incremental)",
        "Зібрати повний backup з ланцюжка",
        "Вибрати файл",
        "Назва плейлиста (необов'язково)",
        "Імпортувати текст"
    )

    @Test fun everyImportButtonIsExplained() {
        importButtons.forEach { label ->
            assertTrue("«$label» is not in the Import help", label in ImportHelpText.titles)
        }
    }

    @Test fun theButtonsStillExistOnTheImportScreen() {
        val source = listOf("src/main", "app/src/main")
            .map { File(it, "java/com/saney/ytmimporter/ImportActivity.kt") }
            .firstOrNull { it.isFile } ?: return
        val text = source.readText()
        importButtons.forEach { label ->
            assertTrue("«$label» is gone from ImportActivity — update the help", "\"$label\"" in text)
        }
    }

    @Test fun plainWordsAsFaricAsked() {
        val all = ImportHelpText.sections.flatMap { s -> s.entries.map { it.title + " " + it.text } }
            .joinToString(" ")
        // faric 2026-11-05 05:59: «YTM» was taken for the app name — said plainly.
        assertTrue("YouTube Music" in all)
        assertTrue("Read-only (лише читання)" in all)
        assertFalse("videoId" in all)
        assertFalse("локальний робочий список" in all)
        assertTrue(ImportHelpText.sections.all { it.entries.isNotEmpty() })
    }
}

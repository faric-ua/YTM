package com.saney.ytmimporter.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Batch 21: the «?» of «Створити / додати» explains the screen's parts. */
class CreateHelpTextTest {
    private val titles = CreateHelpText.sections.flatMap { s -> s.entries.map { it.title } }

    @Test fun everyPartIsExplained() {
        listOf("Назва плейлиста", "Опис (необов'язково)", "Стрічка описів",
            "Приватний / За посиланням / Публічний", "Куди стає нова пісня", "Квота YouTube"
        ).forEach { assertTrue("«$it» missing", it in titles) }
        assertTrue(titles.any { it.contains("Створено через YTM Importer") })
    }

    @Test fun plainWords() {
        val all = CreateHelpText.sections.flatMap { s -> s.entries.map { it.text } }.joinToString(" ")
        assertFalse(all.contains("videoId"))
        assertFalse(all.contains("API"))
    }
}

package com.saney.ytmimporter.util

import com.saney.ytmimporter.youtube.YouTubeApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMessagesReasonTest {
    @Test fun youTubeReasonIsShownAfterTheMessage() {
        // faric 2026-11-05 23:29: «Invaders Must Die» — only «YouTube відхилив запит».
        val message = ErrorMessages.userMessage(
            YouTubeApiException(400, "invalidResourceId", "bad"))
        assertTrue(message.startsWith("YouTube відхилив запит."))
        assertTrue(message.endsWith(" (причина YouTube: invalidResourceId)"))
        assertEquals("", ErrorMessages.reasonNote(null))
        assertEquals("", ErrorMessages.reasonNote("  "))
        assertEquals("", ErrorMessages.reasonNote("x".repeat(61)))
    }
}

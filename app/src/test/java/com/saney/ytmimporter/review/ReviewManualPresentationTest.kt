package com.saney.ytmimporter.review

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewManualPresentationTest {
    @Test fun manualMatchHasDistinctVisualStatus() {
        assertTrue(ReviewManualPresentation.isManualChoice(true, "Track"))
        assertTrue(ReviewManualPresentation.STATUS_LABEL.contains("Ручний вибір"))
    }
    @Test fun automaticMatchKeepsOrdinaryStatus() {
        assertFalse(ReviewManualPresentation.isManualChoice(false, "Track"))
    }
    @Test fun manuallySkippedWithoutSelectedTitleMustNotLookSuccessful() {
        assertFalse(ReviewManualPresentation.isManualChoice(true, null))
        assertFalse(ReviewManualPresentation.isManualChoice(true, " "))
    }
}

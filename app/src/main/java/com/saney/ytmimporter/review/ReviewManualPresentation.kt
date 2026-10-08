package com.saney.ytmimporter.review

/** UI-only semantics: manual SKIPPED is not a successful manual match. */
object ReviewManualPresentation {
    const val STATUS_LABEL = "✓ Ручний вибір"

    fun isManualChoice(
        manuallySelected: Boolean,
        selectedTitle: String?
    ): Boolean = manuallySelected && !selectedTitle.isNullOrBlank()
}

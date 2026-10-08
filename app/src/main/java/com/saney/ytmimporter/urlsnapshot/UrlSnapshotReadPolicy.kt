package com.saney.ytmimporter.urlsnapshot

/** Presentation-only enablement; existing parser owns URL validity. */
object UrlSnapshotReadPolicy {
    fun canRead(
        rawUrl: String,
        running: Boolean
    ): Boolean =
        !running && rawUrl.isNotBlank()
}

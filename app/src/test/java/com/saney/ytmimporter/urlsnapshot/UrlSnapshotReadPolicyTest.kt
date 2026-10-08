package com.saney.ytmimporter.urlsnapshot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSnapshotReadPolicyTest {
    @Test
    fun blankAndWhitespaceOnlyDisableRead() {
        assertFalse(UrlSnapshotReadPolicy.canRead("", false))
        assertFalse(UrlSnapshotReadPolicy.canRead(" \t\n ", false))
        assertFalse(UrlSnapshotReadPolicy.canRead("\u2003", false))
    }

    @Test
    fun nonblankTextEnablesRead() {
        assertTrue(UrlSnapshotReadPolicy.canRead("https://music.youtube.com/playlist?list=PLabc", false))
        assertTrue(UrlSnapshotReadPolicy.canRead("  x  ", false))
    }

    @Test
    fun runningDisablesEvenNonblankRead() {
        assertFalse(UrlSnapshotReadPolicy.canRead("", true))
        assertFalse(UrlSnapshotReadPolicy.canRead("https://www.youtube.com/playlist?list=PLabc", true))
    }
}

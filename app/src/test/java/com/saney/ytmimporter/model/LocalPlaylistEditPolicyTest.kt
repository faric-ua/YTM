package com.saney.ytmimporter.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalPlaylistEditPolicyTest {
    @Test
    fun normalizeName_trimsOuterWhitespace() {
        assertEquals(
            "My Playlist",
            LocalPlaylistEditPolicy
                .normalizeName(
                    "  My Playlist  "
                )
        )
    }

    @Test
    fun normalizeName_preservesInternalWhitespace() {
        assertEquals(
            "My   Playlist",
            LocalPlaylistEditPolicy
                .normalizeName(
                    "My   Playlist"
                )
        )
    }

    @Test
    fun normalizeName_rejectsBlankValue() {
        assertNull(
            LocalPlaylistEditPolicy
                .normalizeName(
                    "   \n\t  "
                )
        )
    }
}

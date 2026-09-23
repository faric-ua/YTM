package com.saney.ytmimporter.urlsnapshot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSnapshotSourceParserTest {
    @Test
    fun youtubePlaylist_isConcreteAndCanonical() {
        val source = supported("https://www.youtube.com/playlist?list=PLabc_123-XYZ&si=tracking")
        assertEquals(UrlSnapshotSourceKind.CONCRETE_PLAYLIST, source.kind)
        assertEquals(UrlSnapshotSurface.YOUTUBE, source.surface)
        assertEquals("PLabc_123-XYZ", source.playlistId)
        assertEquals("https://www.youtube.com/playlist?list=PLabc_123-XYZ", source.canonicalUrl)
        assertNull(source.contextVideoId)
    }

    @Test
    fun musicPlaylist_isConcreteAndKeepsMusicSurface() {
        val source = supported("music.youtube.com/playlist?list=OLAK5uy_example123")
        assertEquals(UrlSnapshotSourceKind.CONCRETE_PLAYLIST, source.kind)
        assertEquals(UrlSnapshotSurface.YOUTUBE_MUSIC, source.surface)
        assertEquals("https://music.youtube.com/playlist?list=OLAK5uy_example123", source.canonicalUrl)
    }

    @Test
    fun recordedDevelopmentMix_isDynamicCandidate() {
        val source = supported(
            "https://music.youtube.com/playlist?list=RDREDRRxBLCgTn4p2e5sfWmEpQ&playnext=1&si=fCmcHgsLZstGHeXj"
        )
        assertEquals(UrlSnapshotSourceKind.DYNAMIC_MIX, source.kind)
        assertEquals("RDREDRRxBLCgTn4p2e5sfWmEpQ", source.playlistId)
        assertEquals(
            "https://music.youtube.com/playlist?list=RDREDRRxBLCgTn4p2e5sfWmEpQ",
            source.canonicalUrl
        )
    }

    @Test
    fun watchWithinPlaylist_preservesExactContextVideoId() {
        val source = supported(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLabc_123"
        )
        assertEquals("dQw4w9WgXcQ", source.contextVideoId)
        assertEquals("PLabc_123", source.playlistId)
    }

    @Test
    fun musicWatchWithinMix_isDynamicCandidate() {
        val source = supported(
            "https://music.youtube.com/watch?v=dQw4w9WgXcQ&list=RDAMVMdQw4w9WgXcQ"
        )
        assertEquals(UrlSnapshotSourceKind.DYNAMIC_MIX, source.kind)
        assertEquals(UrlSnapshotSurface.YOUTUBE_MUSIC, source.surface)
    }

    @Test
    fun shortLinkWithList_isAcceptedAsPlaylistContext() {
        val source = supported("https://youtu.be/dQw4w9WgXcQ?list=PLabc_123")
        assertEquals("dQw4w9WgXcQ", source.contextVideoId)
        assertEquals("https://www.youtube.com/playlist?list=PLabc_123", source.canonicalUrl)
    }

    @Test
    fun mobilePlaylist_isAccepted() {
        val source = supported("https://m.youtube.com/playlist?list=PLmobile_123")
        assertEquals(UrlSnapshotSurface.YOUTUBE, source.surface)
        assertEquals("PLmobile_123", source.playlistId)
    }

    @Test
    fun embedVideoSeries_isAccepted() {
        val source = supported("https://www.youtube.com/embed/videoseries?list=PLembed_123")
        assertEquals("PLembed_123", source.playlistId)
    }

    @Test
    fun directVideoWithoutList_isRejected() {
        assertUnsupported(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            UrlSnapshotParseError.MISSING_PLAYLIST_ID
        )
    }

    @Test
    fun unsupportedHostIsRejectedWithoutSuffixTrick() {
        assertUnsupported(
            "https://youtube.com.evil.example/playlist?list=PLabc",
            UrlSnapshotParseError.UNSUPPORTED_HOST
        )
    }

    @Test
    fun unsupportedPathIsRejected() {
        assertUnsupported(
            "https://www.youtube.com/results?list=PLabc",
            UrlSnapshotParseError.UNSUPPORTED_PATH
        )
    }

    @Test
    fun unsupportedSchemeIsRejected() {
        assertUnsupported(
            "ftp://www.youtube.com/playlist?list=PLabc",
            UrlSnapshotParseError.UNSUPPORTED_SCHEME
        )
    }

    @Test
    fun malformedUrlIsRejected() {
        assertUnsupported("https://[bad", UrlSnapshotParseError.MALFORMED_URL)
    }

    @Test
    fun blankInputIsRejected() {
        assertUnsupported("   ", UrlSnapshotParseError.BLANK_INPUT)
    }

    @Test
    fun differentDuplicateListValuesAreRejected() {
        assertUnsupported(
            "https://www.youtube.com/playlist?list=PLone&list=PLtwo",
            UrlSnapshotParseError.AMBIGUOUS_PLAYLIST_ID
        )
    }

    @Test
    fun identicalDuplicateListValuesAreStable() {
        val source = supported("https://www.youtube.com/playlist?list=PLsame&list=PLsame")
        assertEquals("PLsame", source.playlistId)
    }

    @Test
    fun invalidPlaylistCharactersAreRejected() {
        assertUnsupported(
            "https://www.youtube.com/playlist?list=PLbad%20id",
            UrlSnapshotParseError.INVALID_PLAYLIST_ID
        )
    }

    @Test
    fun httpInputCanonicalizesToHttps() {
        val source = supported("http://youtube.com/playlist?list=PLabc")
        assertEquals("https://www.youtube.com/playlist?list=PLabc", source.canonicalUrl)
    }

    private fun supported(value: String): UrlSnapshotSource {
        val result = UrlSnapshotSourceParser.parse(value)
        assertTrue("Expected Supported for $value but got $result", result is UrlSnapshotParseResult.Supported)
        return (result as UrlSnapshotParseResult.Supported).source
    }

    private fun assertUnsupported(value: String, expected: UrlSnapshotParseError) {
        val result = UrlSnapshotSourceParser.parse(value)
        assertTrue("Expected Unsupported for $value but got $result", result is UrlSnapshotParseResult.Unsupported)
        assertEquals(expected, (result as UrlSnapshotParseResult.Unsupported).error)
    }
}

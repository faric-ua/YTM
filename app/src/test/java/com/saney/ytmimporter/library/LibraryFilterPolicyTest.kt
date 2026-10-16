package com.saney.ytmimporter.library

import com.saney.ytmimporter.library.LibraryFilterPolicy.Privacy
import com.saney.ytmimporter.library.LibraryFilterPolicy.Source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterPolicyTest {
    @Test fun sourceFollowsToolbarFlags() {
        assertEquals(Source.ALL, LibraryFilterPolicy.source(showAll = true, onlineTab = true))
        assertEquals(Source.ALL, LibraryFilterPolicy.source(showAll = true, onlineTab = false))
        assertEquals(Source.YOUTUBE, LibraryFilterPolicy.source(showAll = false, onlineTab = true))
        assertEquals(Source.PHONE, LibraryFilterPolicy.source(showAll = false, onlineTab = false))
    }

    @Test fun privacyOfferedOnlyForYouTubeOnly() {
        assertTrue(LibraryFilterPolicy.privacyAvailable(Source.YOUTUBE))
        assertFalse(LibraryFilterPolicy.privacyAvailable(Source.ALL))
        assertFalse(LibraryFilterPolicy.privacyAvailable(Source.PHONE))
    }

    @Test fun leavingYouTubeOnlyResetsPrivacy() {
        assertEquals(Privacy.ALL,
            LibraryFilterPolicy.privacyAfterSourceChange(Source.ALL, Privacy.PRIVATE))
        assertEquals(Privacy.ALL,
            LibraryFilterPolicy.privacyAfterSourceChange(Source.PHONE, Privacy.PUBLIC))
        assertEquals(Privacy.UNLISTED,
            LibraryFilterPolicy.privacyAfterSourceChange(Source.YOUTUBE, Privacy.UNLISTED))
    }

    @Test fun staleSavedPrivacyIsNotAppliedOutsideYouTubeOnly() {
        // e.g. restored from an old saved state written before this rule
        assertEquals(Privacy.ALL,
            LibraryFilterPolicy.effectivePrivacy(Source.ALL, Privacy.PRIVATE))
        assertEquals(Privacy.PRIVATE,
            LibraryFilterPolicy.effectivePrivacy(Source.YOUTUBE, Privacy.PRIVATE))
    }

    @Test fun googleRestoreOnlyWhenNeededAndNeverLoops() {
        fun restore(
            showsRemote: Boolean = true,
            hasToken: Boolean = false,
            prior: Boolean = true,
            restoring: Boolean = false,
            needsUser: Boolean = false
        ) = LibraryFilterPolicy.shouldRestoreGoogle(
            showsRemote, hasToken, prior, restoring, needsUser)

        assertTrue(restore())
        assertFalse("phone-only view needs no Google", restore(showsRemote = false))
        assertFalse("token already there", restore(hasToken = true))
        assertFalse("never authorized on this device", restore(prior = false))
        assertFalse("already running", restore(restoring = true))
        assertFalse("Google wants the user: no prompt loop", restore(needsUser = true))
    }
}

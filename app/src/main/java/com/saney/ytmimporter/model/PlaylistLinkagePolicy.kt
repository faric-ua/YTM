package com.saney.ytmimporter.model

enum class PlaylistLinkageState {
    LOCAL_ONLY,
    LINKED_YTM,
    PENDING_SEARCH,
    PENDING_WRITE
}

object PlaylistLinkagePolicy {
    fun current(
        tracks: List<Track>,
        destinationPlaylistId: String?
    ): PlaylistLinkageState {
        if (
            tracks.any { track ->
                track.status ==
                    TrackStatus.WAITING_QUOTA ||
                    track.status ==
                    TrackStatus.SEARCHING ||
                    (
                        track.status ==
                            TrackStatus.NEW &&
                            track.selectedVideoId
                                .isNullOrBlank()
                    )
            }
        ) {
            return PlaylistLinkageState
                .PENDING_SEARCH
        }

        if (
            tracks.any {
                it.status ==
                    TrackStatus.PENDING
            }
        ) {
            return PlaylistLinkageState
                .PENDING_WRITE
        }

        return if (
            destinationPlaylistId
                .isNullOrBlank()
        ) {
            PlaylistLinkageState
                .LOCAL_ONLY
        } else {
            PlaylistLinkageState
                .LINKED_YTM
        }
    }

    fun history(
        entry: HistoryEntry
    ): PlaylistLinkageState {
        if (
            entry.pendingCount > 0 ||
            entry.status in
                setOf(
                    HistoryStatus.RUNNING,
                    HistoryStatus.PENDING_QUOTA,
                    HistoryStatus.PENDING_LIMIT
                )
        ) {
            return PlaylistLinkageState
                .PENDING_WRITE
        }

        return if (
            entry.playlistId
                .isNullOrBlank()
        ) {
            PlaylistLinkageState
                .LOCAL_ONLY
        } else {
            PlaylistLinkageState
                .LINKED_YTM
        }
    }

    fun label(
        state: PlaylistLinkageState
    ): String =
        when (state) {
            PlaylistLinkageState.LOCAL_ONLY ->
                "Лише локально"

            PlaylistLinkageState.LINKED_YTM ->
                "Пов'язано з YTM"

            PlaylistLinkageState.PENDING_SEARCH ->
                "Очікує Search"

            PlaylistLinkageState.PENDING_WRITE ->
                "Очікує запис у YTM"
        }
}

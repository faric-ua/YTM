package com.saney.ytmimporter.history

import com.saney.ytmimporter.model.HistoryEntry
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.storage.RestorablePlaylistSnapshot

data class HistoryRestorePlan(
    val localPlaylistId: String,
    val sourceHistoryId: String,
    val playlist: ImportedPlaylist,
    val sourceLabel: String,
    val destinationPlaylistId: String?,
    val destinationPlaylistTitle: String?,
    val usedDurableSnapshot: Boolean
)

object HistoryRecoveryPolicy {
    fun plan(
        entry: HistoryEntry,
        durableSnapshot:
            RestorablePlaylistSnapshot?,
        generatedLocalPlaylistId: String
    ): HistoryRestorePlan {
        if (
            durableSnapshot != null &&
            !entry.localPlaylistId
                .isNullOrBlank() &&
            durableSnapshot.localPlaylistId ==
                entry.localPlaylistId
        ) {
            return HistoryRestorePlan(
                localPlaylistId =
                    durableSnapshot
                        .localPlaylistId,
                sourceHistoryId =
                    entry.id,
                playlist =
                    durableSnapshot
                        .playlist,
                sourceLabel =
                    durableSnapshot
                        .sourceLabel,
                destinationPlaylistId =
                    durableSnapshot
                        .destinationPlaylistId,
                destinationPlaylistTitle =
                    durableSnapshot
                        .destinationPlaylistTitle,
                usedDurableSnapshot =
                    true
            )
        }

        val localPlaylistId =
            entry.localPlaylistId
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: generatedLocalPlaylistId

        val tracks =
            entry.tracks
                .sortedBy {
                    it.index
                }
                .map {
                        stored ->
                    val hasExactVideo =
                        !stored.videoId
                            .isNullOrBlank()

                    Track(
                        originalTitle =
                            stored.originalTitle,
                        originalArtist =
                            stored.originalArtist,
                        selectedVideoId =
                            stored.videoId
                                .takeIf {
                                    hasExactVideo
                                },
                        selectedTitle =
                            stored.selectedTitle
                                .takeIf {
                                    hasExactVideo
                                },
                        selectedChannel =
                            stored.selectedChannel
                                .takeIf {
                                    hasExactVideo
                                },
                        status =
                            if (
                                hasExactVideo
                            ) {
                                TrackStatus.MATCHED
                            } else {
                                TrackStatus.NEW
                            },
                        candidates =
                            emptyList(),
                        manuallySelected =
                            hasExactVideo &&
                                stored.manuallySelected,
                        error = null,
                        historyIndex =
                            stored.index
                    )
                }
                .toMutableList()

        return HistoryRestorePlan(
            localPlaylistId =
                localPlaylistId,
            sourceHistoryId =
                entry.id,
            playlist =
                ImportedPlaylist(
                    name =
                        entry.playlistName,
                    tracks =
                        tracks
                ),
            sourceLabel =
                entry.sourceLabel,
            destinationPlaylistId =
                entry.playlistId,
            destinationPlaylistTitle =
                entry.playlistName
                    .takeIf {
                        !entry.playlistId
                            .isNullOrBlank()
                    },
            usedDurableSnapshot =
                false
        )
    }
}

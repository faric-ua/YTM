package com.saney.ytmimporter.search

import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.PendingSearchCandidate
import com.saney.ytmimporter.model.PendingSearchSnapshot
import com.saney.ytmimporter.model.PendingSearchTrack
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import java.security.MessageDigest

object SearchRecoveryPolicy {
    fun workspaceKey(
        sourceLabel: String,
        playlist: ImportedPlaylist
    ): String {
        val raw =
            buildString {
                append(sourceLabel)
                append('\u001f')
                append(playlist.name)
                playlist.tracks.forEach { track ->
                    append('\u001e')
                    append(track.originalArtist)
                    append('\u001f')
                    append(track.originalTitle)
                }
            }

        return MessageDigest
            .getInstance("SHA-256")
            .digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte ->
                "%02x".format(byte)
            }
    }

    fun snapshot(
        playlist: ImportedPlaylist
    ): PendingSearchSnapshot =
        PendingSearchSnapshot(
            playlistName = playlist.name,
            tracks =
                playlist.tracks.map { track ->
                    PendingSearchTrack(
                        originalTitle =
                            track.originalTitle,
                        originalArtist =
                            track.originalArtist,
                        selectedVideoId =
                            track.selectedVideoId,
                        selectedTitle =
                            track.selectedTitle,
                        selectedChannel =
                            track.selectedChannel,
                        status =
                            track.status.name,
                        manuallySelected =
                            track.manuallySelected,
                        error =
                            track.error,
                        historyIndex =
                            track.historyIndex,
                        candidates =
                            track.candidates.map { candidate ->
                                PendingSearchCandidate(
                                    videoId =
                                        candidate.videoId,
                                    title =
                                        candidate.title,
                                    channelTitle =
                                        candidate.channelTitle,
                                    score =
                                        candidate.score
                                )
                            }
                    )
                }
        )

    fun restore(
        snapshot: PendingSearchSnapshot
    ): ImportedPlaylist =
        ImportedPlaylist(
            name = snapshot.playlistName,
            tracks =
                snapshot.tracks
                    .map { stored ->
                        Track(
                            originalTitle =
                                stored.originalTitle,
                            originalArtist =
                                stored.originalArtist,
                            selectedVideoId =
                                stored.selectedVideoId,
                            selectedTitle =
                                stored.selectedTitle,
                            selectedChannel =
                                stored.selectedChannel,
                            status =
                                runCatching {
                                    TrackStatus.valueOf(
                                        stored.status
                                    )
                                }.getOrDefault(
                                    TrackStatus.NEW
                                ),
                            candidates =
                                stored.candidates.map { candidate ->
                                    SearchCandidate(
                                        videoId =
                                            candidate.videoId,
                                        title =
                                            candidate.title,
                                        channelTitle =
                                            candidate.channelTitle,
                                        score =
                                            candidate.score
                                    )
                                },
                            manuallySelected =
                                stored.manuallySelected,
                            error =
                                stored.error,
                            historyIndex =
                                stored.historyIndex
                        )
                    }
                    .toMutableList()
        )

    fun waitingCount(
        snapshot: PendingSearchSnapshot
    ): Int =
        snapshot.tracks.count {
            it.status ==
                TrackStatus.WAITING_QUOTA.name
        }
}

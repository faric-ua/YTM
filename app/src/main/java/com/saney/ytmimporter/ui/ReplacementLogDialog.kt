package com.saney.ytmimporter.ui

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.saney.ytmimporter.model.ImportedPlaylist
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus

object ReplacementLogDialog {
    fun show(
        activity: Activity,
        playlist: ImportedPlaylist,
        onDismiss: () -> Unit
    ): Dialog? {
        val problemTracks =
            playlist.tracks
                .filter { track ->
                    track.manuallySelected ||
                        track.status ==
                            TrackStatus.SKIPPED ||
                        track.status ==
                            TrackStatus.DUPLICATE ||
                        track.status ==
                            TrackStatus.MISSING ||
                        track.status ==
                            TrackStatus.PENDING ||
                        track.status ==
                            TrackStatus.FAILED
                }

        if (problemTracks.isEmpty()) {
            Toast.makeText(
                activity,
                "Замін, пропусків або проблемних треків поки немає",
                Toast.LENGTH_LONG
            ).show()
            onDismiss()
            return null
        }

        val shortText =
            buildShortReplacementText(
                problemTracks
            )

        val fullText =
            buildFullReplacementText(
                problemTracks
            )

        return UiChrome.showRecordDialog(
            activity = activity,
            title =
                "Заміни / проблемні треки: " +
                    problemTracks.size,
            subtitle =
                "Кожна позиція показана окремою плиткою.",
            records =
                problemTracks.mapIndexed {
                    index,
                    track ->
                    UiChrome.DialogRecord(
                        title =
                            "${index + 1}. " +
                                "${track.originalArtist} — " +
                                track.originalTitle,
                        detail =
                            replacementRecordLabel(
                                track
                            ),
                        tone =
                            if (
                                track.manuallySelected
                            ) {
                                UiChrome.ActionTone.ACCENT
                            } else {
                                UiChrome.ActionTone.NORMAL
                            }
                    )
                },
            actions =
                listOf(
                    UiChrome.DialogAction(
                        "TikTok список"
                    ) {
                        copyText(
                            activity = activity,
                            label =
                                "YTM Importer TikTok replacements",
                            text =
                                shortText,
                            successMessage =
                                "Короткий список для TikTok скопійовано"
                        )
                    },
                    UiChrome.DialogAction(
                        "Повний текст"
                    ) {
                        copyText(
                            activity = activity,
                            label =
                                "YTM Importer replacement log",
                            text =
                                fullText,
                            successMessage =
                                "Повний журнал скопійовано"
                        )
                    },
                    UiChrome.DialogAction(
                        label = "Закрити",
                        tone =
                            UiChrome.ActionTone.ACCENT
                    ) {}
                ),
            actionLayout =
                UiChrome.DialogActionLayout
                    .VERTICAL_WITH_TEXT_CLOSE
        ).also { dialog ->
            dialog.setOnDismissListener {
                onDismiss()
            }
        }
    }

    private fun replacementRecordLabel(
        track: Track
    ): String =
        when {
            track.manuallySelected &&
                !track.selectedTitle
                    .isNullOrBlank() ->
                buildString {
                    append(
                        "Ручний вибір: "
                    )
                    append(
                        track.selectedTitle
                    )

                    if (
                        !track.selectedChannel
                            .isNullOrBlank()
                    ) {
                        append(" • ")
                        append(
                            track.selectedChannel
                        )
                    }
                }

            track.status ==
                TrackStatus.SKIPPED ->
                "Пропущено"

            track.status ==
                TrackStatus.DUPLICATE ->
                "Дублікат у цільовому плейлисті"

            track.status ==
                TrackStatus.MISSING ->
                "Не знайдено"

            track.status ==
                TrackStatus.PENDING ->
                "Очікує в Pending Queue"

            track.status ==
                TrackStatus.FAILED ->
                track.error
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        "Помилка: $it"
                    }
                    ?: "Помилка"

            else ->
                replacementLabel(
                    track
                )
        }

    private fun replacementLabel(
        track: Track
    ): String =
        when {
            track.status ==
                TrackStatus.SKIPPED ->
                "[пропущено]"

            track.status ==
                TrackStatus.DUPLICATE ->
                "[дублікат — write-запит пропущено]"

            track.status ==
                TrackStatus.MISSING ->
                "[не знайдено]"

            track.status ==
                TrackStatus.PENDING ->
                "[очікує в черзі]"

            track.status ==
                TrackStatus.FAILED &&
                track.selectedTitle
                    .isNullOrBlank() ->
                "[помилка]"

            track.selectedTitle ==
                "Ручне посилання" ->
                "[ручне YouTube/YTM посилання]"

            !track.selectedTitle
                .isNullOrBlank() ->
                track.selectedTitle.orEmpty()

            else ->
                "[без заміни]"
        }

    private fun buildShortReplacementText(
        tracks: List<Track>
    ): String =
        buildString {
            append(
                "Заміни / недоступні треки:\n"
            )

            tracks.forEachIndexed {
                index,
                track ->
                append(index + 1)
                append(". ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append(" → ")
                append(
                    replacementLabel(
                        track
                    )
                )

                if (
                    index !=
                    tracks.lastIndex
                ) {
                    append('\n')
                }
            }
        }

    private fun buildFullReplacementText(
        tracks: List<Track>
    ): String =
        buildString {
            append(
                "YTM Importer — журнал замін\n\n"
            )

            tracks.forEachIndexed {
                index,
                track ->
                append(index + 1)
                append(". Оригінал: ")
                append(track.originalArtist)
                append(" – ")
                append(track.originalTitle)
                append('\n')
                append("   Результат: ")
                append(
                    replacementLabel(
                        track
                    )
                )
                append('\n')

                if (
                    !track.selectedChannel
                        .isNullOrBlank()
                ) {
                    append("   Канал: ")
                    append(
                        track.selectedChannel
                    )
                    append('\n')
                }

                if (
                    !track.selectedVideoId
                        .isNullOrBlank()
                ) {
                    append(
                        "   YTM: https://music.youtube.com/watch?v="
                    )
                    append(
                        track.selectedVideoId
                    )
                    append('\n')
                }

                if (
                    !track.error
                        .isNullOrBlank()
                ) {
                    append(
                        "   Помилка: "
                    )
                    append(
                        track.error
                    )
                    append('\n')
                }

                if (
                    index !=
                    tracks.lastIndex
                ) {
                    append('\n')
                }
            }
        }

    private fun copyText(
        activity: Activity,
        label: String,
        text: String,
        successMessage: String
    ) {
        val clipboard =
            activity.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                label,
                text
            )
        )

        Toast.makeText(
            activity,
            successMessage,
            Toast.LENGTH_LONG
        ).show()
    }
}

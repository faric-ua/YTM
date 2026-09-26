package com.saney.ytmimporter.write

import com.saney.ytmimporter.model.PendingPauseReason
import com.saney.ytmimporter.youtube.YouTubeLimitKind

enum class WritePauseAction {
    CREATE_PLAYLIST,
    ADD_TRACK
}

object WritePausePolicy {
    fun pendingReason(
        kind: YouTubeLimitKind
    ): PendingPauseReason =
        when (kind) {
            YouTubeLimitKind.DAILY_QUOTA ->
                PendingPauseReason.DAILY_QUOTA

            YouTubeLimitKind.RATE_LIMIT ->
                PendingPauseReason.RATE_LIMIT

            YouTubeLimitKind.RESOURCE_LIMIT ->
                PendingPauseReason.RESOURCE_LIMIT

            YouTubeLimitKind.UNKNOWN_429 ->
                PendingPauseReason.UNKNOWN_API_LIMIT
        }

    fun userMessage(
        kind: YouTubeLimitKind,
        action: WritePauseAction
    ): String {
        val target =
            when (action) {
                WritePauseAction.CREATE_PLAYLIST ->
                    "створення плейлиста"

                WritePauseAction.ADD_TRACK ->
                    "додавання треків"
            }

        return when (kind) {
            YouTubeLimitKind.DAILY_QUOTA ->
                "YouTube Data API повідомив про вичерпання добової квоти. " +
                    "Операцію «$target» поставлено на паузу. " +
                    "Незавершене завдання збережено в «Черзі». " +
                    "Продовжіть його після відновлення квоти."

            YouTubeLimitKind.RATE_LIMIT ->
                "YouTube тимчасово обмежив частоту write-запитів. " +
                    "Операцію «$target» поставлено на паузу. " +
                    "Не запускайте її багато разів поспіль: " +
                    "зачекайте деякий час і потім натисніть «Продовжити» у «Черзі». " +
                    "Точного часу розблокування API не повідомив."

            YouTubeLimitKind.RESOURCE_LIMIT ->
                "Google/YouTube відхилив write-запит через ресурсний ліміт. " +
                    "Операцію «$target» поставлено на паузу, а незавершене завдання " +
                    "збережено в «Черзі». Зачекайте деякий час і продовжіть вручну."

            YouTubeLimitKind.UNKNOWN_429 ->
                "YouTube тимчасово заблокував write-запит (HTTP 429), " +
                    "але не вказав точний тип ліміту. " +
                    "Це не вважається підтвердженою добовою квотою. " +
                    "Операцію «$target» поставлено на паузу. " +
                    "Зачекайте деякий час і потім натисніть «Продовжити» у «Черзі»."
        }
    }
}

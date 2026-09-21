package com.saney.ytmimporter.util

import com.saney.ytmimporter.youtube.YouTubeApiException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMessages {
    fun userMessage(
        error: Throwable,
        fallback: String = "Сталася помилка"
    ): String {
        val apiError = error as? YouTubeApiException

        if (apiError != null) {
            if (apiError.isQuotaError) {
                return "Закінчилася квота YouTube Data API. " +
                    "Невиконані операції можна продовжити після відновлення квоти."
            }

            val reason = apiError.reason.orEmpty().lowercase()

            return when (apiError.httpCode) {
                400 ->
                    if (
                        reason.contains(
                            "playlistoperationunsupported"
                        )
                    ) {
                        "Цей системний плейлист YouTube не можна видалити."
                    } else {
                        "YouTube відхилив запит. Перевірте вибраний трек або плейлист."
                    }

                401 ->
                    "Авторизація Google більше не дійсна. " +
                        "Відкрийте «2. Акаунт» і увійдіть знову."

                403 ->
                    when {
                        reason.contains("forbidden") ||
                            reason.contains("insufficientpermissions") ->
                            "Google не дозволив цю операцію. " +
                                "Перевірте акаунт і доступ YouTube."

                        else ->
                            "YouTube заборонив цю операцію для поточного акаунта або каналу."
                    }

                404 ->
                    "YouTube не знайшов потрібний трек або плейлист. " +
                        "Можливо, його видалено або він недоступний."

                409 ->
                    "YouTube повідомив про конфлікт даних. " +
                        "Оновіть список і спробуйте ще раз."

                429 ->
                    "YouTube тимчасово обмежив кількість запитів. " +
                        "Спробуйте пізніше."

                in 500..599 ->
                    "Тимчасова помилка на стороні Google/YouTube. " +
                        "Спробуйте ще раз трохи пізніше."

                else ->
                    "$fallback (HTTP ${apiError.httpCode})."
            }
        }

        return when (error) {
            is UnknownHostException ->
                "Немає доступу до інтернету або не вдалося знайти сервер Google."

            is SocketTimeoutException ->
                "Google/YouTube не відповів вчасно. Перевірте інтернет і повторіть спробу."

            is ConnectException ->
                "Не вдалося підключитися до Google/YouTube. Перевірте інтернет."

            else ->
                error.message
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: fallback
        }
    }

    fun technicalDetails(error: Throwable): String =
        when (error) {
            is YouTubeApiException ->
                buildString {
                    append("HTTP ${error.httpCode}")
                    if (!error.reason.isNullOrBlank()) {
                        append(" • reason=${error.reason}")
                    }
                    if (error.message.isNotBlank()) {
                        append(" • ${error.message}")
                    }
                }

            else ->
                "${error::class.java.simpleName}: " +
                    (error.message ?: "без додаткового опису")
        }
}

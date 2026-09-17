package com.saney.ytmimporter.youtube

import com.saney.ytmimporter.model.GoogleAccountInfo
import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import com.saney.ytmimporter.model.TrackStatus
import com.saney.ytmimporter.model.YouTubeChannelInfo
import com.saney.ytmimporter.model.YouTubePlaylistInfo
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeApiException(
    val httpCode: Int,
    val reason: String?,
    override val message: String
) : IllegalStateException(message) {
    val isQuotaError: Boolean
        get() {
            val normalizedReason = reason.orEmpty().lowercase()
            val normalizedMessage = message.lowercase()

            return normalizedReason.contains("quota") ||
                normalizedReason.contains("dailylimit") ||
                normalizedReason.contains("variabletermlimit") ||
                normalizedReason.contains("variabletermexpireddaily") ||
                normalizedMessage.contains("quota exceeded") ||
                normalizedMessage.contains("daily limit")
        }
}

class YouTubeApi {
    data class ApiResponse(val code: Int, val body: String)

    data class PlaylistVideoIdsResult(
        val videoIds: Set<String>,
        val requestCount: Int
    )

    data class PlaylistTracksResult(
        val tracks: List<Track>,
        val requestCount: Int
    )

    fun getGoogleAccountInfo(accessToken: String): GoogleAccountInfo {
        val response = request(
            method = "GET",
            url = "https://www.googleapis.com/oauth2/v2/userinfo",
            accessToken = accessToken
        )
        requireSuccess(response, "Google account")

        val json = JSONObject(response.body)
        return GoogleAccountInfo(
            name = json.optString("name").trim(),
            email = json.optString("email").trim()
        )
    }

    fun getMyYouTubeChannel(accessToken: String): YouTubeChannelInfo? {
        val url =
            "https://www.googleapis.com/youtube/v3/channels" +
                "?part=snippet" +
                "&mine=true" +
                "&maxResults=1"

        val response = request("GET", url, accessToken)
        requireSuccess(response, "YouTube channel")

        val items = JSONObject(response.body).optJSONArray("items") ?: return null
        if (items.length() == 0) return null

        val item = items.getJSONObject(0)
        val snippet = item.optJSONObject("snippet")

        return YouTubeChannelInfo(
            id = item.optString("id").trim(),
            title = decodeEntities(snippet?.optString("title").orEmpty()).trim()
        )
    }

    fun listMyPlaylists(accessToken: String): List<YouTubePlaylistInfo> {
        val result = mutableListOf<YouTubePlaylistInfo>()
        var pageToken: String? = null
        var pages = 0

        do {
            var url =
                "https://www.googleapis.com/youtube/v3/playlists" +
                    "?part=snippet,status,contentDetails" +
                    "&mine=true" +
                    "&maxResults=50"

            if (!pageToken.isNullOrBlank()) {
                url += "&pageToken=" +
                    URLEncoder.encode(pageToken, Charsets.UTF_8.name())
            }

            val response = request("GET", url, accessToken)
            requireSuccess(response, "Список плейлистів")

            val json = JSONObject(response.body)
            val items = json.optJSONArray("items")

            if (items != null) {
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val snippet = item.optJSONObject("snippet")
                    val status = item.optJSONObject("status")
                    val contentDetails = item.optJSONObject("contentDetails")

                    val id = item.optString("id").trim()
                    if (id.isBlank()) continue

                    result += YouTubePlaylistInfo(
                        id = id,
                        title = decodeEntities(
                            snippet?.optString("title").orEmpty()
                        ).trim().ifBlank { "Без назви" },
                        privacyStatus =
                            status?.optString("privacyStatus")
                                .orEmpty()
                                .ifBlank { "private" },
                        itemCount =
                            contentDetails?.optLong("itemCount", 0L) ?: 0L
                    )
                }
            }

            pageToken =
                json.optString("nextPageToken")
                    .trim()
                    .takeIf { it.isNotBlank() }

            pages += 1
        } while (pageToken != null && pages < 20)

        return result.sortedBy { it.title.lowercase() }
    }

    fun listPlaylistTracks(
        accessToken: String,
        playlistId: String
    ): PlaylistTracksResult {
        val tracks = mutableListOf<Track>()
        var pageToken: String? = null
        var requestCount = 0
        var pages = 0

        do {
            var url =
                "https://www.googleapis.com/youtube/v3/playlistItems" +
                    "?part=snippet,contentDetails" +
                    "&maxResults=50" +
                    "&playlistId=" +
                    URLEncoder.encode(
                        playlistId,
                        Charsets.UTF_8.name()
                    )

            if (!pageToken.isNullOrBlank()) {
                url += "&pageToken=" +
                    URLEncoder.encode(
                        pageToken,
                        Charsets.UTF_8.name()
                    )
            }

            val response =
                request(
                    "GET",
                    url,
                    accessToken
                )

            requestCount += 1

            requireSuccess(
                response,
                "Завантаження треків плейлиста"
            )

            val json = JSONObject(response.body)
            val items = json.optJSONArray("items")

            if (items != null) {
                for (i in 0 until items.length()) {
                    val item =
                        items.getJSONObject(i)

                    val snippet =
                        item.optJSONObject("snippet")

                    val contentDetails =
                        item.optJSONObject(
                            "contentDetails"
                        )

                    val contentVideoId =
                        contentDetails
                            ?.optString("videoId")
                            .orEmpty()
                            .trim()

                    val resourceVideoId =
                        snippet
                            ?.optJSONObject(
                                "resourceId"
                            )
                            ?.optString("videoId")
                            .orEmpty()
                            .trim()

                    val videoId =
                        contentVideoId
                            .ifBlank {
                                resourceVideoId
                            }

                    if (videoId.isBlank()) {
                        continue
                    }

                    val title =
                        decodeEntities(
                            snippet
                                ?.optString("title")
                                .orEmpty()
                        )
                            .trim()
                            .ifBlank {
                                "YouTube video $videoId"
                            }

                    val channel =
                        decodeEntities(
                            snippet
                                ?.optString(
                                    "videoOwnerChannelTitle"
                                )
                                .orEmpty()
                        )
                            .trim()
                            .ifBlank {
                                "YouTube"
                            }

                    tracks +=
                        Track(
                            originalTitle = title,
                            originalArtist = channel,
                            selectedVideoId = videoId,
                            selectedTitle = title,
                            selectedChannel = channel,
                            status = TrackStatus.MATCHED,
                            candidates = emptyList(),
                            manuallySelected = false,
                            error = null,
                            historyIndex = tracks.size
                        )
                }
            }

            pageToken =
                json.optString("nextPageToken")
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }

            pages += 1
        } while (
            pageToken != null &&
            pages < 200
        )

        return PlaylistTracksResult(
            tracks = tracks,
            requestCount = requestCount
        )
    }

    fun listPlaylistVideoIds(
        accessToken: String,
        playlistId: String
    ): PlaylistVideoIdsResult {
        val videoIds = linkedSetOf<String>()
        var pageToken: String? = null
        var requestCount = 0
        var pages = 0

        do {
            var url =
                "https://www.googleapis.com/youtube/v3/playlistItems" +
                    "?part=contentDetails" +
                    "&maxResults=50" +
                    "&playlistId=" +
                    URLEncoder.encode(
                        playlistId,
                        Charsets.UTF_8.name()
                    )

            if (!pageToken.isNullOrBlank()) {
                url += "&pageToken=" +
                    URLEncoder.encode(
                        pageToken,
                        Charsets.UTF_8.name()
                    )
            }

            val response = request("GET", url, accessToken)
            requestCount += 1
            requireSuccess(
                response,
                "Перевірка дублікатів у плейлисті"
            )

            val json = JSONObject(response.body)
            val items = json.optJSONArray("items")

            if (items != null) {
                for (i in 0 until items.length()) {
                    val videoId =
                        items
                            .getJSONObject(i)
                            .optJSONObject("contentDetails")
                            ?.optString("videoId")
                            .orEmpty()
                            .trim()

                    if (videoId.isNotBlank()) {
                        videoIds += videoId
                    }
                }
            }

            pageToken =
                json.optString("nextPageToken")
                    .trim()
                    .takeIf { it.isNotBlank() }

            pages += 1
        } while (pageToken != null && pages < 200)

        return PlaylistVideoIdsResult(
            videoIds = videoIds,
            requestCount = requestCount
        )
    }

    fun getVideoInfo(
        accessToken: String,
        videoId: String
    ): SearchCandidate? {
        val encodedId =
            URLEncoder.encode(
                videoId,
                Charsets.UTF_8.name()
            )

        val url =
            "https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet" +
                "&id=$encodedId"

        val response = request("GET", url, accessToken)
        requireSuccess(
            response,
            "Отримання даних відео"
        )

        val items =
            JSONObject(response.body)
                .optJSONArray("items")
                ?: return null

        if (items.length() == 0) {
            return null
        }

        val item = items.getJSONObject(0)
        val snippet =
            item.optJSONObject("snippet")
                ?: return null

        val title =
            decodeEntities(
                snippet.optString("title")
            ).trim()

        val channel =
            decodeEntities(
                snippet.optString("channelTitle")
            ).trim()

        return SearchCandidate(
            videoId = videoId,
            title =
                title.ifBlank {
                    "YouTube video $videoId"
                },
            channelTitle =
                channel.ifBlank {
                    "YouTube"
                },
            score = 1.0
        )
    }

    fun search(accessToken: String, track: Track): List<SearchCandidate> {
        val q = URLEncoder.encode(track.query, Charsets.UTF_8.name())

        // maxResults=10 (до 10 результатів) дає MatchScorer більше варіантів
        // без другого search-запиту для того самого треку.
        val url =
            "https://www.googleapis.com/youtube/v3/search" +
            "?part=snippet" +
            "&type=video" +
            "&order=relevance" +
            "&maxResults=10" +
            "&q=$q"

        val response = request("GET", url, accessToken)
        requireSuccess(response, "Пошук")

        val json = JSONObject(response.body)
        val items = json.optJSONArray("items") ?: return emptyList()

        val result = mutableListOf<SearchCandidate>()
        val seenVideoIds = mutableSetOf<String>()

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val videoId = item.optJSONObject("id")?.optString("videoId").orEmpty()
            if (videoId.isBlank() || !seenVideoIds.add(videoId)) continue

            val snippet = item.optJSONObject("snippet") ?: continue
            val title = decodeEntities(snippet.optString("title"))
            val channel = decodeEntities(snippet.optString("channelTitle"))

            val score = MatchScorer.score(
                artist = track.originalArtist,
                title = track.originalTitle,
                candidateTitle = title,
                channel = channel
            )

            result += SearchCandidate(
                videoId = videoId,
                title = title,
                channelTitle = channel,
                score = score
            )
        }

        return result.sortedByDescending { it.score }
    }

    fun createPlaylist(
        accessToken: String,
        title: String,
        privacyStatus: String
    ): String {
        val url = "https://www.googleapis.com/youtube/v3/playlists?part=snippet,status"
        val body = JSONObject()
            .put(
                "snippet",
                JSONObject()
                    .put("title", title.take(150))
                    .put("description", "Створено через YTM Importer")
            )
            .put(
                "status",
                JSONObject().put(
                    "privacyStatus",
                    when (privacyStatus) {
                        "public", "unlisted", "private" -> privacyStatus
                        else -> "private"
                    }
                )
            )
            .toString()

        val response = request("POST", url, accessToken, body)
        requireSuccess(response, "Створення плейлиста")
        return JSONObject(response.body).getString("id")
    }

    fun addVideo(accessToken: String, playlistId: String, videoId: String) {
        val url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet"
        val body = JSONObject()
            .put(
                "snippet",
                JSONObject()
                    .put("playlistId", playlistId)
                    .put(
                        "resourceId",
                        JSONObject()
                            .put("kind", "youtube#video")
                            .put("videoId", videoId)
                    )
            )
            .toString()

        val response = request("POST", url, accessToken, body)
        requireSuccess(response, "Додавання треку")
    }

    private fun request(
        method: String,
        url: String,
        accessToken: String,
        body: String? = null
    ): ApiResponse {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 20_000
        connection.readTimeout = 30_000
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
        connection.setRequestProperty("Accept", "application/json")

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use {
                it.write(body.toByteArray(Charsets.UTF_8))
            }
        }

        val code = connection.responseCode
        val input =
            if (code in 200..299) connection.inputStream
            else connection.errorStream

        val text = input?.use { stream ->
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
        }.orEmpty()

        connection.disconnect()
        return ApiResponse(code, text)
    }

    private fun requireSuccess(response: ApiResponse, action: String) {
        if (response.code in 200..299) return

        val errorJson =
            runCatching {
                JSONObject(response.body).optJSONObject("error")
            }.getOrNull()

        val message =
            errorJson?.optString("message").orEmpty()

        val errors =
            errorJson?.optJSONArray("errors")

        val reason =
            if (errors != null && errors.length() > 0) {
                errors.optJSONObject(0)?.optString("reason")
            } else {
                null
            }

        val fullMessage =
            "$action: HTTP ${response.code}" +
                if (message.isNotBlank()) " — $message" else "" +
                if (!reason.isNullOrBlank()) " [$reason]" else ""

        throw YouTubeApiException(
            httpCode = response.code,
            reason = reason,
            message = fullMessage
        )
    }

    private fun decodeEntities(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}

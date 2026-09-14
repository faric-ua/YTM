package com.saney.ytmimporter.youtube

import com.saney.ytmimporter.model.SearchCandidate
import com.saney.ytmimporter.model.Track
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeApi {
    data class ApiResponse(val code: Int, val body: String)

    fun search(accessToken: String, track: Track): List<SearchCandidate> {
        val q = URLEncoder.encode(track.query, Charsets.UTF_8.name())

        // maxResults=10 gives the scorer more choices without making
        // a second search request for the same track.
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

    fun createPlaylist(accessToken: String, title: String): String {
        val url = "https://www.googleapis.com/youtube/v3/playlists?part=snippet,status"
        val body = JSONObject()
            .put(
                "snippet",
                JSONObject()
                    .put("title", title.take(150))
                    .put("description", "Створено через YTM Importer")
            )
            .put("status", JSONObject().put("privacyStatus", "private"))
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

        val message = runCatching {
            JSONObject(response.body)
                .optJSONObject("error")
                ?.optString("message")
                .orEmpty()
        }.getOrDefault("")

        throw IllegalStateException(
            "$action: HTTP ${response.code}" +
                if (message.isNotBlank()) " — $message" else ""
        )
    }

    private fun decodeEntities(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}

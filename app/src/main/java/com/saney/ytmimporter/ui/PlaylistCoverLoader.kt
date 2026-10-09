package com.saney.ytmimporter.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Best-effort public playlist artwork; metadata is always usable offline.
 * Only documented YouTube thumbnail hosts or a well-formed videoId are used.
 * Never touches OAuth tokens or downloads audio/video streams.
 */
object PlaylistCoverLoader {
    private val workers = Executors.newFixedThreadPool(2)
    private val cache = LruCache<String, Bitmap>(24)
    private val videoIdPattern = Regex("^[A-Za-z0-9_-]{11}$")

    fun bind(
        activity: Activity,
        image: ImageView,
        videoId: String?,
        suppliedThumbnail: String? = null
    ) {
        val address =
            trustedThumbnail(suppliedThumbnail)
                ?: videoId?.takeIf(videoIdPattern::matches)
                    ?.let { "https://i.ytimg.com/vi/$it/mqdefault.jpg" }
                ?: return
        image.tag = address
        synchronized(cache) { cache.get(address) }?.let {
            image.setImageBitmap(it)
            return
        }
        workers.execute {
            val bitmap = runCatching {
                val connection = URL(address).openConnection() as HttpURLConnection
                connection.connectTimeout = 4500
                connection.readTimeout = 4500
                connection.instanceFollowRedirects = false
                connection.useCaches = true
                try {
                    if (connection.responseCode != 200) return@runCatching null
                    connection.inputStream.use { BitmapFactory.decodeStream(it) }
                } finally {
                    connection.disconnect()
                }
            }.getOrNull() ?: return@execute
            synchronized(cache) { cache.put(address, bitmap) }
            activity.runOnUiThread {
                if (!activity.isDestroyed && image.tag == address) {
                    image.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun trustedThumbnail(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val uri = android.net.Uri.parse(raw)
            val host = uri.host?.lowercase()
            if (uri.scheme == "https" &&
                host in setOf("i.ytimg.com", "img.youtube.com")
            ) raw else null
        }.getOrNull()
    }
}

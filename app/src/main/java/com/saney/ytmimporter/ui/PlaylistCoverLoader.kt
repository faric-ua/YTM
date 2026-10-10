package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.Executors

/**
 * Public official YouTube thumbnail images only; never fetches video streams.
 * Private app-local bounded artwork cache avoids refetching the same image
 * on reopen, process restart or rotation. API tokens are never sent/stored.
 */
object PlaylistCoverLoader {
    private val workers = Executors.newFixedThreadPool(2)
    private val cache = LruCache<String, Bitmap>(24)
    private val waiting = mutableMapOf<String, MutableList<Pair<Activity, ImageView>>>()
    private val videoIdPattern = Regex("^[A-Za-z0-9_-]{11}$")
    private const val MAX_IMAGE_BYTES = 1_048_576
    private const val MAX_DISK_BYTES = 24L * 1024L * 1024L
    private const val MAX_DISK_FILES = 240

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
            image.clearColorFilter()
            image.setImageBitmap(it)
            return
        }
        // Several rows can share the same cover. Schedule one disk/network
        // read and deliver it to each still-visible target.
        synchronized(waiting) {
            val existing = waiting[address]
            if (existing != null) {
                existing.add(activity to image)
                return
            }
            waiting[address] = mutableListOf(activity to image)
        }
        workers.execute {
            val bitmap = runCatching { loadArtwork(activity.applicationContext, address) }
                .getOrNull()
            if (bitmap != null) synchronized(cache) { cache.put(address, bitmap) }
            val targets = synchronized(waiting) { waiting.remove(address).orEmpty() }
            if (bitmap != null) for ((owner, target) in targets) {
                owner.runOnUiThread {
                    if (!owner.isDestroyed && target.tag == address) {
                        target.clearColorFilter()
                        target.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    private fun loadArtwork(context: Context, address: String): Bitmap? {
        // The canonical SHA-256 key has no raw URL, account name or token.
        val key = MessageDigest.getInstance("SHA-256")
            .digest(address.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val dir = File(context.filesDir, "playlist_covers_v1")
        val file = File(dir, "$key.img")
        synchronized(PlaylistCoverLoader) {
            if (file.isFile) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    file.setLastModified(System.currentTimeMillis())
                    return bitmap
                }
                file.delete() // Corrupted/incomplete cache entry: refill safely.
            }
        }
        val connection = URL(address).openConnection() as HttpURLConnection
        connection.connectTimeout = 4500
        connection.readTimeout = 4500
        connection.instanceFollowRedirects = false
        connection.useCaches = true
        val payload = try {
            if (connection.responseCode != 200) return null
            val length = connection.contentLengthLong
            if (length > MAX_IMAGE_BYTES) return null
            connection.inputStream.use { stream ->
                ByteArrayOutputStream().use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val n = stream.read(buffer)
                        if (n < 0) break
                        if (n == 0) continue
                        if (output.size() + n > MAX_IMAGE_BYTES) return null
                        output.write(buffer, 0, n)
                    }
                    output.toByteArray()
                }
            }
        } finally {
            connection.disconnect()
        }
        val bitmap = BitmapFactory.decodeByteArray(payload, 0, payload.size) ?: return null
        synchronized(PlaylistCoverLoader) {
            dir.mkdirs()
            val temp = File(dir, "$key.${Thread.currentThread().id}.tmp")
            try {
                temp.writeBytes(payload)
                if (!file.isFile && !temp.renameTo(file)) {
                    temp.delete()
                } else {
                    temp.delete()
                }
            } catch (_: Exception) {
                temp.delete()
            }
            trimCache(dir)
        }
        return bitmap
    }

    private fun trimCache(dir: File) {
        val ordered = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".img") }
            ?.sortedBy { it.lastModified() } ?: return
        var total = ordered.sumOf { it.length() }
        var count = ordered.size
        for (file in ordered) {
            if (total <= MAX_DISK_BYTES && count <= MAX_DISK_FILES) break
            val size = file.length()
            if (file.delete()) {
                total -= size
                count--
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

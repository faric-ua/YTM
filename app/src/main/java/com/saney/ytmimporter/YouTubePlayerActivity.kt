package com.saney.ytmimporter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.saney.ytmimporter.library.YouTubeEmbedPolicy
import com.saney.ytmimporter.ui.AppThemeManager
import com.saney.ytmimporter.ui.UiChrome

/**
 * Official visible YouTube player only. The WebView never extracts a media
 * stream, hides player controls/branding or starts playback in the background.
 * There is no API write and no implicit playlist switching.
 */
class YouTubePlayerActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var page: LinearLayout
    private var player: WebView? = null
    private var fullscreen: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppThemeManager.applyWindow(this)
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)?.take(160)
            ?.takeIf(String::isNotBlank) ?: "Відео YouTube"
        val palette = AppThemeManager.palette(this)

        root = FrameLayout(this).apply {
            setBackgroundColor(palette.background)
        }
        page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(12))
        }
        root.addView(page, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(root)
        UiChrome.applyScreenInsets(this, page)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(action("‹") { finish() }, LinearLayout.LayoutParams(dp(54), dp(52)))
        header.addView(caption(title, 19f, palette.text), LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        ))
        page.addView(header)

        if (!YouTubeEmbedPolicy.isValidVideoId(videoId)) {
            page.addView(caption(
                "Немає коректного YouTube videoId для відтворення.",
                15f,
                palette.muted
            ))
            return
        }

        // Full official YouTube iframe remains visible, with its native controls,
        // ads and branding. At least 220dp high even on a narrow phone.
        val web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(
                    view: View?,
                    callback: CustomViewCallback?
                ) {
                    if (view == null || callback == null || fullscreen != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    fullscreen = view
                    fullscreenCallback = callback
                    page.visibility = View.GONE
                    root.addView(view, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ))
                }

                override fun onHideCustomView() {
                    closeFullscreen()
                }
            }
        }
        player = web
        page.addView(web, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(232)
        ).apply { topMargin = dp(12) })

        page.addView(caption(
            "Офіційний YouTube-плеєр. Деякі відео можуть забороняти вбудоване відтворення.",
            13f,
            palette.muted
        ).apply { setPadding(0, dp(14), 0, dp(12)) })

        page.addView(action("Відкрити це відео на YouTube ↗") {
            runCatching {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(YouTubeEmbedPolicy.watchUrl(videoId!!))))
            }.onFailure {
                Toast.makeText(this, "Не вдалося відкрити YouTube.", Toast.LENGTH_LONG).show()
            }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(54)
        ))

        // Per YouTube's WebView embedding requirements, identify the application
        // on the player request. Never pass the Google OAuth token into WebView.
        web.loadUrl(
            YouTubeEmbedPolicy.embedUrl(videoId!!),
            mapOf("Referer" to YouTubeEmbedPolicy.referer(packageName))
        )
    }

    override fun onPause() {
        player?.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        player?.onResume()
    }

    @Deprecated("Android Activity back callback compatibility")
    override fun onBackPressed() {
        if (fullscreen != null) closeFullscreen() else super.onBackPressed()
    }

    private fun closeFullscreen() {
        fullscreen?.let { root.removeView(it) }
        fullscreen = null
        page.visibility = View.VISIBLE
        val callback = fullscreenCallback
        fullscreenCallback = null
        callback?.onCustomViewHidden()
    }

    override fun onDestroy() {
        if (fullscreen != null) closeFullscreen()
        player?.let { web ->
            (web.parent as? ViewGroup)?.removeView(web)
            web.stopLoading()
            web.destroy()
        }
        player = null
        super.onDestroy()
    }

    private fun caption(value: String, size: Float, color: Int) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            maxLines = 3
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }

    private fun action(label: String, onClick: () -> Unit) =
        Button(this).apply {
            text = label
            isAllCaps = false
            maxLines = 2
            setOnClickListener { onClick() }
        }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        const val EXTRA_VIDEO_ID = "official_youtube_video_id"
        const val EXTRA_TITLE = "official_youtube_video_title"
    }
}

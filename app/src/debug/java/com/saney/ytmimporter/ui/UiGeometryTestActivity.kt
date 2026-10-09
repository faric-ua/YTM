package com.saney.ytmimporter.ui

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.util.TypedValue

/**
 * Debug-build-only Android layout fixture. No account, data store, network,
 * Google API, or mutation actions are constructed here.
 */
class UiGeometryTestActivity : Activity() {
    lateinit var rootPanel: LinearLayout
        private set
    lateinit var actionPanel: LinearLayout
        private set
    lateinit var actions: List<Button>
        private set
    lateinit var contentScroll: ScrollView
        private set

    var actionsInvoked: Int = 0
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSessionFooter(widthDp = 320, fontSp = 15f)
    }

    fun showPreviewCard(
        widthDp: Int,
        fontSp: Float,
        firstCaption: String = "Усі готові",
        secondCaption: String = "Лише доповнити",
        minButtonWidthDp: Int = 132
    ) {
        val root = makeRoot()
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        root.addView(
            card,
            LinearLayout.LayoutParams(dp(safeFixtureWidth(widthDp)), ViewGroup.LayoutParams.WRAP_CONTENT)
        )

        val actionsContainer = LinearLayout(this)
        card.addView(
            actionsContainer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        configureActions(
            actionsContainer, firstCaption, secondCaption, fontSp
        )
        UiChrome.addAdaptiveActionButtons(
            activity = this,
            container = actionsContainer,
            buttons = actions,
            buttonHeightDp = 64,
            minButtonWidthDp = minButtonWidthDp,
            // Deliberately optimistic pre-measure estimate: measured bounds
            // MUST override it before Android paints the horizontal layout.
            horizontalChromeDp = 0
        )
        contentScroll = ScrollView(this)
        root.addView(
            contentScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )
        mount(root, actionsContainer)
    }

    fun showSessionFooter(
        widthDp: Int,
        fontSp: Float,
        primary: String = "Почати синхронізацію",
        secondary: String = "Закрити"
    ) {
        val root = makeRoot()
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val hugeBody = TextView(this).apply {
            text = (1..80).joinToString("\n") {
                "Плейліст $it: read-only тест розміщення панелі"
            }
            textSize = 15f
        }
        scroll.addView(hugeBody)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(12))
        }
        root.addView(
            footer,
            LinearLayout.LayoutParams(
                dp(safeFixtureWidth(widthDp)), ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        configureActions(footer, primary, secondary, fontSp)
        UiChrome.addAdaptiveActionButtons(
            activity = this,
            container = footer,
            buttons = actions,
            buttonHeightDp = 72,
            minButtonWidthDp = 132,
            horizontalChromeDp = 0
        )
        // Session explicitly permits two-line captions when necessary.
        actions.forEach { it.maxLines = 2 }
        contentScroll = scroll
        mount(root, footer)
    }

    private fun configureActions(
        container: LinearLayout,
        first: String,
        second: String,
        fontSp: Float
    ) {
        actionPanel = container
        actions = listOf(first, second).map { label ->
            Button(this).apply {
                text = label
                isAllCaps = false
                setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSp)
                setOnClickListener { actionsInvoked++ }
            }
        }
    }

    private fun makeRoot() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(AppThemeManager.palette(this@UiGeometryTestActivity).background)
    }

    private fun mount(root: LinearLayout, container: LinearLayout) {
        rootPanel = root
        actionPanel = container
        setContentView(root)
        UiChrome.applyScreenInsets(this, root)
    }

    // A requested synthetic width must never extend beyond the actual
    // emulator window; otherwise global-visible-rect clipping tests would
    // incorrectly blame UiChrome for a fixture that is wider than the screen.
    private fun safeFixtureWidth(requestedDp: Int): Int =
        minOf(requestedDp, resources.configuration.screenWidthDp - 32)
            .coerceAtLeast(220)

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}

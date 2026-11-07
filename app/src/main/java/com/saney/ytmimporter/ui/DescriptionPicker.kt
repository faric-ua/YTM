package com.saney.ytmimporter.ui

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.library.PlaylistDescriptionPolicy
import com.saney.ytmimporter.storage.PlaylistDescriptionStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Batch 21: the description block of «Створити / додати» (faric 01:19,
 * mockup `docs/mockups/create_description_mock.png`): the signature that is
 * always the first line, the «Опис (необов'язково)» tick, and — unfolded —
 * the strip of all saved descriptions with «✓ активний» and «використано N
 * разів», «Змінити цей опис», «Новий опис». Nothing here talks to YouTube.
 */
class DescriptionPicker(
    private val activity: Activity,
    private val store: PlaylistDescriptionStore,
    /** Edit the description with this id, or write a new one (null). */
    private val onEdit: (String?) -> Unit,
    /** Batch 22: delete this description (the screen asks first). */
    private val onDelete: (String) -> Unit = {}
) {
    val view: LinearLayout = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
    private val palette get() = AppThemeManager.palette(activity)

    init { render() }

    fun render() {
        view.removeAllViews()
        val state = store.load()
        view.addView(label("Перший рядок опису — завжди"))
        view.addView(TextView(activity).apply {
            text = PlaylistDescriptionPolicy.SIGNATURE
            textSize = 14f
            setTextColor(palette.text)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), palette.muted, dp(6).toFloat(), dp(4).toFloat())
            }
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) })

        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(2), dp(8), dp(2))
            minimumHeight = dp(48)
            background = AppThemeManager.surfaceDrawable(context = activity,
                fill = palette.surfaceAlt, radiusDp = 12)
            contentDescription = "Опис плейліста — показати мої описи"
            setOnClickListener { expanded = !expanded; render() }
        }
        row.addView(CheckBox(activity).apply {
            isChecked = state.enabled
            contentDescription = "Додавати мій опис"
            setOnCheckedChangeListener { _, checked ->
                store.update { it.copy(enabled = checked) }
                render()
            }
        })
        row.addView(TextView(activity).apply {
            text = "Опис (необов'язково)"
            textSize = 15f
            setTextColor(palette.text)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(activity).apply {
            text = if (expanded) "⌃" else "⌄"
            textSize = 18f
            setTextColor(palette.muted)
        })
        view.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))

        if (!expanded) {
            view.addView(label(
                if (!state.enabled) "Зараз: лише підпис, без опису."
                else state.active?.let { "Зараз: «${PlaylistDescriptionPolicy.firstLine(it.text)}»" }
                    ?: "Зараз: лише підпис, без опису."
            ).apply { setPadding(dp(4), dp(4), dp(4), dp(8)) })
            return
        }

        view.addView(label("Мої описи — гортайте ← →, торкніться, щоб вибрати"))
        val strip = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL }
        var activeTile: View? = null
        state.descriptions.asReversed().forEach { item ->
            val tile = tile(state, item)
            if (item.id == state.activeId) activeTile = tile
            strip.addView(tile, LinearLayout.LayoutParams(dp(250),
                ViewGroup.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) })
        }
        val scroller = HorizontalScrollView(activity).apply {
            isHorizontalScrollBarEnabled = true
            addView(strip)
            setOnScrollChangeListener { _, x, _, _, _ -> stripScrollX = x }
        }
        view.addView(scroller, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4); bottomMargin = dp(6) })
        // Batch 22 (faric 2026-11-07 18:32): the active tile is always seen
        // whole — a tile cut on the right sticks to the right edge, one cut on
        // the left to the left edge. The strip keeps its place between redraws.
        val keep = stripScrollX
        scroller.post {
            scroller.scrollTo(keep, 0)
            activeTile?.let { tile ->
                val left = tile.left
                val right = tile.right
                val viewLeft = scroller.scrollX
                val viewRight = viewLeft + scroller.width
                val target = when {
                    left < viewLeft -> left
                    right > viewRight -> right - scroller.width
                    else -> viewLeft
                }
                if (target != viewLeft) scroller.smoothScrollTo(target.coerceAtLeast(0), 0)
            }
        }
        state.active?.let { active ->
            val number = PlaylistDescriptionPolicy.number(state, active.id)
            view.addView(button("✎ Змінити цей опис (№$number)") { onEdit(active.id) })
            view.addView(button("🗑 Видалити цей опис (№$number)") { onDelete(active.id) })
        }
        view.addView(button("＋ Новий опис") { onEdit(null) })
        view.addView(label("Старі описи не губляться — лишаються в цій стрічці. Вибраний (✓) " +
            "іде в усі нові плейлісти, доки не виберете інший.").apply {
            setPadding(dp(4), dp(2), dp(4), dp(8))
        })
    }

    private fun tile(state: PlaylistDescriptionPolicy.State,
                     item: PlaylistDescriptionPolicy.Description): View {
        val active = item.id == state.activeId
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(9), dp(10), dp(10))
            tag = "description:${item.id}"
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(palette.surfaceAlt)
                setStroke(dp(if (active) 2 else 1),
                    if (active) palette.semantic.success else palette.muted)
            }
            contentDescription = "Опис №${PlaylistDescriptionPolicy.number(state, item.id)}" +
                if (active) ", активний" else ", торкніться, щоб вибрати"
            setOnClickListener {
                store.update { PlaylistDescriptionPolicy.activate(it, item.id) }
                render()
            }
            addView(LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(small("№${PlaylistDescriptionPolicy.number(state, item.id)} • " +
                    DATE.format(Date(item.createdAt))), LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                if (active) addView(small("✓ активний").apply {
                    setTextColor(palette.semantic.success)
                    setTypeface(typeface, Typeface.BOLD)
                })
            })
            addView(TextView(activity).apply {
                text = item.text
                textSize = 12.5f
                setTextColor(palette.text)
                maxLines = 7
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, dp(5), 0, dp(5))
            })
            addView(small(PlaylistDescriptionPolicy.usesLine(item.uses)))
        }
    }

    private fun label(value: String) = TextView(activity).apply {
        text = value
        textSize = 12.5f
        setTextColor(palette.muted)
        setPadding(dp(4), dp(6), dp(4), dp(4))
    }

    private fun small(value: String) = TextView(activity).apply {
        text = value
        textSize = 11.5f
        setTextColor(palette.muted)
    }

    private fun button(value: String, onClick: () -> Unit) = Button(activity).apply {
        text = value
        isAllCaps = false
        textSize = 13.5f
        setTextColor(palette.text)
        background = AppThemeManager.surfaceDrawable(context = activity,
            fill = palette.surface, radiusDp = 11)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            dp(48)).apply { bottomMargin = dp(6) }
    }

    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()

    companion object {
        /** Unfolded or not — kept for a rotation (process-wide is enough). */
        private var expanded = false
        /** Batch 22: where the strip was scrolled, kept between redraws. */
        private var stripScrollX = 0
        private val DATE = SimpleDateFormat("dd.MM.yyyy", Locale.ROOT)
    }
}

package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.R
import kotlin.math.roundToInt

object HomeDashboardChrome {
    fun sectionTitle(
        activity: Activity,
        label: String
    ): TextView {
        val palette =
            AppThemeManager.palette(activity)

        return TextView(activity).apply {
            text = label
            textSize = 13f
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                palette.text
            )
            setPadding(
                dp(activity, 14),
                dp(activity, 7),
                dp(activity, 14),
                dp(activity, 7)
            )
        }
    }

    fun sectionCard(
        activity: Activity,
        title: String
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(activity)

        return LinearLayout(activity).apply {
            orientation =
                LinearLayout.VERTICAL
            setPadding(
                dp(activity, 12),
                dp(activity, 9),
                dp(activity, 12),
                dp(activity, 10)
            )
            background =
                AppThemeManager
                    .surfaceDrawable(
                        context = activity,
                        fill =
                            palette.surface,
                        radiusDp = 16,
                        accentStroke = true
                    )

            addView(
                TextView(activity).apply {
                    text = title
                    textSize = 12f
                    setTypeface(
                        typeface,
                        Typeface.BOLD
                    )
                    setTextColor(
                        palette.muted
                    )
                    setPadding(
                        dp(activity, 2),
                        0,
                        0,
                        dp(activity, 5)
                    )
                }
            )
        }
    }

    fun workflowButton(
        activity: Activity,
        label: String,
        primary: Boolean,
        action: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(activity)

        return Button(activity).apply {
            text = label
            isAllCaps = false
            textSize =
                if (primary) {
                    13.5f
                } else {
                    13f
                }
            if (primary) {
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
            }
            setTextColor(
                Color.WHITE
            )
            gravity =
                Gravity.CENTER
            maxLines = 2
            applyButtonIcon(
                activity = activity,
                button = this,
                label = label,
                tint =
                    palette.accent
            )
            setPadding(
                dp(activity, 9),
                dp(activity, 9),
                dp(activity, 9),
                dp(activity, 9)
            )
            compoundDrawablePadding =
                dp(activity, 6)

            UiChrome.autoSizeButton(
                this,
                minSp = 9,
                maxSp =
                    if (primary) {
                        13
                    } else {
                        13
                    }
            )

            background =
                if (primary) {
                    AppThemeManager
                        .accentButtonDrawable(
                            context = activity,
                            radiusDp = 12
                        )
                } else {
                    AppThemeManager
                        .neutralButtonDrawable(
                            context = activity,
                            radiusDp = 12
                        )
                }

            setOnClickListener {
                action()
            }
        }
    }

    fun compactButton(
        activity: Activity,
        label: String,
        action: () -> Unit
    ): Button =
        workflowButton(
            activity = activity,
            label = label,
            primary = false,
            action = action
        ).apply {
            textSize = 11f
            maxLines = 1
            minLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(false)
            setPadding(
                dp(activity, 5),
                dp(activity, 6),
                dp(activity, 5),
                dp(activity, 6)
            )
            compoundDrawablePadding =
                dp(activity, 4)

            resizeButtonStartIcon(
                activity = activity,
                button = this,
                sizeDp = 17
            )

            UiChrome.autoSizeButton(
                this,
                minSp = 8,
                maxSp = 11
            )
        }

    fun equalButtonsRow(
        activity: Activity,
        first: Button,
        second: Button
    ): LinearLayout =
        LinearLayout(activity).apply {
            orientation =
                LinearLayout.HORIZONTAL
            isBaselineAligned = false
            gravity =
                Gravity.CENTER_VERTICAL

            addView(
                first,
                LinearLayout.LayoutParams(
                    0,
                    dp(activity, 70),
                    1f
                )
            )

            addView(
                second,
                LinearLayout.LayoutParams(
                    0,
                    dp(activity, 70),
                    1f
                ).apply {
                    marginStart =
                        dp(activity, 6)
                }
            )
        }

    fun quickActionButton(
        activity: Activity,
        label: String,
        icon: Int,
        action: () -> Unit
    ): Button {
        val palette =
            AppThemeManager.palette(activity)

        return Button(activity).apply {
            text = label
            isAllCaps = false
            textSize = 12f
            maxLines = 2
            gravity =
                Gravity.CENTER
            setTypeface(
                typeface,
                Typeface.BOLD
            )
            setTextColor(
                palette.text
            )
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                0,
                0,
                0
            )
            resizeButtonStartIcon(
                activity = activity,
                button = this,
                sizeDp = 19
            )
            compoundDrawablePadding =
                dp(activity, 7)
            compoundDrawableTintList =
                ColorStateList.valueOf(
                    palette.accent
                )
            background =
                AppThemeManager
                    .neutralButtonDrawable(
                        context = activity,
                        radiusDp = 12
                    )
            setOnClickListener {
                action()
            }
        }
    }

    fun bottomNavigation(
        activity: Activity,
        onSearch: () -> Unit,
        onPlaylist: () -> Unit,
        onService: () -> Unit
    ): LinearLayout {
        val palette =
            AppThemeManager.palette(activity)

        val row =
            LinearLayout(activity).apply {
                orientation =
                    LinearLayout.HORIZONTAL
                isBaselineAligned = false
                setPadding(
                    dp(activity, 8),
                    dp(activity, 6),
                    dp(activity, 8),
                    dp(activity, 6)
                )
                background =
                    AppThemeManager
                        .surfaceDrawable(
                            context = activity,
                            fill =
                                palette.surface,
                            radiusDp = 16,
                            accentStroke = true
                        )
            }

        fun navButton(
            label: String,
            icon: Int,
            active: Boolean = false,
            action: () -> Unit
        ): Button =
            Button(activity).apply {
                text = label
                isAllCaps = false
                textSize = 10.5f
                maxLines = 1
                setSingleLine(true)
                gravity =
                    Gravity.CENTER
                setTextColor(
                    if (active) {
                        palette.accent
                    } else {
                        palette.muted
                    }
                )
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    icon,
                    0,
                    0
                )
                compoundDrawableTintList =
                    ColorStateList.valueOf(
                        if (active) {
                            palette.accent
                        } else {
                            palette.muted
                        }
                    )
                background =
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                setPadding(
                    dp(activity, 4),
                    dp(activity, 3),
                    dp(activity, 4),
                    dp(activity, 3)
                )
                setOnClickListener {
                    action()
                }
            }

        val items =
            listOf(
                navButton(
                    label = "Головна",
                    icon =
                        R.drawable.ic_ytm_home,
                    active = true,
                    action = {}
                ),
                navButton(
                    label = "Пошук",
                    icon =
                        R.drawable.ic_ytm_search,
                    action = onSearch
                ),
                navButton(
                    label = "Плейлист",
                    icon =
                        R.drawable.ic_ytm_playlist_add,
                    action = onPlaylist
                ),
                navButton(
                    label = "Сервіс",
                    icon =
                        R.drawable.ic_ytm_more,
                    action = onService
                )
            )

        items.forEach { button ->
            row.addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    dp(activity, 62),
                    1f
                )
            )
        }

        return row
    }

    private fun applyButtonIcon(
        activity: Activity,
        button: Button,
        label: String,
        tint: Int
    ) {
        val icon =
            buttonIconRes(label)

        if (icon == 0) {
            return
        }

        button
            .setCompoundDrawablesRelativeWithIntrinsicBounds(
                icon,
                0,
                0,
                0
            )

        resizeButtonStartIcon(
            activity = activity,
            button = button,
            sizeDp = 20
        )

        button.compoundDrawablePadding =
            dp(activity, 6)

        button.compoundDrawableTintList =
            ColorStateList.valueOf(
                tint
            )
    }

    private fun buttonIconRes(
        label: String
    ): Int =
        when {
            label.contains(
                "Google / YTM"
            ) ->
                R.drawable.ic_ytm_link

            label.contains(
                "Знайти"
            ) ->
                R.drawable.ic_ytm_search

            label.contains(
                "Створити"
            ) ->
                R.drawable.ic_ytm_playlist_add

            label.contains(
                "Імпорт"
            ) ->
                R.drawable.ic_ytm_download

            label.contains(
                "Історія"
            ) ->
                R.drawable.ic_ytm_history

            label.contains(
                "Черга"
            ) ->
                R.drawable.ic_ytm_queue

            label.contains(
                "Квота"
            ) ->
                R.drawable.ic_ytm_quota

            label.contains(
                "Ще"
            ) ||
                label.contains(
                    "Меню"
                ) ->
                R.drawable.ic_ytm_more

            else ->
                0
        }

    private fun resizeButtonStartIcon(
        activity: Activity,
        button: Button,
        sizeDp: Int
    ) {
        val drawables =
            button.compoundDrawablesRelative

        val start =
            drawables[0]
                ?: return

        val size =
            dp(
                activity,
                sizeDp
            )

        start.setBounds(
            0,
            0,
            size,
            size
        )

        button.setCompoundDrawablesRelative(
            start,
            drawables[1],
            drawables[2],
            drawables[3]
        )
    }

    private fun dp(
        activity: Activity,
        value: Int
    ): Int =
        (
            value *
                activity.resources
                    .displayMetrics
                    .density
        ).roundToInt()
}

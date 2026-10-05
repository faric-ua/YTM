package com.saney.ytmimporter.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.saney.ytmimporter.RecoveryCenterActivity
import com.saney.ytmimporter.recovery.RecoveryCenterSource
import java.util.WeakHashMap

object RecoveryAttentionChrome {
    private const val TAG =
        "recovery_attention_badge"

    private val animators =
        WeakHashMap<TextView, AnimatorSet>()

    private val acknowledgedCounts =
        WeakHashMap<TextView, Int>()

    fun attach(
        activity: Activity,
        header: LinearLayout,
        requestCode: Int
    ) {
        if (
            header.findViewWithTag<View>(
                TAG
            ) != null
        ) {
            return
        }

        val view =
            TextView(activity).apply {
                tag = TAG
                textSize = 13f
                gravity = Gravity.CENTER
                setTypeface(
                    typeface,
                    Typeface.BOLD
                )
                setTextColor(
                    Color.rgb(
                        255,
                        195,
                        80
                    )
                )
                setPadding(
                    dp(activity, 10),
                    dp(activity, 6),
                    dp(activity, 10),
                    dp(activity, 6)
                )
                background =
                    AppThemeManager
                        .surfaceDrawable(
                            context = activity,
                            fill =
                                AppThemeManager
                                    .palette(activity)
                                    .surfaceAlt,
                            radiusDp = 18,
                            accentStroke = true
                        )
                visibility = View.GONE

                setOnClickListener {
                    open(
                        activity,
                        requestCode
                    )
                }

                addOnAttachStateChangeListener(
                    object :
                        View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(
                            v: View
                        ) = Unit

                        override fun onViewDetachedFromWindow(
                            v: View
                        ) {
                            val badge =
                                v as? TextView
                                    ?: return

                            stopAnimation(
                                badge
                            )
                            acknowledgedCounts
                                .remove(badge)
                        }
                    }
                )
            }

        header.addView(
            view,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams
                    .WRAP_CONTENT,
                ViewGroup.LayoutParams
                    .WRAP_CONTENT
            ).apply {
                marginStart =
                    dp(activity, 8)
                marginEnd =
                    dp(activity, 8)
            }
        )

        update(
            activity = activity,
            view = view
        )
    }

    fun open(
        activity: Activity,
        requestCode: Int
    ) {
        activity.window
            .decorView
            .findViewWithTag<TextView>(
                TAG
            )
            ?.let { view ->
                acknowledgedCounts[
                    view
                ] =
                    RecoveryCenterSource(
                        activity
                    ).snapshot()
                        .actionableCount

                stopAnimation(view)
            }

        activity.startActivityForResult(
            Intent(
                activity,
                RecoveryCenterActivity
                    ::class.java
            ),
            requestCode
        )
    }

    fun refresh(
        activity: Activity
    ) {
        val view =
            activity.window
                .decorView
                .findViewWithTag<TextView>(
                    TAG
                )
                ?: return

        update(
            activity = activity,
            view = view
        )
    }

    private fun update(
        activity: Activity,
        view: TextView
    ) {
        val count =
            RecoveryCenterSource(
                activity
            ).snapshot()
                .actionableCount

        if (count <= 0) {
            stopAnimation(view)
            acknowledgedCounts
                .remove(view)
            view.visibility =
                View.GONE
            return
        }

        view.text =
            "⚠ " + count
        view.contentDescription =
            "Потребує уваги: " + count
        view.visibility =
            View.VISIBLE

        if (
            acknowledgedCounts[view] ==
            count
        ) {
            stopAnimation(view)
        } else {
            startAnimation(view)
        }
    }

    private fun startAnimation(
        view: TextView
    ) {
        if (
            !animatorsEnabled() ||
            animators[view]
                ?.isRunning == true
        ) {
            return
        }

        val scaleX =
            pulse(
                view,
                View.SCALE_X,
                1f,
                1.05f
            )

        val scaleY =
            pulse(
                view,
                View.SCALE_Y,
                1f,
                1.05f
            )

        val alpha =
            pulse(
                view,
                View.ALPHA,
                1f,
                0.86f
            )

        animators[view] =
            AnimatorSet().apply {
                playTogether(
                    scaleX,
                    scaleY,
                    alpha
                )
                start()
            }
    }

    private fun pulse(
        view: TextView,
        property:
            android.util.Property<View, Float>,
        from: Float,
        to: Float
    ): ObjectAnimator =
        ObjectAnimator
            .ofFloat(
                view,
                property,
                from,
                to
            )
            .apply {
                duration = 1400L
                repeatCount =
                    ValueAnimator.INFINITE
                repeatMode =
                    ValueAnimator.REVERSE
            }

    private fun stopAnimation(
        view: TextView
    ) {
        animators
            .remove(view)
            ?.cancel()

        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f
    }

    private fun animatorsEnabled():
        Boolean =
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            ValueAnimator.areAnimatorsEnabled()
        } else {
            true
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
        ).toInt()
}

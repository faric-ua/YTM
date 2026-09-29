package com.saney.ytmimporter.ui

import android.os.Bundle
import android.widget.ScrollView

class ScrollPositionState(
    private val stateKey: String
) {
    private var scrollY =
        0

    fun restore(
        savedInstanceState: Bundle?
    ) {
        scrollY =
            savedInstanceState
                ?.getInt(
                    stateKey,
                    0
                )
                ?: 0
    }

    fun capture(
        scrollView: ScrollView?
    ) {
        scrollY =
            scrollView
                ?.scrollY
                ?.coerceAtLeast(0)
                ?: scrollY
    }

    fun save(
        outState: Bundle,
        scrollView: ScrollView?
    ) {
        capture(
            scrollView
        )
        outState.putInt(
            stateKey,
            scrollY
        )
    }

    fun restoreInto(
        scrollView: ScrollView
    ) {
        val target =
            scrollY
                .coerceAtLeast(0)

        scrollView.post {
            scrollView.scrollTo(
                0,
                target
            )
        }
    }
}

package com.saney.ytmimporter.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Persists active text-selection ranges for selectable TextViews inside a rebuilt
 * user-facing surface.
 *
 * The state is presentation-only. Matching requires the same selectable-text
 * order and the same complete text value; changed content fails closed and leaves
 * the rebuilt TextView unselected.
 */
object SelectableTextState {
    private const val KEY_COUNT =
        "selectable_count"
    private const val KEY_TEXT_PREFIX =
        "selectable_text_"
    private const val KEY_START_PREFIX =
        "selectable_start_"
    private const val KEY_END_PREFIX =
        "selectable_end_"
    private const val KEY_FOCUSED_PREFIX =
        "selectable_focused_"

    fun capture(
        root: View?
    ): Bundle {
        val selectable =
            collectSelectableTextViews(
                root
            )

        return Bundle().apply {
            putInt(
                KEY_COUNT,
                selectable.size
            )

            selectable.forEachIndexed {
                    index,
                    textView ->
                putString(
                    KEY_TEXT_PREFIX + index,
                    textView.text
                        ?.toString()
                        .orEmpty()
                )
                putInt(
                    KEY_START_PREFIX + index,
                    textView.selectionStart
                )
                putInt(
                    KEY_END_PREFIX + index,
                    textView.selectionEnd
                )
                putBoolean(
                    KEY_FOCUSED_PREFIX + index,
                    textView.hasFocus()
                )
            }
        }
    }

    fun restore(
        root: View?,
        state: Bundle?
    ) {
        if (state == null) {
            return
        }

        val selectable =
            collectSelectableTextViews(
                root
            )
        val savedCount =
            state.getInt(
                KEY_COUNT,
                0
            )

        if (
            savedCount <= 0 ||
            savedCount !=
                selectable.size
        ) {
            return
        }

        selectable.forEachIndexed {
                index,
                textView ->
            val currentText =
                textView.text
                    ?.toString()
                    .orEmpty()
            val savedText =
                state.getString(
                    KEY_TEXT_PREFIX +
                        index
                )
                    ?: return@forEachIndexed

            if (
                currentText !=
                    savedText
            ) {
                return@forEachIndexed
            }

            val start =
                state.getInt(
                    KEY_START_PREFIX +
                        index,
                    -1
                )
            val end =
                state.getInt(
                    KEY_END_PREFIX +
                        index,
                    -1
                )
            val wasFocused =
                state.getBoolean(
                    KEY_FOCUSED_PREFIX +
                        index,
                    false
                )

            if (
                start < 0 ||
                end < start ||
                end >
                    currentText.length ||
                start == end
            ) {
                return@forEachIndexed
            }

            textView.post {
                val latestText =
                    textView.text
                        ?.toString()
                        .orEmpty()

                if (
                    latestText ==
                        savedText &&
                    end <=
                        latestText.length
                ) {
                    if (wasFocused) {
                        textView.requestFocus()
                    }
                    textView.setSelection(
                        start,
                        end
                    )
                }
            }
        }
    }

    private fun collectSelectableTextViews(
        root: View?
    ): List<TextView> {
        if (root == null) {
            return emptyList()
        }

        val result =
            mutableListOf<TextView>()

        fun visit(
            view: View
        ) {
            if (
                view is TextView &&
                view.isTextSelectable
            ) {
                result += view
            }

            if (
                view is ViewGroup
            ) {
                for (
                    index in
                    0 until
                        view.childCount
                ) {
                    visit(
                        view.getChildAt(
                            index
                        )
                    )
                }
            }
        }

        visit(root)
        return result
    }
}

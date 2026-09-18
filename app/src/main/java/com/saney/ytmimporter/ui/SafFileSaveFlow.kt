package com.saney.ytmimporter.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.saney.ytmimporter.storage.SafTreeAccess

object SafFileSaveFlow {
    fun show(
        activity: Activity,
        title: String,
        suggestedFileName: String,
        mimeType: String,
        addFolderRequestCode: Int,
        createDocumentRequestCode: Int,
        onRememberedRoot: (Uri) -> Unit
    ) {
        val roots =
            SafTreeAccess
                .persistedRoots(
                    context = activity,
                    access =
                        SafTreeAccess.Access.READ_WRITE
                )

        val actions =
            roots
                .map { root ->
                    UiChrome.MenuAction(
                        label =
                            buildString {
                                append(root.label)
                                append("\n")
                                append(
                                    "Зберегти: "
                                )
                                append(
                                    suggestedFileName
                                )
                            },
                        onClick = {
                            onRememberedRoot(
                                root.uri
                            )
                        }
                    )
                } +
                listOf(
                    UiChrome.MenuAction(
                        label =
                            "Додати папку для швидкого збереження…",
                        onClick = {
                            launchTreePicker(
                                activity =
                                    activity,
                                requestCode =
                                    addFolderRequestCode
                            )
                        }
                    ),
                    UiChrome.MenuAction(
                        label =
                            "Системне збереження / змінити ім’я…",
                        onClick = {
                            launchCreateDocument(
                                activity =
                                    activity,
                                requestCode =
                                    createDocumentRequestCode,
                                mimeType =
                                    mimeType,
                                suggestedFileName =
                                    suggestedFileName
                            )
                        }
                    )
                )

        UiChrome.showMenuDialog(
            activity = activity,
            title = title,
            subtitle =
                if (roots.isEmpty()) {
                    "Немає збережених папок для запису. " +
                        "Додайте папку, скористайтеся системним збереженням " +
                        "або натисніть «Скасувати»."
                } else {
                    "Виберіть збережену папку, додайте нову або " +
                        "відкрийте системне збереження для іншого місця чи імені."
                },
            actions = actions,
            negativeLabel =
                "Скасувати"
        )
    }

    fun launchTreePicker(
        activity: Activity,
        requestCode: Int
    ) {
        val intent =
            Intent(
                Intent.ACTION_OPEN_DOCUMENT_TREE
            ).apply {
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
            }

        activity.startActivityForResult(
            intent,
            requestCode
        )
    }

    fun launchCreateDocument(
        activity: Activity,
        requestCode: Int,
        mimeType: String,
        suggestedFileName: String
    ) {
        val intent =
            Intent(
                Intent.ACTION_CREATE_DOCUMENT
            ).apply {
                addCategory(
                    Intent.CATEGORY_OPENABLE
                )
                type =
                    mimeType
                putExtra(
                    Intent.EXTRA_TITLE,
                    suggestedFileName
                )
            }

        activity.startActivityForResult(
            intent,
            requestCode
        )
    }
}

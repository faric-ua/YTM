package com.saney.ytmimporter.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

object SafTreeFileWriter {
    data class Result(
        val uri: Uri,
        val fileName: String
    )

    fun writeText(
        context: Context,
        treeUri: Uri,
        preferredFileName: String,
        mimeType: String,
        content: String
    ): Result {
        SafTreeAccess.persist(
            context = context,
            treeUri = treeUri,
            access =
                SafTreeAccess.Access.READ_WRITE
        )

        val resolver =
            context.contentResolver

        val treeDocumentId =
            DocumentsContract
                .getTreeDocumentId(
                    treeUri
                )

        val parentUri =
            DocumentsContract
                .buildDocumentUriUsingTree(
                    treeUri,
                    treeDocumentId
                )

        val fileName =
            uniqueFileName(
                context = context,
                treeUri = treeUri,
                preferredFileName =
                    preferredFileName
            )

        val documentUri =
            DocumentsContract
                .createDocument(
                    resolver,
                    parentUri,
                    mimeType,
                    fileName
                )
                ?: error(
                    "Android не створив файл у вибраній папці"
                )

        try {
            resolver
                .openOutputStream(
                    documentUri,
                    "w"
                )
                ?.bufferedWriter(
                    Charsets.UTF_8
                )
                ?.use { writer ->
                    writer.write(content)
                }
                ?: error(
                    "Android не відкрив створений файл для запису"
                )
        } catch (error: Throwable) {
            runCatching {
                DocumentsContract
                    .deleteDocument(
                        resolver,
                        documentUri
                    )
            }

            throw error
        }

        return Result(
            uri = documentUri,
            fileName = fileName
        )
    }

    private fun uniqueFileName(
        context: Context,
        treeUri: Uri,
        preferredFileName: String
    ): String {
        val normalized =
            preferredFileName
                .trim()
                .ifBlank {
                    "YTM_Export.txt"
                }

        val existing =
            existingNames(
                context = context,
                treeUri = treeUri
            )

        if (
            existing.isEmpty() ||
            normalized !in existing
        ) {
            return normalized
        }

        val dot =
            normalized.lastIndexOf('.')

        val hasExtension =
            dot > 0 &&
                dot < normalized.lastIndex

        val base =
            if (hasExtension) {
                normalized.substring(
                    0,
                    dot
                )
            } else {
                normalized
            }

        val extension =
            if (hasExtension) {
                normalized.substring(dot)
            } else {
                ""
            }

        for (index in 2..999) {
            val candidate =
                "$base ($index)$extension"

            if (candidate !in existing) {
                return candidate
            }
        }

        return "$base (${System.currentTimeMillis()})$extension"
    }

    private fun existingNames(
        context: Context,
        treeUri: Uri
    ): Set<String> =
        runCatching {
            val treeDocumentId =
                DocumentsContract
                    .getTreeDocumentId(
                        treeUri
                    )

            val childrenUri =
                DocumentsContract
                    .buildChildDocumentsUriUsingTree(
                        treeUri,
                        treeDocumentId
                    )

            context
                .contentResolver
                .query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract
                            .Document
                            .COLUMN_DISPLAY_NAME
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    val index =
                        cursor.getColumnIndex(
                            DocumentsContract
                                .Document
                                .COLUMN_DISPLAY_NAME
                        )

                    if (index < 0) {
                        emptySet()
                    } else {
                        buildSet {
                            while (
                                cursor.moveToNext()
                            ) {
                                cursor
                                    .getString(index)
                                    ?.let(::add)
                            }
                        }
                    }
                }
                ?: emptySet()
        }.getOrDefault(
            emptySet()
        )
}

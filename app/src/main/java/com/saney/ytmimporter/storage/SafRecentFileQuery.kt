package com.saney.ytmimporter.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.util.Locale

object SafRecentFileQuery {
    data class Entry(
        val uri: Uri,
        val name: String,
        val mimeType: String?,
        val lastModified: Long,
        val size: Long?,
        val rootLabel: String
    )

    fun list(
        context: Context,
        roots: List<SafTreeAccess.Root>,
        allowedExtensions: Set<String>,
        limit: Int = 200
    ): List<Entry> {
        val normalizedExtensions =
            allowedExtensions
                .map {
                    it.trim()
                        .removePrefix(".")
                        .lowercase(Locale.ROOT)
                }
                .filter { it.isNotBlank() }
                .toSet()

        return roots
            .asSequence()
            .flatMap { root ->
                listRoot(
                    context = context,
                    root = root,
                    allowedExtensions = normalizedExtensions
                ).asSequence()
            }
            .distinctBy { it.uri.toString() }
            .sortedWith(
                compareByDescending<Entry> {
                    it.lastModified
                }.thenBy {
                    it.name.lowercase(Locale.ROOT)
                }
            )
            .take(limit.coerceAtLeast(1))
            .toList()
    }

    private fun listRoot(
        context: Context,
        root: SafTreeAccess.Root,
        allowedExtensions: Set<String>
    ): List<Entry> {
        val treeDocumentId =
            runCatching {
                DocumentsContract.getTreeDocumentId(root.uri)
            }.getOrNull()
                ?: return emptyList()

        val childrenUri =
            runCatching {
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    root.uri,
                    treeDocumentId
                )
            }.getOrNull()
                ?: return emptyList()

        val projection =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_SIZE
            )

        return runCatching {
            context.contentResolver
                .query(
                    childrenUri,
                    projection,
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    val idIndex =
                        cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID
                        )
                    val nameIndex =
                        cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                        )
                    val mimeIndex =
                        cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_MIME_TYPE
                        )
                    val modifiedIndex =
                        cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_LAST_MODIFIED
                        )
                    val sizeIndex =
                        cursor.getColumnIndex(
                            DocumentsContract.Document.COLUMN_SIZE
                        )

                    buildList {
                        while (cursor.moveToNext()) {
                            if (
                                idIndex < 0 ||
                                nameIndex < 0
                            ) {
                                continue
                            }

                            val documentId =
                                cursor.getString(idIndex)

                            val name =
                                cursor.getString(nameIndex)
                                    ?.trim()
                                    .orEmpty()

                            val mime =
                                if (
                                    mimeIndex >= 0 &&
                                    !cursor.isNull(mimeIndex)
                                ) {
                                    cursor.getString(mimeIndex)
                                } else {
                                    null
                                }

                            if (
                                name.isBlank() ||
                                mime ==
                                    DocumentsContract.Document.MIME_TYPE_DIR ||
                                !matchesExtension(
                                    name,
                                    allowedExtensions
                                )
                            ) {
                                continue
                            }

                            val uri =
                                DocumentsContract.buildDocumentUriUsingTree(
                                    root.uri,
                                    documentId
                                )

                            val lastModified =
                                if (
                                    modifiedIndex >= 0 &&
                                    !cursor.isNull(modifiedIndex)
                                ) {
                                    cursor.getLong(modifiedIndex)
                                } else {
                                    0L
                                }

                            val size =
                                if (
                                    sizeIndex >= 0 &&
                                    !cursor.isNull(sizeIndex)
                                ) {
                                    cursor.getLong(sizeIndex)
                                } else {
                                    null
                                }

                            add(
                                Entry(
                                    uri = uri,
                                    name = name,
                                    mimeType = mime,
                                    lastModified = lastModified,
                                    size = size,
                                    rootLabel = root.label
                                )
                            )
                        }
                    }
                }
                ?: emptyList()
        }.getOrElse {
            emptyList()
        }
    }

    private fun matchesExtension(
        name: String,
        allowedExtensions: Set<String>
    ): Boolean {
        if (allowedExtensions.isEmpty()) {
            return true
        }

        val extension =
            name
                .substringAfterLast('.', "")
                .lowercase(Locale.ROOT)

        return extension in allowedExtensions
    }
}

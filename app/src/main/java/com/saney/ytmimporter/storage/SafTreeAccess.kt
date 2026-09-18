package com.saney.ytmimporter.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

object SafTreeAccess {
    enum class Access {
        READ,
        READ_WRITE
    }

    data class Root(
        val uri: Uri,
        val label: String,
        val canWrite: Boolean
    )

    fun persistedRoots(
        context: Context,
        access: Access
    ): List<Root> =
        context
            .contentResolver
            .persistedUriPermissions
            .asSequence()
            .filter { permission ->
                DocumentsContract.isTreeUri(
                    permission.uri
                ) &&
                    permission.isReadPermission &&
                    (
                        access == Access.READ ||
                            permission.isWritePermission
                    )
            }
            .map { permission ->
                Root(
                    uri = permission.uri,
                    label =
                        displayName(
                            context,
                            permission.uri
                        ),
                    canWrite =
                        permission.isWritePermission
                )
            }
            .distinctBy {
                it.uri.toString()
            }
            .sortedWith(
                compareBy<Root> {
                    it.label.lowercase()
                }.thenBy {
                    it.uri.toString()
                }
            )
            .toList()

    fun persist(
        context: Context,
        treeUri: Uri,
        access: Access
    ) {
        val flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                if (
                    access ==
                        Access.READ_WRITE
                ) {
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                } else {
                    0
                }

        context
            .contentResolver
            .takePersistableUriPermission(
                treeUri,
                flags
            )
    }

    private fun displayName(
        context: Context,
        treeUri: Uri
    ): String {
        val treeDocumentId =
            runCatching {
                DocumentsContract
                    .getTreeDocumentId(
                        treeUri
                    )
            }.getOrNull()

        val documentUri =
            treeDocumentId
                ?.let {
                    runCatching {
                        DocumentsContract
                            .buildDocumentUriUsingTree(
                                treeUri,
                                it
                            )
                    }.getOrNull()
                }

        val queriedName =
            documentUri
                ?.let { uri ->
                    runCatching {
                        context
                            .contentResolver
                            .query(
                                uri,
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
                                if (
                                    cursor.moveToFirst()
                                ) {
                                    cursor.getString(0)
                                } else {
                                    null
                                }
                            }
                    }.getOrNull()
                }
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        if (queriedName != null) {
            return queriedName
        }

        val fallback =
            treeDocumentId
                ?.let {
                    Uri.decode(it)
                }
                ?.substringAfterLast(':')
                ?.substringAfterLast('/')
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        return fallback
            ?: "Дозволена папка"
    }
}

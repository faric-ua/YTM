package com.saney.ytmimporter.storage

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

object DirectDownloadFileQuery {
    fun list(
        context: Context,
        allowedExtensions: Set<String>,
        limit: Int = 200
    ): List<SafRecentFileQuery.Entry> {
        if (
            !AllFilesAccess.isRequired() ||
            !AllFilesAccess.isGranted()
        ) {
            return emptyList()
        }

        val normalizedExtensions =
            allowedExtensions
                .map {
                    it.trim()
                        .removePrefix(".")
                        .lowercase(Locale.ROOT)
                }
                .filter {
                    it.isNotBlank()
                }
                .toSet()

        @Suppress("DEPRECATION")
        val downloadDir =
            Environment
                .getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )

        val files =
            downloadDir
                .listFiles()
                ?.asSequence()
                ?.filter {
                    it.isFile &&
                        matchesExtension(
                            file = it,
                            allowedExtensions =
                                normalizedExtensions
                        )
                }
                ?.sortedWith(
                    compareByDescending<File> {
                        it.lastModified()
                    }.thenBy {
                        it.name.lowercase(
                            Locale.ROOT
                        )
                    }
                )
                ?.take(
                    limit.coerceAtLeast(1)
                )
                ?.toList()
                ?: emptyList()

        return files.mapNotNull { file ->
            runCatching {
                SafRecentFileQuery.Entry(
                    uri =
                        FileProvider
                            .getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            ),
                    name =
                        file.name,
                    mimeType =
                        null,
                    lastModified =
                        file.lastModified(),
                    size =
                        file.length(),
                    rootLabel =
                        "Download"
                )
            }.getOrNull()
        }
    }

    private fun matchesExtension(
        file: File,
        allowedExtensions: Set<String>
    ): Boolean {
        if (allowedExtensions.isEmpty()) {
            return true
        }

        val extension =
            file.extension
                .lowercase(
                    Locale.ROOT
                )

        return extension in
            allowedExtensions
    }
}

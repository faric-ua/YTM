package com.saney.ytmimporter.storage

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AccountBackupNaming {
    fun exportFolderName(
        date: Date = Date()
    ): String =
        "${timestamp(date)}-YTM-Export"

    fun syncFolderName(
        date: Date = Date()
    ): String =
        "${timestamp(date)}-YTM-Sync"

    fun consolidatedFolderName(
        date: Date = Date()
    ): String =
        "${timestamp(date)}-YTM-Full"

    private fun timestamp(
        date: Date
    ): String =
        SimpleDateFormat(
            "yyMMdd-HHmmss",
            Locale.US
        ).format(
            date
        )
}

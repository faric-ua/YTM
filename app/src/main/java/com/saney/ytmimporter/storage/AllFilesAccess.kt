package com.saney.ytmimporter.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings

object AllFilesAccess {
    fun isRequired(): Boolean =
        Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R

    fun isGranted(): Boolean =
        !isRequired() ||
            Environment
                .isExternalStorageManager()

    fun settingsIntent(
        context: Context
    ): Intent {
        if (!isRequired()) {
            return Intent(
                Settings.ACTION_SETTINGS
            )
        }

        val appIntent =
            Intent(
                Settings
                    .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse(
                    "package:${context.packageName}"
                )
            )

        return if (
            appIntent.resolveActivity(
                context.packageManager
            ) != null
        ) {
            appIntent
        } else {
            Intent(
                Settings
                    .ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )
        }
    }
}

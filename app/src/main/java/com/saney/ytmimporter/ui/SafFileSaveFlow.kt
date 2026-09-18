package com.saney.ytmimporter.ui

import android.app.Activity
import com.saney.ytmimporter.StorageChooserActivity

object SafFileSaveFlow {
    fun show(
        activity: Activity,
        title: String,
        suggestedFileName: String,
        mimeType: String,
        requestCode: Int
    ) {
        activity.startActivityForResult(
            StorageChooserActivity.saveIntent(
                activity = activity,
                title = title,
                suggestedFileName =
                    suggestedFileName,
                mimeType =
                    mimeType
            ),
            requestCode
        )
    }
}

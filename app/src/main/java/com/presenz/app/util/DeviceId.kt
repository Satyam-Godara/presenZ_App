package com.presenz.app.util

import android.content.Context
import android.provider.Settings

object DeviceId {

    fun get(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""
    }
}
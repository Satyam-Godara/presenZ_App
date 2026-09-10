package com.presenz.app

import android.app.Application
import com.presenz.app.util.Prefs

class PresenZApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}

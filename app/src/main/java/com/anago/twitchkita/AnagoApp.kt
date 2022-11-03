package com.anago.twitchkita

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO

class AnagoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
    }

    companion object {
        var application: Application? = null
        fun getAnagoApp(): Application? {
            return application
        }
    }
}
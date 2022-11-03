package com.anago.twitchkita.Util

import android.util.Log
import com.anago.twitchkita.BuildConfig

class Logger {
    companion object {
        private const val TAG: String = "anago-android"
        private val isDebug = BuildConfig.DEBUG
        private fun str(str: String?): String {
            return str ?: "null"
        }

        fun d(x: String) {
            if(isDebug)
                Log.d(TAG, str(x))
        }

        fun e(x: String) {
            if(isDebug)
                Log.e(TAG, str(x))
        }
    }
}
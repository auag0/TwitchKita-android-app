package com.anago.twitchkita

import android.content.Context
import com.anago.twitchkita.AnagoApp.Companion.getAnagoApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class DataManager {
    companion object {
        private val sharedPreferences by lazy {
            getAnagoApp()!!.getSharedPreferences(
                "saveData",
                Context.MODE_PRIVATE
            )
        }

        private const val streamers_key = "streamers"
        var streamers: ArrayList<String> = ArrayList()
        fun loadStreamers() {
            val tmp = sharedPreferences.getString(streamers_key, "")
            if (tmp == null || tmp.isEmpty()) {
                return
            }
            streamers = Gson().fromJson(tmp, object : TypeToken<ArrayList<String?>?>() {}.type)
        }

        fun saveStreamers() {
            sharedPreferences.edit().putString(streamers_key, Gson().toJson(streamers)).commit()
        }

        private const val latest_stream_key = "latest_stream"
        var latest_stream: ArrayList<String> = ArrayList()
        fun loadLatestStream() {
            val tmp = sharedPreferences.getString(latest_stream_key, "")
            if (tmp == null || tmp.isEmpty()) {
                return
            }
            latest_stream = Gson().fromJson(tmp, object : TypeToken<ArrayList<String?>?>() {}.type)
        }

        fun saveLatestStream() {
            sharedPreferences.edit().putString(latest_stream_key, Gson().toJson(latest_stream))
                .commit()
        }
    }
}
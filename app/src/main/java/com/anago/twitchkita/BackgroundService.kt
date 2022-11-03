package com.anago.twitchkita

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anago.twitchkita.DataManager.Companion.latest_stream
import com.anago.twitchkita.DataManager.Companion.loadLatestStream
import com.anago.twitchkita.DataManager.Companion.loadStreamers
import com.anago.twitchkita.DataManager.Companion.saveLatestStream
import com.anago.twitchkita.DataManager.Companion.streamers
import com.anago.twitchkita.Util.Logger
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.util.*


class BackgroundService : Service() {

    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null

    private fun notificationInitialize() {
        notificationManager = NotificationManagerCompat.from(this)

        val channel =
            NotificationChannel("TwitchKita", "TwitchKita", NotificationManager.IMPORTANCE_HIGH)
        notificationManager!!.createNotificationChannel(channel)

        builder = NotificationCompat.Builder(this, "TwitchKita").apply {
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setVibrate(longArrayOf(100, 500, 100, 500))
        }

    }

    fun notificatiom(title: String, text: String) {
        builder!!.apply {
            setContentTitle(title)
            setContentText(text)
            setWhen(System.currentTimeMillis())
        }
        notificationManager!!.notify(Random().nextInt(), builder!!.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun checkStreamers() {
        for (streamer in streamers) {
            val query = """
            query { 
                user(login: \"__displayName__\") { 
                    id 
                    login 
                    displayName 
                    stream { 
                        id 
                        title 
                        createdAt 
                        game { 
                            name 
                        } 
                    } 
                } 
            }
            """.replace("__displayName__", streamer).replace("    ", "").replace("\n", "")
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = RequestBody.create(mediaType, "{\"query\": \"$query\"}")
            val request: Request = Request.Builder().apply {
                post(requestBody)
                url("https://gql.twitch.tv/gql")
                header("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko")
            }.build()

            val okHttpClient = OkHttpClient()
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val json = JSONObject(response.body!!.string())
                    if (!json.isNull("extensions")) {
                        json.remove("extensions")
                    }
                    if (response.code != 200) {
                        Logger.e("失敗 ${response.code}\n${json.toString(4)}")
                        return
                    }
                    Logger.d("[$streamer] \n${json.toString(4)}")
                    val strJson = json.toString()
                    if (latest_stream.contains(strJson)) {
                        return
                    }
                    latest_stream.add(strJson)
                    saveLatestStream()

                    if (json.isNull("data")) {
                        Logger.d("[$streamer] データの取得に失敗しました")
                        return
                    }
                    val data = json.getJSONObject("data") ?: return
                    if (data.isNull("user")) {
                        Logger.d("[$streamer] ユーザーが存在しません")
                        return
                    }
                    val user = data.getJSONObject("user")
                    if (user.isNull("stream")) {
                        Logger.d("[$streamer] オフライン")
                        return
                    }
                    val login = user.getString("login")
                    val displayName = user.getString("displayName")
                    val stream = user.getJSONObject("stream")

                    val id = stream.getString("id")
                    val title = stream.getString("title")
                    val createdAt = stream.getString("createdAt")
                    val gameName = stream.getJSONObject("game").getString("name") ?: "null"

                    notificatiom(title, gameName)
                    Logger.d("$title: $gameName")
                }

                override fun onFailure(call: Call, e: IOException) {
                    Logger.e("接続に失敗\n${e.message}")
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationInitialize()
        loadStreamers()
        loadLatestStream()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                checkStreamers()
            }
        }, 0, 60000)

        return START_STICKY
    }
}
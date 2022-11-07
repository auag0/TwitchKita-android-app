package com.anago.twitchkita.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anago.twitchkita.DataManager
import com.anago.twitchkita.DataManager.Companion.loadLatestStream
import com.anago.twitchkita.DataManager.Companion.loadStreamers
import com.anago.twitchkita.DataManager.Companion.streamers
import com.anago.twitchkita.MainActivity
import com.anago.twitchkita.R
import com.anago.twitchkita.TwitchAPI.Companion.getUserInfo
import com.anago.twitchkita.Util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*


class BackgroundService : Service() {

    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null

    private fun notificationInitialize() {
        notificationManager = NotificationManagerCompat.from(this)

        notificationManager!!.createNotificationChannel(
            NotificationChannel(
                "TwitchKita",
                "TwitchKita",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        builder = NotificationCompat.Builder(this, "TwitchKita").apply {
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_DEFAULT
        }

        notificationManager!!.createNotificationChannel(
            NotificationChannel(
                "HelloWorld",
                "HelloWorld",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val intent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notification = NotificationCompat.Builder(this, "HelloWorld")
            .setSmallIcon(R.drawable.ic_baseline_android)
            .setContentTitle("TwitchKita")
            .setContentText("バックグラウンドで実行中です。")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(intent)
            .build()
        startForeground(Random().nextInt(), notification)
    }

    fun notificatiom(title: String, text: String) {
        builder!!.apply {
            setContentTitle(title)
            setContentText(text)
            setWhen(System.currentTimeMillis())
            setVibrate(longArrayOf(0, 2500, 1000, 2500))
        }
        notificationManager!!.notify(Random().nextInt(), builder!!.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun checkStreamers() {
        for (streamer in streamers) {
            getUserInfo(streamer, object : Callback {
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
                    if (DataManager.latest_stream.contains(strJson)) {
                        return
                    }
                    DataManager.latest_stream.add(strJson)
                    DataManager.saveLatestStream()

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
        }, 0, 300000)

        return START_STICKY
    }
}
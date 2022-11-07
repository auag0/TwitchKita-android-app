package com.anago.twitchkita.Adapter

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anago.twitchkita.AnagoApp.Companion.getAnagoApp
import com.anago.twitchkita.DataManager.Companion.saveStreamers
import com.anago.twitchkita.DataManager.Companion.streamers
import com.anago.twitchkita.R
import com.anago.twitchkita.TwitchAPI.Companion.getProfileURL
import com.anago.twitchkita.Util.Logger
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class UserAdapter :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View, userAdapter: UserAdapter) : RecyclerView.ViewHolder(view) {
        val iconImage: ImageView
        val nameText: TextView
        private val deleteBtn: ImageView

        init {
            iconImage = view.findViewById(R.id.icon)
            nameText = view.findViewById(R.id.name)
            deleteBtn = view.findViewById(R.id.delete)

            deleteBtn.setOnClickListener {
                streamers.remove(streamers[adapterPosition])
                userAdapter.notifyItemRemoved(adapterPosition)
                saveStreamers()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_user, viewGroup, false)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getProfileURL(streamers[position], object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val profileImageURL: String
                try {
                    profileImageURL = JSONObject(response.body!!.string()).getJSONObject("data")
                        .getJSONObject("user").getString("profileImageURL")
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
                val handler = Handler(getAnagoApp()!!.mainLooper)
                handler.post {
                    Glide
                        .with(viewHolder.iconImage.context)
                        .load(profileImageURL)
                        .into(viewHolder.iconImage)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Logger.d("プロフィールの取得に失敗しました。")
            }
        })
        viewHolder.nameText.text = streamers[position]
    }

    override fun getItemCount() = streamers.size
}

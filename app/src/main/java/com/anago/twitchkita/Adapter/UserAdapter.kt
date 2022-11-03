package com.anago.twitchkita.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anago.twitchkita.DataManager.Companion.saveStreamers
import com.anago.twitchkita.DataManager.Companion.streamers
import com.anago.twitchkita.R

class UserAdapter :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View, userAdapter: UserAdapter) : RecyclerView.ViewHolder(view) {
        val nameText: TextView
        private val deleteBtn: ImageView

        init {
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
        viewHolder.nameText.text = streamers[position]
    }

    override fun getItemCount() = streamers.size
}

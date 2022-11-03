package com.anago.twitchkita

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anago.twitchkita.Adapter.UserAdapter
import com.anago.twitchkita.DataManager.Companion.loadStreamers
import com.anago.twitchkita.DataManager.Companion.saveStreamers
import com.anago.twitchkita.DataManager.Companion.streamers

class MainActivity : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    private var nameEdit: EditText? = null
    private var addBtn: Button? = null
    private var startBtn: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadStreamers()
        setUpView()

        addBtn!!.setOnClickListener {
            val name = nameEdit!!.text.toString()
            if (name.isEmpty() || name.length < 3)
                return@setOnClickListener
            streamers.add(name)
            recyclerView!!.adapter!!.notifyItemInserted(streamers.size)
            saveStreamers()
        }

        startBtn!!.setOnClickListener {
            startService(Intent(this, BackgroundService::class.java))
        }
    }

    private fun setUpView() {
        nameEdit = findViewById(R.id.name)
        addBtn = findViewById(R.id.add)
        startBtn = findViewById(R.id.start)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.users)
        recyclerView!!.layoutManager = linearLayoutManager
        recyclerView!!.adapter = UserAdapter()
    }
}
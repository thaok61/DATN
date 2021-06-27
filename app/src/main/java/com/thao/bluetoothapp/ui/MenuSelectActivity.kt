package com.thao.bluetoothapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.thao.bluetoothapp.R

class MenuSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_select)

        val bluetoothButton = findViewById<Button>(R.id.buttonBluetooth)
        val videoCallButton = findViewById<Button>(R.id.buttonVideoCall)

        bluetoothButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        videoCallButton.setOnClickListener {
            val intent = Intent(this, MainWebRTCActivity::class.java)
            startActivity(intent)
        }
    }



}
package com.thao.bluetoothapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Device(
    val name: String,
    val type: String,
    val micStatus: Boolean,
    val soundStatus: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null
)
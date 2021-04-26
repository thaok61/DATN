package com.thao.bluetoothapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.thao.bluetoothapp.data.model.Device

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device")
    fun getAll(): LiveData<MutableList<Device>>

    @Insert
    suspend fun insertAll(vararg devices: Device)

    @Delete
    suspend fun delete(vararg device: Device)

    @Update
    suspend fun update(history: Device)

    @Query("SELECT * FROM device WHERE id = :id")
    fun getById(id: Int): LiveData<Device>

}
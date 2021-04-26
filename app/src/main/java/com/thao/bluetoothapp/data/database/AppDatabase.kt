package com.thao.bluetoothapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thao.bluetoothapp.data.dao.DeviceDao
import com.thao.bluetoothapp.data.model.Device

@Database(entities = [Device::class], version = 0)
abstract class AppDatabase: RoomDatabase() {
    abstract fun deviceDao(): DeviceDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "sleep_history_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
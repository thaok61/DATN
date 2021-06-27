package com.thao.bluetoothapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DeviceViewModel : ViewModel() {
    var isConnected = MutableLiveData<Boolean>().apply {
        value = false
    }
}
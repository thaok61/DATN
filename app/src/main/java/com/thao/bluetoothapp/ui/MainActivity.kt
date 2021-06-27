package com.thao.bluetoothapp.ui


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paramsen.noise.Noise
import com.thao.bluetoothapp.R
import com.thao.bluetoothapp.databinding.ActivityMainBinding
import com.thao.bluetoothapp.utils.TAG
import com.thao.bluetoothapp.viewmodel.DeviceViewModel


class MainActivity : AppCompatActivity() {
    private val deviceViewModel: DeviceViewModel by viewModels()
    lateinit var binding: ActivityMainBinding

    private val bluetoothConnectionResult = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    deviceViewModel.isConnected.value = true
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    deviceViewModel.isConnected.value = false
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        deviceViewModel.isConnected.value = false
                    }
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothConnectionResult, filter)
        val listHistoryDeviceFragment = ListHistoryDeviceFragment()
        val listScanDeviceFragment = ListScanDeviceFragment()
        val controlPanelFragment = DeviceControlPanelFragment()

        makeCurrentFragment(listScanDeviceFragment)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.scan -> makeCurrentFragment(listScanDeviceFragment)
                R.id.history -> makeCurrentFragment(listHistoryDeviceFragment)
                R.id.control -> {
                    if (deviceViewModel.isConnected.value == true) {
                        makeCurrentFragment(controlPanelFragment)
                    } else {
                        Toast.makeText(this, "DEVICE IS NOT CONNECTED", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }

        val floatArray = floatArrayOf(1f, 0f, 2f, 0f, 3f, 0f, 4f, 0f)
        val noise = Noise.imaginary(8)
        val fft = noise.fft(floatArray, FloatArray(8))
        fft.forEach {
            Log.d(TAG, "$it: FFT")
        }

        for (i in 0 until fft.size / 2) {
            fft[i * 2 + 1] = fft[i * 2 + 1] * -1
        }
        val deFFT= noise.fft(fft, FloatArray(8))
        deFFT.forEach {
            Log.d(TAG, "${it*2 / fft.size}: IFFT")
        }



    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    override fun onStop() {
        super.onStop()
        deviceViewModel.isConnected.value = false
    }


}
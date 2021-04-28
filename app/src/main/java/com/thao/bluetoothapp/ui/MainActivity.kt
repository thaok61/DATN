package com.thao.bluetoothapp.ui


import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.thao.bluetoothapp.R
import com.thao.bluetoothapp.databinding.ActivityMainBinding
import com.thao.bluetoothapp.utils.PERMISSION_REQUEST_CODE


class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val listHistoryDeviceFragment = ListHistoryDeviceFragment()
        val listScanDeviceFragment = ListScanDeviceFragment()
        val controlPanelFragment = DeviceControlPanelFragment()

        makeCurrentFragment(listScanDeviceFragment)

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.scan -> makeCurrentFragment(listScanDeviceFragment)
                R.id.history -> makeCurrentFragment(listHistoryDeviceFragment)
                R.id.control -> makeCurrentFragment(controlPanelFragment)
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }


}
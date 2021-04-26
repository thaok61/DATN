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

        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                Toast.makeText(this, "GRANTED READ CONTACTS", Toast.LENGTH_SHORT).show()

            }

            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(this, "Granted All", Toast.LENGTH_SHORT).show()
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_REQUEST_CODE
                    )
                }
                return
            }
        }
    }
}
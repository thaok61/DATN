package com.thao.bluetoothapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.thao.bluetoothapp.IBluetoothA2dp
import com.thao.bluetoothapp.adapter.ScanDeviceAdapter
import com.thao.bluetoothapp.databinding.FragmentListScanDeviceBinding
import com.thao.bluetoothapp.utils.BT_MODULE_UUID
import com.thao.bluetoothapp.utils.ENABLE_BLUETOOTH
import com.thao.bluetoothapp.utils.REQUEST_ACCESS_FINE_LOCATION
import com.thao.bluetoothapp.utils.REQUEST_ENABLE_DISCOVERY
import java.lang.reflect.Method


class ListScanDeviceFragment : Fragment(), ScanDeviceAdapter.OnItemClickListener {

    private var bluetoothHeadset: BluetoothHeadset? = null
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val deviceListAdapter = ScanDeviceAdapter()
    private var device: BluetoothDevice? = null
    private lateinit var binding: FragmentListScanDeviceBinding
    private var b: IBinder? = null
    private lateinit var a2dp: BluetoothA2dp  //class to connect to an A2dp device
    private lateinit var ia2dp: IBluetoothA2dp

    private var mIsA2dpReady = false
    fun setIsA2dpReady(ready: Boolean) {
        mIsA2dpReady = ready
    }

    /* Broadcast receiver to listen for discovery results. */
    private val bluetoothDiscoveryResult = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                deviceListAdapter.addDevice(device)
            }
        }
    }

    /* Broadcast receiver to listen for discovery updates. */
    private val bluetoothDiscoveryMonitor: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    Toast.makeText(requireContext(), "Scan started...", Toast.LENGTH_SHORT).show()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(
                        requireContext(),
                        "Scan complete. Found ${deviceListAdapter.itemCount} devices.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListScanDeviceBinding.inflate(layoutInflater)
        registerBluetoothDevice()
        initUI()
        return binding.root
    }

    private fun initUI() {
        deviceListAdapter.onItemClickListener = this
        binding.recyclerViewScanDevice.apply {
            adapter = deviceListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            initBluetooth()
        }
    }

    private fun initBluetooth() {
        if (bluetoothAdapter.isDiscovering) return

        if (bluetoothAdapter.isEnabled) {
            enableDiscovery()
        } else {
            // Bluetooth isn't enabled - prompt user to turn it on
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, ENABLE_BLUETOOTH)
        }
    }

    private fun enableDiscovery() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        startActivityForResult(intent, REQUEST_ENABLE_DISCOVERY)
    }

    private fun registerBluetoothDevice() {
        requireActivity().registerReceiver(
            bluetoothDiscoveryResult,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        requireActivity().registerReceiver(
            bluetoothDiscoveryMonitor,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        )
        requireActivity().registerReceiver(
            bluetoothDiscoveryMonitor,
            IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        )
    }


    private fun startDiscovery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isEnabled && !bluetoothAdapter.isDiscovering) {
                beginDiscovery()
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun beginDiscovery() {
        deviceListAdapter.clearDevice()
        bluetoothAdapter.startDiscovery()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beginDiscovery()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission required to scan for devices",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ENABLE_BLUETOOTH -> if (resultCode == Activity.RESULT_OK) {
                enableDiscovery()
            }

            REQUEST_ENABLE_DISCOVERY -> if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Discovery cancelled", Toast.LENGTH_SHORT).show()
            } else {
                startDiscovery()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(bluetoothDiscoveryMonitor)
        requireActivity().unregisterReceiver(bluetoothDiscoveryResult)
    }

    override fun onItemClick(device: BluetoothDevice) {
        if (!device.createBond()) connectUsingBluetoothA2dp(device)
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    private fun connectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice) {
        try {
            val c2 = Class.forName("android.os.ServiceManager")
            val m2: Method = c2.getDeclaredMethod("getService", String::class.java)
            b = m2.invoke(c2.newInstance(), "bluetooth_a2dp") as IBinder?
            if (b == null) {
                // For Android 4.2 Above Devices
                device = deviceToConnect
                //establish a connection to the profile proxy object associated with the profile
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(
                    requireContext(),
                    // listener notifies BluetoothProfile clients when they have been connected to or disconnected from the service
                    object : BluetoothProfile.ServiceListener {
                        override fun onServiceDisconnected(profile: Int) {
                            setIsA2dpReady(false)
                            disConnectUsingBluetoothA2dp(deviceToConnect)
                        }

                        override fun onServiceConnected(
                            profile: Int,
                            proxy: BluetoothProfile
                        ) {
                            a2dp = proxy as BluetoothA2dp
                            try {
                                //establishing bluetooth connection with A2DP devices
                                a2dp.javaClass
                                    .getMethod("connect", BluetoothDevice::class.java)
                                    .invoke(a2dp, deviceToConnect)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            setIsA2dpReady(true)
                        }
                    }, BluetoothProfile.A2DP
                )
            } else {
                val c3 =
                    Class.forName("android.bluetooth.IBluetoothA2dp")
                val s2 = c3.declaredClasses
                val c = s2[0]
                val m: Method = c.getDeclaredMethod("asInterface", IBinder::class.java)
                m.isAccessible = true
                ia2dp = m.invoke(null, b) as IBluetoothA2dp
                ia2dp.connect(deviceToConnect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disConnectUsingBluetoothA2dp(device: BluetoothDevice) {
        try {
            // For Android 4.2 Above Devices
            if (b == null) {
                try {
                    //disconnecting bluetooth device
                    a2dp.javaClass.getMethod(
                        "disconnect",
                        BluetoothDevice::class.java
                    ).invoke(a2dp, device)
                    BluetoothAdapter.getDefaultAdapter()
                        .closeProfileProxy(BluetoothProfile.A2DP, a2dp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                ia2dp.disconnect(device)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
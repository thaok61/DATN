package com.thao.bluetoothapp.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.thao.bluetoothapp.adapter.HistoryDeviceAdapter
import com.thao.bluetoothapp.databinding.FragmentListHistoryDeviceBinding

class ListHistoryDeviceFragment : Fragment(), HistoryDeviceAdapter.OnItemClickListener {
    private lateinit var launchRequestEnableBluetooth: ActivityResultLauncher<Intent>
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var listDevice= MutableLiveData<MutableList<BluetoothDevice>>()
    private lateinit var binding: FragmentListHistoryDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchRequestEnableBluetooth = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d("TAG", "onActivityResult: ENABLE BLUETOOTH")
            } else {
                Log.d("TAG", "onActivityResult: DISABLE BLUETOOTH")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentListHistoryDeviceBinding.inflate(layoutInflater)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Log.d("TAG", "Device doesn't support Bluetooth")
        }
        if (bluetoothAdapter?.isEnabled == false) {
            // Bluetooth isn't enabled - prompt user to turn it on
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            launchRequestEnableBluetooth.launch(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            if (pairedDevices != null) {
                listDevice.value = pairedDevices.toMutableList()
            }
        }

        listDevice.observe(viewLifecycleOwner, {
            binding.recyclerViewHistoryDevice.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = HistoryDeviceAdapter(it).apply {
                    onItemClickListener = this@ListHistoryDeviceFragment
                }
            }
        })
    }

    override fun onItemClick(device: BluetoothDevice) {
        pairDevice(device)
    }

    private fun pairDevice(device: BluetoothDevice) {
        try {
            device.createBond()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
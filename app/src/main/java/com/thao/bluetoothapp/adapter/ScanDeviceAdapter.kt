package com.thao.bluetoothapp.adapter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.thao.bluetoothapp.databinding.ItemDeviceScanBinding
import com.thao.bluetoothapp.data.model.Device
import com.thao.bluetoothapp.utils.BT_MODULE_UUID
import com.thao.bluetoothapp.utils.TAG
import java.lang.Exception
import java.util.*

class ScanDeviceAdapter :
    RecyclerView.Adapter<ScanDeviceAdapter.DeviceViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(device: BluetoothDevice)
    }
    inner class DeviceViewHolder(private val binding: ItemDeviceScanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            binding.device = device

            binding.cardScan.setOnClickListener {
                onItemClickListener?.onItemClick(device)
            }
        }
    }
    
    var onItemClickListener: OnItemClickListener? = null

    private var listDevice = mutableListOf<BluetoothDevice>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceScanBinding.inflate(layoutInflater, parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(listDevice[position])
    }

    fun addDevice(device: BluetoothDevice) {
        listDevice.add(device)
        notifyItemInserted(itemCount)
    }

    fun clearDevice() {
        listDevice.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listDevice.size
}
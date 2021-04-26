package com.thao.bluetoothapp.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thao.bluetoothapp.data.model.Device
import com.thao.bluetoothapp.databinding.ItemDeviceHistoryBinding
import java.util.*

class HistoryDeviceAdapter(private var listDevice: MutableList<BluetoothDevice>) : RecyclerView.Adapter<HistoryDeviceAdapter.DeviceViewHolder>() {
    interface OnItemClickListener {
        fun onItemClick(device: BluetoothDevice)
    }

    var onItemClickListener: OnItemClickListener? = null

    inner class DeviceViewHolder(private val binding: ItemDeviceHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            binding.device = device

            binding.cardHistory.setOnClickListener {
                onItemClickListener?.onItemClick(device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDeviceHistoryBinding.inflate(layoutInflater, parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(listDevice[position])
    }

    override fun getItemCount(): Int = listDevice.size

}
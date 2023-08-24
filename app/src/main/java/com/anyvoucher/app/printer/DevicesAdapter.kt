package com.anyvoucher.app.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anyvoucher.app.R


class DevicesAdapter(private val devices: ArrayList<BluetoothDevice>, private val btListener: OnSelectDeviceListener) :
    RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_list, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[holder.adapterPosition]
        holder.deviceNameTV.text = device.name
        holder.deviceAddressTV.text = device.address

        holder.selectButton.setOnClickListener {
            btListener.onSelect(device)
        }
    }

/*
    private fun resolveMajorDeviceClass(majorBtClass: Int): String {
        return when (majorBtClass) {
            BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio/ Video"
            BluetoothClass.Device.Major.COMPUTER -> "Computer"
            BluetoothClass.Device.Major.HEALTH -> "Health"
            BluetoothClass.Device.Major.IMAGING -> "Imaging"
            BluetoothClass.Device.Major.MISC -> "Misc"
            BluetoothClass.Device.Major.NETWORKING -> "Networking"
            BluetoothClass.Device.Major.PERIPHERAL -> "Peripheral"
            BluetoothClass.Device.Major.PHONE -> "Phone"
            BluetoothClass.Device.Major.TOY -> "Toy"
            BluetoothClass.Device.Major.UNCATEGORIZED -> "Uncategorized"
            BluetoothClass.Device.Major.WEARABLE -> "Wearable"
            else -> "Unknown ($majorBtClass)"
        }
    }
*/

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceNameTV: TextView = itemView.findViewById(R.id.deviceNameTV)
        val deviceAddressTV: TextView = itemView.findViewById(R.id.deviceAddressTV)
        val selectButton: Button = itemView.findViewById(R.id.selectButton)

    }

    interface OnSelectDeviceListener {
        fun onSelect(btDevice: BluetoothDevice)

    }

}
package com.arsinde.weatherapp.features.ble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arsinde.weatherapp.R
import kotlinx.android.synthetic.main.card_bluetooth_device_info.view.*

class BluetoothDevicesAdapter(
    private val deviceInfos: List<BluetoothDeviceInfo>,
    private val listener: BleClickListener
) :
    RecyclerView.Adapter<BluetoothDevicesAdapter.BluetoothDeviceViewHolder>() {

    class BluetoothDeviceViewHolder(private val btdView: View) : RecyclerView.ViewHolder(btdView) {

        fun bind(data: BluetoothDeviceInfo, listener: BleClickListener) {
            btdView.tvTitle.text = data.deviceName
            btdView.tvSubTitle.text = data.deviceAddress
            btdView.setOnClickListener {
                listener.onClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_bluetooth_device_info, parent, false)
        return BluetoothDeviceViewHolder(card)
    }

    override fun getItemCount(): Int = deviceInfos.size

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.bind(deviceInfos[position], listener)
    }
}
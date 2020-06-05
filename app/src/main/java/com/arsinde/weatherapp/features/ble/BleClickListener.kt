package com.arsinde.weatherapp.features.ble

class BleClickListener(val clickListener: (BluetoothDeviceInfo) -> Unit) {
    fun onClick(deviceInfo: BluetoothDeviceInfo) = clickListener(deviceInfo)
}
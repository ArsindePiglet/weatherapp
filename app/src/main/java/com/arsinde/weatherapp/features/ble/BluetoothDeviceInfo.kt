package com.arsinde.weatherapp.features.ble

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceInfo(
    val deviceName: String,
    val deviceAddress: String,
    val device: BluetoothDevice
)
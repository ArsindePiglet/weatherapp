package com.arsinde.weatherapp.models.ble

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arsinde.weatherapp.features.ble.BluetoothDeviceInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BleViewModel(private val app: Application) : AndroidViewModel(app) {

    val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val _deviceList = mutableSetOf<BluetoothDeviceInfo>()
    val leDevices = MutableLiveData<Set<BluetoothDeviceInfo>>()

    val serviceResponse = MutableLiveData<String>()

    fun scanLeDevices() {
        viewModelScope.launch {
            startLEScan {
                it?.let { device ->
                    if (device.name != null) {
                        _deviceList.add(
                            BluetoothDeviceInfo(
                                device.name,
                                device.address,
                                device
                            )
                        )
                    }
                }
            }
            delay(5000)
            stopLEScan()
            leDevices.postValue(_deviceList)
        }
    }

    private fun enableBluetooth(context: Context) { //private fun enableBluetooth(context: Context, onBtEnabledAction: Runnable)
        val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                println("Bluetooth is enabled now!")
                //TODO result onBtEnabledAction.run()
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        if (!BluetoothAdapter.getDefaultAdapter().enable()) {
            println("Bluetooth is enabled!")
            context.unregisterReceiver(bluetoothStateReceiver)
        }
    }

    private var leScanCallback: ScanCallback? = null

    private fun startLEScan(doOnDeviceFound: (BluetoothDevice?) -> Unit) {
        leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                doOnDeviceFound(result.device)
            }
        }
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.startScan(leScanCallback)
    }

    private fun stopLEScan() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(leScanCallback)
        leScanCallback = null
    }
}

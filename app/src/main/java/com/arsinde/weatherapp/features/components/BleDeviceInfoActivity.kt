package com.arsinde.weatherapp.features.components

import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.SimpleExpandableListAdapter
import androidx.fragment.app.FragmentActivity
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.services.*
import kotlinx.android.synthetic.main.ble_device_info_dialog_activity.*

const val DEVICE_NAME = "device_name"
const val DEVICE_ADDRESS = "device_address"
private const val TAG = "BleDeviceInfoActivity"

class BleDeviceInfoActivity : FragmentActivity() {

    private var bluetoothLeService: BluetoothLeService? = null
    private var isConnected = false
    private lateinit var bluetoothLeServices: BluetoothLeService

    private val deviceName by lazy { intent.getStringExtra(DEVICE_NAME) }
    private val deviceAddress by lazy { intent.getStringExtra(DEVICE_ADDRESS) }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothLeService = null
            isConnected = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothLeService?.connect(deviceAddress)
            isConnected = true
        }

    }

    private val gattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when(action) {
                ACTION_GATT_CONNECTED -> {
                    isConnected = true
                    updateConnectionState(R.string.connected)
                }
                ACTION_GATT_DISCONNECTED -> {
                    isConnected = false
                    updateConnectionState(R.string.disconnected)
                    clearUI()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> displayGattServices(bluetoothLeServices.getSupportedGattServices())
                ACTION_DATA_AVAILABLE -> displayData(intent.getStringExtra(EXTRA_DATA))
            }
        }

    }

    private fun displayData(data: String?) {
        data?.let {
            data_value.text = it
        }
    }

    private fun displayGattServices(supportedGattServices: List<BluetoothGattService?>?) {
        //TODO("Not yet implemented")
    }

    private fun clearUI() {
        gatt_services_list.setAdapter(null as? SimpleExpandableListAdapter)
        data_value.setText(R.string.no_data)
    }

    private fun updateConnectionState(resId: Int) {
        runOnUiThread {
            connection_state.setText(resId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_device_info_dialog_activity)

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStart() {
        super.onStart()
        device_name.text = deviceName
        device_address.text = deviceAddress
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (bluetoothLeService != null) {
            val result = bluetoothLeService?.connect(deviceAddress)
            Log.d(
                TAG,
                "Connect request result=$result"
            )
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        bluetoothLeService = null
    }


    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_GATT_CONNECTED)
        intentFilter.addAction(ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(ACTION_DATA_AVAILABLE)
        return intentFilter
    }
}
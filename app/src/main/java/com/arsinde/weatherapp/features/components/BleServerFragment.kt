package com.arsinde.weatherapp.features.components

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arsinde.weatherapp.BuildConfig
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.services.TimeProfile
import kotlinx.android.synthetic.main.settings_fragment.*
import java.util.*

private const val TAG = "SettingsFragment"

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    /* Collection of notification subscribers */
    private val registeredDevices = mutableSetOf<BluetoothDevice>()
    private var bluetoothGattServer: BluetoothGattServer? = null

    /* Bluetooth API */
    private lateinit var bluetoothManager: BluetoothManager

    /**
     * Listens for system time changes and triggers a notification to
     * Bluetooth subscribers.
     */
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val adjustReason = when (intent.action) {
                Intent.ACTION_TIME_CHANGED -> TimeProfile.ADJUST_MANUAL
                Intent.ACTION_TIMEZONE_CHANGED -> TimeProfile.ADJUST_TIMEZONE
                Intent.ACTION_TIME_TICK -> TimeProfile.ADJUST_NONE
                else -> TimeProfile.ADJUST_NONE
            }
            val now = System.currentTimeMillis()
            notifyRegisteredDevices(now, adjustReason)
            updateLocalUi(now)
        }
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                BluetoothAdapter.STATE_ON -> {
                    startAdvertising()
                    startServer()
                }
                BluetoothAdapter.STATE_OFF -> {
                    stopServer()
                    stopAdvertising()
                }
            }
        }
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "LE Advertise Started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.w(TAG, "LE Advertise Failed: $errorCode")
        }
    }

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "BluetoothDevice CONNECTED: $device")
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "BluetoothDevice DISCONNECTED: $device")
                }
                registeredDevices.remove(device)
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            val now = System.currentTimeMillis()
            when {
                TimeProfile.CURRENT_TIME == characteristic?.uuid -> {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Read CurrentTime")
                    }
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE)
                    )
                }
                TimeProfile.LOCAL_TIME_INFO == characteristic?.uuid -> {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Read LocalTimeInfo")
                    }
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        TimeProfile.getLocalTimeInfo(now)
                    )
                }
                else -> {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Invalid Characteristic Read: " + characteristic?.uuid)
                    }
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            if (TimeProfile.CLIENT_CONFIG == descriptor?.uuid) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Config descriptor read")
                }
                val returnValue = if (registeredDevices.contains(device)) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    returnValue
                )
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Unknown descriptor read request")
                }
                bluetoothGattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0, null
                )
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (TimeProfile.CLIENT_CONFIG == descriptor?.uuid) {
                device?.let {
                    if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Subscribe device to notifications: $it")
                        }
                        registeredDevices.add(it)
                    } else if (Arrays.equals(
                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE,
                            value
                        )
                    ) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Unsubscribe device from notifications: $it")
                        }
                        registeredDevices.remove(it)
                    }

                    if (responseNeeded) {
                        bluetoothGattServer?.sendResponse(
                            device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0, null
                        )
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Unknown descriptor write request")
                }
                if (responseNeeded) {
                    bluetoothGattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0, null
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Register for system clock events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }

        context?.registerReceiver(timeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()

        context?.unregisterReceiver(timeReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter.isEnabled) {
            stopServer()
            stopAdvertising()
        }

        context?.unregisterReceiver(bluetoothReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.settings_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        // We can't continue without proper Bluetooth support
        if (!checkBluetoothSupport(bluetoothAdapter)) {
            Toast.makeText(context, "Bluetooth is not supported", Toast.LENGTH_LONG).show()
        } else {
            // Register for system Bluetooth events
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            context?.registerReceiver(bluetoothReceiver, filter)
            if (!bluetoothAdapter.isEnabled) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Bluetooth is currently disabled...enabling")
                }
                bluetoothAdapter.enable()
            } else {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Bluetooth enabled...starting services")
                }
                startAdvertising()
                startServer()
            }
        }
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System [BluetoothAdapter].
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean {

        if (bluetoothAdapter == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Bluetooth is not supported")
            }
            return false
        }

        context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)?.let {
            if (!it) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Bluetooth LE is not supported")
                }
                return false
            }
        }

        return true
    }

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    private fun startAdvertising() {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser

        bluetoothLeAdvertiser?.let {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(TimeProfile.TIME_SERVICE))
                .build()

            it.startAdvertising(settings, data, advertiseCallback)
        } ?: Log.w(TAG, "Failed to create advertiser")
    }

    /**
     * Stop Bluetooth advertisements.
     */
    private fun stopAdvertising() {
        val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
            bluetoothManager.adapter.bluetoothLeAdvertiser
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback) ?: Log.w(
            TAG,
            "Failed to create advertiser"
        )
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    private fun startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        bluetoothGattServer?.addService(TimeProfile.createTimeService())
            ?: Log.w(TAG, "Unable to create GATT server")

        // Initialize the local UI
        updateLocalUi(System.currentTimeMillis())
    }

    /**
     * Shut down the GATT server.
     */
    private fun stopServer() {
        bluetoothGattServer?.close()
    }

    /**
     * Send a time service notification to any devices that are subscribed
     * to the characteristic.
     */
    private fun notifyRegisteredDevices(timestamp: Long, adjustReason: Byte) {
        if (registeredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered")
            return
        }
        val exactTime = TimeProfile.getExactTime(timestamp, adjustReason)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Sending update to ${registeredDevices.size} subscribers")
        }
        for (device in registeredDevices) {
            val timeCharacteristic = bluetoothGattServer
                ?.getService(TimeProfile.TIME_SERVICE)
                ?.getCharacteristic(TimeProfile.CURRENT_TIME)
            timeCharacteristic?.value = exactTime
            bluetoothGattServer?.notifyCharacteristicChanged(device, timeCharacteristic, false)
        }
    }

    /**
     * Update graphical UI on devices that support it with the current time.
     */
    private fun updateLocalUi(timestamp: Long) {
        val date = Date(timestamp)
        val displayDate = DateFormat.getMediumDateFormat(context).format(date)
        val displayTime = DateFormat.getTimeFormat(context).format(date)
        val tmp = "$displayDate\n$displayTime"
        tvSystemTime?.text = tmp
        tvStartServiceResult.setText(R.string.server_is_started)
    }
}
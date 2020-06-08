package com.arsinde.weatherapp.services

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

private val TAG = BluetoothLeService::class.java.simpleName
private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2
const val ACTION_GATT_CONNECTED = "com.arsinde.weatherapp.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.arsinde.weatherapp.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.arsinde.weatherapp.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.arsinde.weatherapp.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.arsinde.weatherapp.EXTRA_DATA"

class BluetoothLeService : Service() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothDeviceAddress: String? = null
    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate() {
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        println("Bluetooth manager is initialized")
        bluetoothAdapter = bluetoothManager.adapter
        println("Bluetooth adapter is initialized")
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? = binder

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }


    override fun onUnbind(intent: Intent?): Boolean {
        /**
         * After using a given BLE device, the app must call this method to ensure resources are
         * released properly.
         */
        bluetoothGatt?.close()
        return super.onUnbind(intent)
    }

    private var connectionState = STATE_DISCONNECTED

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    println("onConnectionStateChange: SUCCESS")
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            println("onConnectionStateChange: le device connected")

                            intentAction = ACTION_GATT_CONNECTED
                            connectionState = STATE_CONNECTED
                            broadcastUpdate(intentAction)
                            Log.i(TAG, "Connected to GATT server.")
                            Log.i(
                                TAG, "Attempting to start service discovery: " +
                                        bluetoothGatt?.discoverServices()
                            )

                            gatt.discoverServices()
                        }
                        BluetoothProfile.STATE_DISCONNECTED -> {
                            println("onConnectionStateChange: le device disconnected")
                            gatt.close()
                            intentAction = ACTION_GATT_DISCONNECTED
                            connectionState = STATE_DISCONNECTED
                            Log.i(TAG, "Disconnected from GATT server.")
                            broadcastUpdate(intentAction)
                        }
                    }
                }
                BluetoothGatt.GATT_FAILURE -> {
                    println("onConnectionStateChange: BluetoothGatt.GATT_FAILURE")
                }
                else -> {
                    println("onConnectionStateChange: $status")
                    gatt.close()

                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                println(
                    "service discovery failed due to internal error '%s', disconnecting"
                )
                Log.w(TAG, "onServicesDiscovered received: $status")
                return
            }
            val services = gatt.services
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            services.forEach {
                println("Service: ${it.uuid} & ${it.type}")
                it.characteristics.forEach { ch ->

                }
            }
            println(
                String.format(
                    "discovered %d services for '%s'",
                    services.size,
                    gatt.device.name
                )
            )
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                }
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            TimeProfile.CURRENT_TIME -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        Log.d(TAG, "Heart rate format UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        Log.d(TAG, "Heart rate format UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                }
            }

        }
        sendBroadcast(intent)
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String): Boolean {
        return if (bluetoothAdapter != null) {
            val leDevice = bluetoothAdapter?.getRemoteDevice(address)
            return if (leDevice != null) {
                leDevice.connectGatt(
                    this,
                    true,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
                Log.d(TAG, "Trying to create a new connection.")
                bluetoothDeviceAddress = address
                connectionState = STATE_CONNECTING
                true
            } else {
                Log.w(TAG, "Device not found.  Unable to connect.")
                false
            }
        } else {
            Log.d(TAG, "Bluetooth adapter is null.")
            false
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt?.disconnect()
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
//        if (BluetoothLeService.UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
        val descriptor = characteristic.getDescriptor(
            UUID.fromString(TimeProfile.CURRENT_TIME.toString())
        )
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt?.writeDescriptor(descriptor)
//        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }
}
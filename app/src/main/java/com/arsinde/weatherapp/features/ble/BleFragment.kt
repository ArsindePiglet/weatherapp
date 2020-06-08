package com.arsinde.weatherapp.features.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.adapters.BluetoothDevicesAdapter
import com.arsinde.weatherapp.features.components.BleDeviceInfoActivity
import com.arsinde.weatherapp.features.components.DEVICE_ADDRESS
import com.arsinde.weatherapp.features.components.DEVICE_NAME
import com.arsinde.weatherapp.models.ble.BleViewModel
import kotlinx.android.synthetic.main.ble_fragment.*
import java.util.*


const val REQUEST_LOCATION = 101

class BleFragment : Fragment() {

    //TODO add the check of the Location permissions

    companion object {
        private val TAG: String = BleFragment::javaClass.name
        fun newInstance() = BleFragment()
    }

    private val viewLayoutManager by lazy { LinearLayoutManager(context) }
    private val viewModel: BleViewModel by viewModels()
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.ble_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }

        checkPermissions()

        btnStartScan.setOnClickListener {
            progressBar.show()
            viewModel.scanLeDevices()
        }
        btnStopScan.setOnClickListener {
        }
    }

    private fun checkPermissions() {
        context?.let { cnt ->
            when {
                ContextCompat.checkSelfPermission(
                    cnt,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    //TODO Action if permission granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO Explication to the user why your app requires this
                    // permission for a specific feature to behave as expected.
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION
                    )
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.serviceResponse.observe(viewLifecycleOwner, Observer {
            println("ServiceResponse^ $it")
        })
        viewModel.leDevices.observe(viewLifecycleOwner, Observer {
            it.forEach { device ->
                println("Device: ${device.deviceName}:${device.deviceAddress}")
            }
            progressBar.hide()
            val btdAdapter =
                BluetoothDevicesAdapter(
                    it.toList(),
                    BleClickListener { device ->
                        val intent = Intent(context, BleDeviceInfoActivity::class.java)
                        intent.putExtra(DEVICE_NAME, device.deviceName)
                        intent.putExtra(DEVICE_ADDRESS, device.deviceAddress)

                        startActivity(intent)

//                        val dialog =
//                            CurrentWeatherDialog("${device.deviceName}:${device.deviceAddress}")
//                        dialog.show(
//                            childFragmentManager,
//                            TAG
//                        )
//                        val leDevice =
//                            viewModel.bluetoothAdapter?.getRemoteDevice(device.deviceAddress)
//                        leDevice?.createBond()
//                        leDevice?.let { bd ->
//                            val deviceGatt: BluetoothGatt = bd.connectGatt(
//                                context,
//                                true,
//                                gattCallback,
//                                BluetoothDevice.TRANSPORT_LE
//                            )
//                        }
                    })
            bluetoothDevices.apply {
                layoutManager = viewLayoutManager
                adapter = btdAdapter
            }
        })
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                println(
                    String.format(
                        Locale.ENGLISH,
                        "ERROR: Read failed for characteristic: %s, status %d",
                        characteristic.uuid,
                        status
                    )
                )
            }
            println("BluetoothGattCharacteristic: ${characteristic.uuid}")

        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != GATT_SUCCESS) {
                println(
                    "service discovery failed due to internal error '%s', disconnecting"
                )
                return
            }
            val services = gatt.services
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

        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            println("RSSI: $rssi")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                println("onConnectionStateChange: SUCCESS")
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    println("onConnectionStateChange: le device connected")

                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    println("onConnectionStateChange: le device disconnected")
                    gatt.close()
                }
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                println("onConnectionStateChange: BluetoothGatt.GATT_FAILURE")
            } else {
                println("onConnectionStateChange: $status")
                gatt.close()
            }
        }
    }
}
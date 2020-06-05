package com.arsinde.weatherapp.features.ble

import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.dialogs.CurrentWeatherDialog
import kotlinx.android.synthetic.main.ble_fragment.*
import java.util.*

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

        btnStartScan.setOnClickListener {
            progressBar.show()
            viewModel.scanLeDevices()
        }
        btnStopScan.setOnClickListener {
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
            val btdAdapter = BluetoothDevicesAdapter(it.toList(), BleClickListener { device ->
                val dialog = CurrentWeatherDialog("${device.deviceName}:${device.deviceAddress}")
                dialog.show(
                    childFragmentManager,
                    TAG
                )
                val leDevice = viewModel.bluetoothAdapter?.getRemoteDevice(device.deviceAddress)
                leDevice?.let { bd ->
                    val deviceGatt: BluetoothGatt = bd.connectGatt(
                        context,
                        true,
                        gattCallback,
                        BluetoothDevice.TRANSPORT_LE
                    )
                }
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
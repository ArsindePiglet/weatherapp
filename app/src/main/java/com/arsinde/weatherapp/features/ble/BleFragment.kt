package com.arsinde.weatherapp.features.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinde.weatherapp.BuildConfig
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.adapters.BluetoothDevicesAdapter
import com.arsinde.weatherapp.dialogs.CurrentWeatherDialog
import com.arsinde.weatherapp.features.components.BleDeviceInfoActivity
import com.arsinde.weatherapp.features.components.DEVICE_ADDRESS
import com.arsinde.weatherapp.features.components.DEVICE_NAME
import com.arsinde.weatherapp.models.ble.BleViewModel
import kotlinx.android.synthetic.main.ble_fragment.*
import kotlinx.coroutines.delay
import java.util.*


const val REQUEST_LOCATION = 101

class BleFragment : Fragment() {

    companion object {
        fun newInstance() = BleFragment()
    }

    private val viewLayoutManager by lazy { LinearLayoutManager(context) }
    private val viewModel: BleViewModel by viewModels()
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private val bluetoothManager by lazy {
        context?.let {
            it.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }
    }

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
            if (BuildConfig.DEBUG) {
                it.forEach { device ->
                    println("Device: ${device.deviceName}:${device.deviceAddress}")
                }
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
                    })
            bluetoothDevices.apply {
                layoutManager = viewLayoutManager
                adapter = btdAdapter
            }
        })
    }
}
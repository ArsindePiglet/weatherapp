package com.arsinde.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arsinde.weatherapp.features.CurrentWeatherFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentNavigator()
    }

    private fun fragmentNavigator() {
        val bluetoothFragment = CurrentWeatherFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainContainer, bluetoothFragment)
        transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss()
    }
}

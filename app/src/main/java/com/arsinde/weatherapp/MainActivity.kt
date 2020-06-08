package com.arsinde.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.arsinde.weatherapp.features.ble.BleFragment
import com.arsinde.weatherapp.features.ble.REQUEST_LOCATION
import com.arsinde.weatherapp.features.components.SettingsFragment
import com.arsinde.weatherapp.features.weather.CurrentWeatherFragment
import kotlinx.android.synthetic.main.activity_main.*

//class MainActivity : FragmentActivity() {
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentNavigator(BleFragment.newInstance())
        setupBottomNavigationBar()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG) {
                    println("Location permission is granted")
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                println("Huston, we have a problem!!!")
            }
        }
    }

    private fun fragmentNavigator(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, fragment)
            .commit()
    }

    private fun setupBottomNavigationBar() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.translatePage -> {
                    fragmentNavigator(BleFragment.newInstance())
                    true
                }
                R.id.dictionaryPage -> {
                    fragmentNavigator(CurrentWeatherFragment.newInstance())
                    true
                }
                R.id.settingsPage -> {
                    fragmentNavigator(SettingsFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }
}

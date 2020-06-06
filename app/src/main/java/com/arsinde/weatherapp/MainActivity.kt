package com.arsinde.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arsinde.weatherapp.features.ble.BleFragment
import com.arsinde.weatherapp.features.components.SettingsFragment
import com.arsinde.weatherapp.features.weather.CurrentWeatherFragment
import kotlinx.android.synthetic.main.activity_main.*

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

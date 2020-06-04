package com.arsinde.weatherapp

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.arsinde.weatherapp.features.dictionary.DictionaryFragment
import com.arsinde.weatherapp.features.dictionary.SettingsFragment
import com.arsinde.weatherapp.features.dictionary.TranslateFragment
import com.arsinde.weatherapp.features.weather.CurrentWeatherFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentNavigator(TranslateFragment.newInstance())
        setupBottomNavigationBar()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun fragmentNavigator(fragment: Fragment) {
//        val bluetoothFragment = CurrentWeatherFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, fragment)
            .commit()
    }

    private fun setupBottomNavigationBar() {
        bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.translatePage -> {
                    fragmentNavigator(TranslateFragment.newInstance())
                    true
                }
                R.id.dictionaryPage -> {
                    fragmentNavigator(DictionaryFragment.newInstance())
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

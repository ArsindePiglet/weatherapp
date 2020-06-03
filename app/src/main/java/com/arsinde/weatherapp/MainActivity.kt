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

    private val host by lazy { NavHostFragment.create(R.navigation.dictionary_navigation) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        fragmentNavigator()
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun fragmentNavigator() {
//        val bluetoothFragment = CurrentWeatherFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, host)
            .commit()
    }

    private fun setupBottomNavigationBar() {
        val navController = host.findNavController()
        bottomNavigation?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val dest: String = try {
                resources.getResourceName(destination.id)
            } catch (e: Resources.NotFoundException) {
                println(e.message)
                e.localizedMessage
            }
            Log.d("NavigationActivity", "Navigated to $dest")
        }
    }
}

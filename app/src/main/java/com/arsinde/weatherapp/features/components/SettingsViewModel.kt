package com.arsinde.weatherapp.features.components

import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arsinde.weatherapp.models.system.RunningProcessData
import com.arsinde.weatherapp.services.SleepService
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: Application) : AndroidViewModel(app) {

    fun startService() {
        with(app.applicationContext) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(
                    Intent(
                        this,
                        SleepService::class.java
                    )
                )
            } else {
                startService(
                    Intent(
                        this,
                        SleepService::class.java
                    )
                )
            }
        }
    }
}
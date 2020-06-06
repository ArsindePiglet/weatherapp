package com.arsinde.weatherapp.features.components

import android.app.ActivityManager
import android.app.Application
import android.app.Service
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arsinde.weatherapp.models.system.RunningProcessData
import kotlinx.coroutines.launch

class SettingsViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _listOfServices = mutableListOf<RunningProcessData>()
    val processesList = MutableLiveData<List<RunningProcessData>>()

    fun getSystemServices() {
        viewModelScope.launch {
            (app.applicationContext.getSystemService(Service.ACTIVITY_SERVICE) as? ActivityManager)?.let {
                it.runningAppProcesses.map {pcs ->
                    val tmp = RunningProcessData(
                        pid = pcs.pid,
                        uid = pcs.uid,
                        process = pcs.processName
                    )
                    _listOfServices.add(tmp)
                }
            }
            processesList.postValue(_listOfServices)
        }
    }
}
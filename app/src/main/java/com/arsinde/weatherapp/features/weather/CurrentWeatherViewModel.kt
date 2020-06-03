package com.arsinde.weatherapp.features.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arsinde.libcurrentweather.CurrentWeather
import com.arsinde.weatherapp.common.isCityValid
import com.arsinde.weatherapp.common.isLocationValid
import kotlinx.coroutines.launch

class CurrentWeatherViewModel : ViewModel() {
    private val weatherLib = CurrentWeather()

    val currentWeather = MutableLiveData<String>()
    val errorMsg = MutableLiveData<String>()

    fun fetchWeather(location: String) {
        viewModelScope.launch {
            if (location.isLocationValid() || location.isCityValid()) {
                currentWeather.postValue(weatherLib.getCurrentWeather(location))
            } else {
                errorMsg.postValue("Location is invalid!")
            }
        }
    }
}

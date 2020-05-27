package com.arsinde.libcurrentweather.repo

import com.arsinde.libcurrentweather.data.SearchData
import com.arsinde.libcurrentweather.data.WeatherData
import com.arsinde.libcurrentweather.net.WeatherApi

class WeatherRepository(private val api: WeatherApi) : BaseRepository() {
    suspend fun getWeatherByLocation(lattlong: String): List<SearchData>? {
        val response = safeApiCall(
            call = { api.getCurrentWeatherByLocation(lattlong) },
            errorMessage = "Something was gone wrong!!!!"
        )
        return response?.toList()
    }

    suspend fun getWeatherByCity(city: String): List<SearchData>? {
        val response = safeApiCall(
            call = { api.getCurrentWeatherByCity(city) },
            errorMessage = "Something was gone wrong!!!!"
        )
        return response?.toList()
    }

    suspend fun getWeatherById(id: Int): WeatherData? {
        return safeApiCall(
            call = { api.getCurrentWeatherById(id) },
            errorMessage = "Something was gone wrong!!!!"
        )
    }
}
package com.arsinde.libcurrentweather

import com.arsinde.libcurrentweather.net.ApiFactory
import com.arsinde.libcurrentweather.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class CurrentWeather {

    private val repo: WeatherRepository = WeatherRepository(ApiFactory.weatherApi())
    private val error = "Location is absent!"

    private val cityPattern = Pattern.compile("^[a-zA-Z]*$")
    private fun String.isCity() = cityPattern.matcher(this).matches()

    suspend fun getCurrentWeather(location: String): String {
        val str = StringBuilder()
        if (location.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                if (location.isCity()) {
                    repo.getWeatherByCity(location)
                } else {
                    repo.getWeatherByLocation(location)
                }?.apply {
                    forEach {
                        val weatherInCity = getWeatherInCity(it.woeid)
                        str.append(weatherInCity)
                        str.append("\n............................\n")
                    }
                }

            }
        } else {
            str.append(error)
        }

        return str.toString()
    }

    private suspend fun getWeatherInCity(id: Int): String {
        val str = StringBuilder()

        withContext(Dispatchers.IO) {
            repo.getWeatherById(id)?.apply {
                with(this) {
                    str.append("$title\n")
                    consolidated_weather.forEach { cw ->
                        val date = cw.applicable_date.split("T").first()
                        str.append("$date:\t${cw.the_temp.toInt()}\n")
                    }
                }
            }
        }
        return str.toString()
    }
}
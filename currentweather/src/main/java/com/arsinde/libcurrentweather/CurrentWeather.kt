package com.arsinde.libcurrentweather

import com.arsinde.libcurrentweather.data.SearchData
import com.arsinde.libcurrentweather.net.ApiFactory
import com.arsinde.libcurrentweather.repo.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import java.util.regex.Pattern

class CurrentWeather {

    private val repo: WeatherRepository = WeatherRepository(ApiFactory.weatherApi())
    private val error = "Location is absent!"

    private val parentJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(parentJob + Dispatchers.Default)

    private val cityPattern = Pattern.compile("^[a-zA-Z]*$")
    private fun String.isCity() = cityPattern.matcher(this).matches()

    suspend fun getCurrentWeather(location: String): String {

        val str = StringBuilder()
        if (location.isNotEmpty()) {

            var list = listOf<SearchData>()

            val job = coroutineScope.async {
                if (location.isCity()) {
                    repo.getWeatherByCity(location)
                } else {
                    repo.getWeatherByLocation(location)
                }
            }

            try {
                job.await()?.let {
                    list = it
                }
            } catch (ex: Exception) {
                println(ex.message)
            }

            list.forEach {
                val weatherInCity = getWeatherInCity(it.woeid)
                str.append(weatherInCity)
                str.append("\n............................\n")
            }
        } else {
            str.append(error)
        }

        return str.toString()
    }

    private suspend fun getWeatherInCity(id: Int): String {

        val str = StringBuilder()
        val job = coroutineScope.async {
            repo.getWeatherById(id)
        }
        try {
            job.await()?.let {
                with(it) {
                    str.append("$title\n")
                    consolidated_weather.forEach { cw ->
                        val date = cw.applicable_date.split("T").first()
                        str.append("$date:\t${cw.the_temp.toInt()}\n")
                    }
                }
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
        return str.toString()
    }
}

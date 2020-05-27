package com.arsinde.libcurrentweather.data

data class WeatherData(
    val consolidated_weather: List<ConsolidatedWeather>,
    val time: String,
    val sun_rise: String,
    val sun_set: String,
    val timezone_name: String,
    val parent: Parent,
    val sources: List<Source>,
    val title: String,
    val location_type: String,
    val woeid: Int,
    val latt_long: String,
    val timezone: String
)

data class ConsolidatedWeather(
    val id: Long,
    val weather_state_name: String,
    val weather_state_abbr: String,
    val wind_direction_compass: String,
    val created: String,
    val applicable_date: String,
    val min_temp: Float,
    val max_temp: Float,
    val the_temp: Float,
    val wind_speed: Float,
    val wind_direction: Float,
    val air_pressure: Float,
    val humidity: Float,
    val visibility: Float,
    val predictability: Int
)

data class Parent(
    val title: String,
    val location_type: String,
    val woeid: Int,
    val latt_long: String
)

data class Source(
    val title: String,
    val slug: String,
    val url: String,
    val crawl_rate: Int
)
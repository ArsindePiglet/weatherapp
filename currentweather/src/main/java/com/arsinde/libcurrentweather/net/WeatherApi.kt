package com.arsinde.libcurrentweather.net

import com.arsinde.libcurrentweather.data.SearchData
import com.arsinde.libcurrentweather.data.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    @GET("location/search/")
    suspend fun getCurrentWeatherByLocation(@Query("lattlong") lattlong: String): Response<List<SearchData>>

    @GET("location/search/")
    suspend fun getCurrentWeatherByCity(@Query("query") city: String): Response<List<SearchData>>

    @GET("/api/location/{id}")
    suspend fun getCurrentWeatherById(@Path("id") id: Int): Response<WeatherData>
}
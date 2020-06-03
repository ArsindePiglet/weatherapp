package com.arsinde.libcurrentweather.net

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    private const val WEATHER_HTTP = "https://www.metaweather.com/api/"
    private val level = HttpLoggingInterceptor.Level.HEADERS

    private val client = OkHttpClient()
        .newBuilder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(level))
        .build()

    private fun retrofit(): Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(WEATHER_HTTP)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    fun weatherApi(): WeatherApi = retrofit().create(WeatherApi::class.java)
}
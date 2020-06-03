package com.arsinde.libcurrentweather.data

data class SearchData(
    val distance: Int?,
    val title: String,
    val location_type: String,
    val woeid: Int,
    val latt_long: String
)
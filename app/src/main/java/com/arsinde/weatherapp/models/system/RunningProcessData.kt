package com.arsinde.weatherapp.models.system

data class RunningProcessData(
    val pid: Int,
    val uid: Int,
    val process: String
)
package com.arsinde.libcurrentweather.net

import java.lang.Exception

sealed class ResponseResult<out T: Any> {
    data class Success<out T: Any> (val data: T): ResponseResult<T>()
    data class Error (val exception: Exception): ResponseResult<Nothing>()
}
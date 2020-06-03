package com.arsinde.libcurrentweather.repo

import com.arsinde.libcurrentweather.net.ResponseResult
import retrofit2.Response
import java.io.IOException

open class BaseRepository {

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, errorMessage: String): T? {
        val result: ResponseResult<T> = safeApiResult(call, errorMessage)
        var data: T? = null

        when (result) {
            is ResponseResult.Success -> data = result.data
            is ResponseResult.Error -> println("DataRepository: ${result.exception}")
        }
        return data
    }

    private suspend fun <T : Any> safeApiResult(
        call: suspend () -> Response<T>,
        errorMessage: String
    ): ResponseResult<T> {
        val response = call.invoke()
        if (response.isSuccessful) {
            response.body()?.let {
                return ResponseResult.Success(it)
            }
        }
        return ResponseResult.Error(IOException("Error Occurred during getting safe Api result, Custom ERROR - $errorMessage"))
    }
}
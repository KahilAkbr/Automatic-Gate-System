package com.example.iot_licenseplatedetection.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("update")
    fun greenLEDListener(
        @Query("token") token: String,
        @Query("V2") value: Int
    ): Call<Void>

    @GET("update")
    fun servoListener(
        @Query("token") token: String,
        @Query("V1") value: Int
    ) : Call<Void>
}
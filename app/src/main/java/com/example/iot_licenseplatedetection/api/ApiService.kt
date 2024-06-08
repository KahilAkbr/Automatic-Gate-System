package com.example.iot_licenseplatedetection.api

import com.example.iot_licenseplatedetection.api.response.LicensePlateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @GET("update")
    fun greenLEDListener(
        @Query("token") token: String,
        @Query("V2") value: Int
    ): Call<Void>

    @GET("update")
    fun redLEDListener(
        @Query("token") token : String,
        @Query("V3") value : Int
    )

    @GET("update")
    fun servoListener(
        @Query("token") token: String,
        @Query("V1") value: Int
    ) : Call<Void>

    @Multipart
    @POST("plate-reader")
    suspend fun sendLicensePlate(
        @Part file : MultipartBody.Part,
        @Part("regions") regions: RequestBody,
    ) : LicensePlateResponse
}
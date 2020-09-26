package com.transformer.dashboard.network

import com.transformer.dashboard.model.Stats
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://192.168.1.10:60000/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface HwMonitorApiService {
    @GET("json")
    fun getStatsData(): Call<Stats>
}

object HwMonitorApi {
    val retrofitService: HwMonitorApiService by lazy {
        retrofit.create(HwMonitorApiService::class.java)
    }
}
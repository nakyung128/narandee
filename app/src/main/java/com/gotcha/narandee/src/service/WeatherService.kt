package com.gotcha.narandee.src.service

import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.src.clothes.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): WeatherResponse
}

object WeatherApi {
    val weatherService: WeatherService by lazy {
        ApplicationClass.weatherRetrofit.create(WeatherService::class.java)
    }
}
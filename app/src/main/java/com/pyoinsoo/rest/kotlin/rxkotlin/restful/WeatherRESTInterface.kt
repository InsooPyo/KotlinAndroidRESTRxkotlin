package com.pyoinsoo.rest.kotlin.rxkotlin.restful

import com.pyoinsoo.rest.kotlin.rxkotlin.jsondata.CurrentWeather
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/*
 * Created by pyoinsoo on 2018-01-11.
 * insoo.pyo@gmail.com
 * Rx 방식의 Retrofit 요청을 구성한다
 */
interface WeatherRESTInterface {

    /*
     * 현재 날씨를 가져온다(json data)
     */
    @GET("/weather/current/minutely")
    fun requestSKCurrentWeather(
            @Query("lat") latitude: String,
            @Query("lon") longitude: String,
            @Query("version") apiVersion: String
          ): Observable<CurrentWeather>

    /*
     * 현재 미세먼지 수치를 가져온다(json data)
     */
    @GET("/weather/dust")
    fun requestSKFineDust(
            @Query("lat") latitude: String,
            @Query("lon") longitude: String,
            @Query("version") apiVersion: String
          ): Observable<CurrentWeather>

    /*
     * 현재 자외선 지수를 가져온다(json data)
     */
    @GET("/weather/windex/uvindex")
    fun requestSKUvRays(
            @Query("lat") latitude: String,
            @Query("lon") longitude: String,
            @Query("version") apiVersion: String
         ): Observable<CurrentWeather>
}
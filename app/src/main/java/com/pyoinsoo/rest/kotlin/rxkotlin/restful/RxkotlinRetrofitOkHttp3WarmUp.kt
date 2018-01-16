package com.pyoinsoo.rest.kotlin.rxkotlin.restful

import com.pyoinsoo.rest.kotlin.rxkotlin.R
import com.pyoinsoo.rest.kotlin.rxkotlin.common.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 * Created by pyoinsoo on 2018-01-16.
 * okhttp3/retrofit2/Rxkotlin setup 및 연동
 */
object RxkotlinRetrofitOkHttp3WarmUp {

    private val ALL_TIMEOUT = 10L
    private val SK_API_KEY = MyApplication.myApplication.
            resources.getString(R.string.sk_weather_api_key)
    private val SK_WEATHER_HOST = "http://apis.skplanetx.com/"

    private var okHttpClient: OkHttpClient
    private var retrofit: Retrofit

    init{
        /*
         * 로깅 인터셉터 연결
         */
        val httpLogging = HttpLoggingInterceptor()
        httpLogging.level = HttpLoggingInterceptor.Level.BASIC

        /*
         * OkHttp3를 설정한다
         */
        okHttpClient = OkHttpClient().newBuilder().apply {

            addInterceptor(httpLogging)
            addInterceptor(HeaderSettingInterceptor())
            connectTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)

        }.build()
        /*
         * Rxandroid(Rxkotlin)와 Retrofit2/OkHttp3 연동
         */
        retrofit = Retrofit.Builder().apply{

            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())//gson을 이용해 json파싱
            baseUrl(SK_WEATHER_HOST)
            client(okHttpClient)

        }.build()

    }
    /*
     *  Request Header를 세팅하는 Interceptor
     *  요청을 실행할 때 마다 다음의 코드로
     *  Http Header를 세팅한다
     */
    private  class HeaderSettingInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val chainRequest = chain.request()

            val request = chainRequest.newBuilder().apply{
                addHeader("Accept", "application/json")
                addHeader("appKey",SK_API_KEY)
            }.build()

            return chain.proceed(request)
        }
    }

    /*
     * interface로 선언된 Retrofit REST 객체를 생성하여 넘긴다
     */
    fun <T> createSKWeatherRESTService(retrobitRESTClass: Class<T>): T {
        return retrofit.create(retrobitRESTClass)
    }

}
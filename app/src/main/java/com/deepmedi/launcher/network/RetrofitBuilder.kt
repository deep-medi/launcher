package com.deepmedi.launcher.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitBuilder {
    private const val BASE_URL: String = "https://kr.object.ncloudstorage.com/"

    private const val TIME_OUT_SEC = 180L

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor {
            val request = it.request().newBuilder()
                .build()

            it.proceed(request)
        }
        .readTimeout(TIME_OUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT_SEC, TimeUnit.SECONDS)
        .connectTimeout(TIME_OUT_SEC, TimeUnit.SECONDS)
        .build()

    private var gson: Gson = GsonBuilder().setLenient().create()

    val newServiceApi: NetworkService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(NetworkService::class.java)

}
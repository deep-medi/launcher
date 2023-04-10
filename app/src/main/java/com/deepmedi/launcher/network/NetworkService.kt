package com.deepmedi.launcher.network

import retrofit2.Response
import retrofit2.http.GET

interface NetworkService {
    @GET("face-health-releases/latest_version.txt")
    suspend fun getVersion(): Response<String>

}
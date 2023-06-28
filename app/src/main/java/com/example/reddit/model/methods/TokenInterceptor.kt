package com.example.reddit.model.methods

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sharedPreferences.getString("access_token", null)
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(request)
    }
}

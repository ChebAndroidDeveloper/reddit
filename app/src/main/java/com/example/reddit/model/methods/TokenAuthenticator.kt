package com.example.reddit.model.methods

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.for_get_token.TokenResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.Base64
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Authenticator {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun authenticate(route: Route?, response: Response): Request? {
        var tokenResponse: TokenResponse? = null
        runBlocking {
            tokenResponse = refreshToken()
        }
        if (tokenResponse != null) {
            sharedPreferences.edit()
                .putString("access_token", tokenResponse!!.accessToken)
                .putString("refresh_token", tokenResponse!!.refreshToken)
                .apply()
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${tokenResponse!!.accessToken}")
                .build()
        }
        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshToken(): TokenResponse? {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        if (refreshToken != null) {
            val client = OkHttpClient()
            val requestBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build()
            val request = Request.Builder()
                .url("https://www.reddit.com/api/v1/access_token")
                .header("Authorization", "Basic ${encodeInBase64()}")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val gson = Gson()
                return gson.fromJson(responseBody, TokenResponse::class.java)
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encodeInBase64(): String {
        val authString = "${Constants.CLIENT_ID}:"
        return Base64.getEncoder().encodeToString(authString.toByteArray())
    }

}



package com.example.reddit.view

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.for_get_token.TokenResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.UUID

@Composable
fun WebViewClientForRedditToGetTokens() {
    val state = remember { UUID.randomUUID().toString() }
    val context = LocalContext.current

    val authUrl = "${Constants.AUTHORIZE_URL}client_id=${Constants.CLIENT_ID}&" +
            "response_type=code&" +
            "state=$state&" +
            "redirect_uri=${Constants.REDIRECT_URI}&" +
            "duration=permanent&" +
            "scope=${Constants.SCOPES}"
    Log.d("MyApp", "authUrl: $authUrl")

    val webView = remember {
        WebView(context).also { webView ->
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.userAgentString = Constants.USER_AGENT_STRING

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            webView.webViewClient = object : WebViewClient() {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    if (url != null && url.startsWith(Constants.REDIRECT_URI)) {
                        val uri = Uri.parse(url)
                        val code = uri.getQueryParameter("code")
                        val responseState = uri.getQueryParameter("state")

                        // Verify the state value
                        if (responseState == state) {
                            exchangeAuthorizationCodeForTokens(
                                code!!,
                                Constants.CLIENT_ID,
                                Constants.REDIRECT_URI,
                                context
                            )


                        } else Log.d("MyApp", "State value does not match")
                        return true
                    }
                    return false
                }
            }
            webView.loadUrl(authUrl)
        }
    }

    AndroidView(
        factory = { webView },
        update = { it.loadUrl(authUrl) }
    )

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }
}


fun exchangeAuthorizationCodeForTokens(
    code: String,
    clientId: String,
    redirectUri: String,
    context: Context
) {
    val auth = "Basic " + Base64.encodeToString("$clientId:".toByteArray(), Base64.NO_WRAP)

    val requestBody = FormBody.Builder()
        .add("grant_type", "authorization_code")
        .add("code", code)
        .add("redirect_uri", redirectUri)
        .build()

    val request = Request.Builder()
        .url(Constants.URL_FOR_TAKE_ACESS_TOKEN)
        .addHeader("Authorization", auth)
        .addHeader("User-Agent", Constants.USER_AGENT)
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle failure
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            if (responseBody != null) {
                val tokenResponse = Json.decodeFromString<TokenResponse>(responseBody)
                val accessToken = tokenResponse.accessToken
                val refreshToken = tokenResponse.refreshToken
                val expirationTime = System.currentTimeMillis() + 3600 * 1000 // 1 hour in milliseconds

                // Save the access token, refresh token and expiration time in SharedPreferences
                val sharedPreferences =
                    context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("access_token", accessToken)
                    .putString("refresh_token", refreshToken)
                    .putLong("expiration_time", expirationTime)
                    .apply()
                Log.d("MyApp", "accessToken: $accessToken")
                Log.d("MyApp", "refreshToken: $refreshToken")
                Log.d("MyApp", "expirationTime: $expirationTime")
            }
        }

    })
}


package com.example.reddit

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.reddit.ui.theme.RedditTheme
import com.example.reddit.view.MainScreen
import com.example.reddit.view.OnboardingScreen
import com.example.reddit.view.WebViewClientForRedditToGetTokens
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "access_token" || key == "refresh_token") {
                val accessToken = sharedPreferences.getString("access_token", null)
                val refreshToken = sharedPreferences.getString("refresh_token", null)
                currentScreen = if (accessToken != null && refreshToken != null) {
                    "main"
                } else {
                    "login"
                }
            }
        }

    private var currentScreen by mutableStateOf("onboarding")
    private var isOnboardingSkipped by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedditTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
                    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

                    val accessToken = sharedPreferences.getString("access_token", null)
                    val refreshToken = sharedPreferences.getString("refresh_token", null)
                    val skipOnboarding = sharedPreferences.getBoolean("skip_onboarding", false)

                    if (skipOnboarding || isOnboardingSkipped) {
                        currentScreen = if (accessToken != null && refreshToken != null) {
                            "main"
                        } else {
                            "login"
                        }
                    }

                    when (currentScreen) {
                        "main" -> MainScreen()
                        "login" -> WebViewClientForRedditToGetTokens()
                        "onboarding" -> OnboardingScreen(
                            onSkip = {
                                isOnboardingSkipped = true
                                currentScreen = if (accessToken != null && refreshToken != null) {
                                    "main"
                                } else {
                                    "login"
                                }
                            },
                            onDontShowAgain = { dontShowAgain ->
                                sharedPreferences.edit().putBoolean("skip_onboarding", dontShowAgain).apply()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isOnboardingSkipped", isOnboardingSkipped)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isOnboardingSkipped = savedInstanceState.getBoolean("isOnboardingSkipped")
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}








package com.example.reddit.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.TokenAuthenticator
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    fun provideOkHttpClient(
        sharedPreferences: SharedPreferences
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }


    @Provides
    @Named("withAuth")
    fun provideOkHttpClientWithAuth(
        sharedPreferences: SharedPreferences
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val addRawJsonInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val url = originalRequest.url.newBuilder()
                .addQueryParameter("raw_json", "1")
                .build()
            val newRequest = originalRequest.newBuilder()
                .url(url)
                .build()
            chain.proceed(newRequest)
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(addRawJsonInterceptor)
            .authenticator(TokenAuthenticator(sharedPreferences))
            .build()
    }



    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://oauth.reddit.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Named("withAuth")
    fun provideRetrofitWithAuth(@Named("withAuth") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://oauth.reddit.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    fun provideRedditApi(retrofit: Retrofit): RedditApi {
        return retrofit.create(RedditApi::class.java)
    }

    @Provides
    @Named("withAuth")
    fun provideRedditApiWithAuth(@Named("withAuth") retrofit: Retrofit): RedditApi {
        return retrofit.create(RedditApi::class.java)
    }
}



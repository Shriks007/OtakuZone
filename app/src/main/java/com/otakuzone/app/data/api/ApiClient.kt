package com.otakuzone.app.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.otakuzone.app.data.local.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val prefs: PreferencesManager) {

    val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val baseOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(prefs))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val anilistService: AniListApiService = Retrofit.Builder()
        .baseUrl("https://graphql.anilist.co/")
        .client(baseOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(AniListApiService::class.java)

    val aniKageService: AniKageApiService = Retrofit.Builder()
        .baseUrl("https://api.anikage.workers.dev/")
        .client(baseOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(AniKageApiService::class.java)

    fun getMangaVaultService(): MangaVaultApiService {
        val baseUrl = prefs.mangaVaultUrl.trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(baseOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MangaVaultApiService::class.java)
    }
}

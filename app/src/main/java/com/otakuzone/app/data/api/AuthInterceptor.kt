package com.otakuzone.app.data.api

import com.otakuzone.app.data.local.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val prefs: PreferencesManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = prefs.anilistToken

        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        if (!token.isNullOrBlank() && original.url.host.contains("anilist.co")) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

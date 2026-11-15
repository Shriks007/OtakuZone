package com.otakuzone.app.data.models

import com.google.gson.annotations.SerializedName

data class AniListTokenRequest(
    @SerializedName("grant_type") val grantType: String = "authorization_code",
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("redirect_uri") val redirectUri: String,
    @SerializedName("code") val code: String
)

data class AniListTokenResponse(
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Long?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?
)

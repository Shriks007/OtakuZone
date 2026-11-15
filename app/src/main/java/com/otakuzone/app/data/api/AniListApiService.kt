package com.otakuzone.app.data.api

import com.google.gson.JsonObject
import com.otakuzone.app.data.models.AniListTokenRequest
import com.otakuzone.app.data.models.AniListTokenResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any?> = emptyMap()
)

interface AniListApiService {

    @POST("https://graphql.anilist.co")
    @Headers("Content-Type: application/json")
    suspend fun queryGraphQL(@Body request: GraphQLRequest): JsonObject

    @POST("https://anilist.co/api/v2/oauth/token")
    @Headers("Content-Type: application/json")
    suspend fun exchangeToken(@Body request: AniListTokenRequest): AniListTokenResponse
}

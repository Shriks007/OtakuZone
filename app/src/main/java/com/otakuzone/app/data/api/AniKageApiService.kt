package com.otakuzone.app.data.api

import com.google.gson.JsonObject
import com.otakuzone.app.data.models.AniKageStreamResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface AniKageApiService {

    @GET("{anilistId}/{episode}")
    suspend fun getEpisodeStreams(
        @Path("anilistId") anilistId: Int,
        @Path("episode") episode: Int
    ): AniKageStreamResponse

    @GET("stream/mal/{malId}/{episode}/{subOrDub}")
    suspend fun testZokoMalStream(
        @Path("malId") malId: Int,
        @Path("episode") episode: Int,
        @Path("subOrDub") subOrDub: String
    ): JsonObject
}

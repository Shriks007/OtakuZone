package com.otakuzone.app.data.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaVaultApiService {

    @GET("nato/home")
    suspend fun getNatoHome(): JsonObject

    @GET("nato/manga/{slug}/details")
    suspend fun getNatoDetails(
        @Path("slug") slug: String
    ): JsonObject

    @GET("nato/manga/{manga_slug}/{chapter_slug}/images")
    suspend fun getNatoChapterImages(
        @Path("manga_slug") mangaSlug: String,
        @Path("chapter_slug") chapterSlug: String
    ): JsonObject

    @GET("atsu/home")
    suspend fun getAtsuHome(): JsonObject

    @GET("atsu/search")
    suspend fun searchAtsu(
        @Query("keyword") keyword: String,
        @Query("limit") limit: Int = 20
    ): JsonObject

    @GET("comix/home")
    suspend fun getComixHome(): JsonObject

    @GET("comix/search")
    suspend fun searchComix(
        @Query("keyword") keyword: String,
        @Query("limit") limit: Int = 20
    ): JsonObject
}

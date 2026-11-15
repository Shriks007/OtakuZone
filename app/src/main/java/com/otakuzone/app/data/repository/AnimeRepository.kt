package com.otakuzone.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.otakuzone.app.data.api.AniListApiService
import com.otakuzone.app.data.api.GraphQLRequest
import com.otakuzone.app.data.models.AiringScheduleEntry
import com.otakuzone.app.data.models.AniListMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnimeRepository(
    private val anilistService: AniListApiService,
    private val gson: Gson
) {

    private val mediaFragment = """
        fragment mediaFields on Media {
            id
            idMal
            title {
                romaji
                english
                native
            }
            description
            format
            status
            episodes
            duration
            bannerImage
            coverImage {
                extraLarge
                large
                medium
                color
            }
            genres
            averageScore
            popularity
            nextAiringEpisode {
                id
                airingAt
                timeUntilAiring
                episode
            }
        }
    """.trimIndent()

    suspend fun getTrendingAnime(page: Int = 1, perPage: Int = 20): Result<List<AniListMedia>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query (${'$'}page: Int, ${'$'}perPage: Int) {
                    Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                        media(type: ANIME, sort: TRENDING_DESC) {
                            ...mediaFields
                        }
                    }
                }
                $mediaFragment
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("page" to page, "perPage" to perPage))
            )
            val mediaArray = response.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("media")

            if (mediaArray != null) {
                val listType = object : TypeToken<List<AniListMedia>>() {}.type
                val result: List<AniListMedia> = gson.fromJson(mediaArray, listType)
                Result.success(result)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularAnime(page: Int = 1, perPage: Int = 20): Result<List<AniListMedia>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query (${'$'}page: Int, ${'$'}perPage: Int) {
                    Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                        media(type: ANIME, sort: POPULARITY_DESC) {
                            ...mediaFields
                        }
                    }
                }
                $mediaFragment
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("page" to page, "perPage" to perPage))
            )
            val mediaArray = response.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("media")

            if (mediaArray != null) {
                val listType = object : TypeToken<List<AniListMedia>>() {}.type
                val result: List<AniListMedia> = gson.fromJson(mediaArray, listType)
                Result.success(result)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAiringToday(): Result<List<AiringScheduleEntry>> = withContext(Dispatchers.IO) {
        try {
            val nowSeconds = System.currentTimeMillis() / 1000
            val startOfDay = nowSeconds - (nowSeconds % 86400)
            val endOfDay = startOfDay + 86400

            val query = """
                query (${'$'}airingAt_greater: Int, ${'$'}airingAt_lesser: Int) {
                    Page(page: 1, perPage: 30) {
                        airingSchedules(airingAt_greater: ${'$'}airingAt_greater, airingAt_lesser: ${'$'}airingAt_lesser, sort: TIME) {
                            id
                            airingAt
                            episode
                            media {
                                ...mediaFields
                            }
                        }
                    }
                }
                $mediaFragment
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(
                    query,
                    mapOf("airingAt_greater" to startOfDay, "airingAt_lesser" to endOfDay)
                )
            )
            val schedules = response.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("airingSchedules")

            if (schedules != null) {
                val listType = object : TypeToken<List<AiringScheduleEntry>>() {}.type
                val result: List<AiringScheduleEntry> = gson.fromJson(schedules, listType)
                Result.success(result)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnimeDetails(animeId: Int): Result<AniListMedia> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query (${'$'}id: Int) {
                    Media(id: ${'$'}id, type: ANIME) {
                        ...mediaFields
                    }
                }
                $mediaFragment
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("id" to animeId))
            )
            val mediaObj = response.getAsJsonObject("data")?.getAsJsonObject("Media")
            if (mediaObj != null) {
                val result: AniListMedia = gson.fromJson(mediaObj, AniListMedia::class.java)
                Result.success(result)
            } else {
                Result.failure(Exception("Anime details not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAnime(keyword: String, page: Int = 1): Result<List<AniListMedia>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query (${'$'}search: String, ${'$'}page: Int) {
                    Page(page: ${'$'}page, perPage: 25) {
                        media(search: ${'$'}search, type: ANIME, sort: SEARCH_MATCH) {
                            ...mediaFields
                        }
                    }
                }
                $mediaFragment
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("search" to keyword, "page" to page))
            )
            val mediaArray = response.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("media")

            if (mediaArray != null) {
                val listType = object : TypeToken<List<AniListMedia>>() {}.type
                val result: List<AniListMedia> = gson.fromJson(mediaArray, listType)
                Result.success(result)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotificationsAiringWindow(): Result<List<AiringScheduleEntry>> = withContext(Dispatchers.IO) {
        try {
            val nowSeconds = System.currentTimeMillis() / 1000
            val past24Hours = nowSeconds - 86400
            val next24Hours = nowSeconds + 86400

            val query = """
                query (${'$'}start: Int, ${'$'}end: Int) {
                    Page(page: 1, perPage: 50) {
                        airingSchedules(airingAt_greater: ${'$'}start, airingAt_lesser: ${'$'}end, sort: TIME) {
                            id
                            airingAt
                            episode
                            media {
                                id
                                idMal
                                title {
                                    english
                                    romaji
                                }
                                coverImage {
                                    medium
                                    large
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("start" to past24Hours, "end" to next24Hours))
            )
            val schedules = response.getAsJsonObject("data")
                ?.getAsJsonObject("Page")
                ?.getAsJsonArray("airingSchedules")

            if (schedules != null) {
                val listType = object : TypeToken<List<AiringScheduleEntry>>() {}.type
                val result: List<AiringScheduleEntry> = gson.fromJson(schedules, listType)
                Result.success(result)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

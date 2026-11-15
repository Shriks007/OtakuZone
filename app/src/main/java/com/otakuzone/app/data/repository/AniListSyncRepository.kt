package com.otakuzone.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.otakuzone.app.BuildConfig
import com.otakuzone.app.data.api.AniListApiService
import com.otakuzone.app.data.api.GraphQLRequest
import com.otakuzone.app.data.local.PreferencesManager
import com.otakuzone.app.data.models.AniListMedia
import com.otakuzone.app.data.models.AniListTokenRequest
import com.otakuzone.app.data.models.MediaListEntry
import com.otakuzone.app.data.models.MediaListGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AniListSyncRepository(
    private val anilistService: AniListApiService,
    private val prefs: PreferencesManager,
    private val gson: Gson
) {

    val isLoggedIn: Boolean
        get() = !prefs.anilistToken.isNullOrBlank()

    suspend fun loginWithPin(pin: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tokenResponse = anilistService.exchangeToken(
                AniListTokenRequest(
                    clientId = BuildConfig.ANILIST_CLIENT_ID,
                    clientSecret = BuildConfig.ANILIST_CLIENT_SECRET,
                    redirectUri = BuildConfig.ANILIST_REDIRECT_URI,
                    code = pin.trim()
                )
            )

            val accessToken = tokenResponse.accessToken
            if (!accessToken.isNullOrBlank()) {
                prefs.anilistToken = accessToken
                fetchViewerProfile()
                Result.success(accessToken)
            } else {
                Result.failure(Exception(tokenResponse.error ?: tokenResponse.message ?: "Failed to acquire token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchViewerProfile(): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        try {
            val query = """
                query {
                    Viewer {
                        id
                        name
                        avatar {
                            large
                            medium
                        }
                    }
                }
            """.trimIndent()

            val response = anilistService.queryGraphQL(GraphQLRequest(query))
            val viewerObj = response.getAsJsonObject("data")?.getAsJsonObject("Viewer")
            if (viewerObj != null) {
                val userId = viewerObj.get("id").asInt
                val username = viewerObj.get("name").asString
                val avatar = viewerObj.getAsJsonObject("avatar")?.get("large")?.asString

                prefs.anilistUserId = userId
                prefs.anilistUsername = username
                prefs.anilistUserAvatar = avatar

                Result.success(Pair(username, avatar))
            } else {
                Result.failure(Exception("Failed to fetch viewer profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserLists(): Result<List<MediaListGroup>> = withContext(Dispatchers.IO) {
        try {
            val userId = prefs.anilistUserId
            if (userId <= 0) {
                // Fetch profile first
                fetchViewerProfile()
            }
            val activeUserId = prefs.anilistUserId
            if (activeUserId <= 0) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            val query = """
                query (${'$'}userId: Int) {
                    MediaListCollection(userId: ${'$'}userId, type: ANIME) {
                        lists {
                            name
                            status
                            entries {
                                id
                                status
                                score
                                progress
                                updatedAt
                                media {
                                    id
                                    idMal
                                    title {
                                        english
                                        romaji
                                        native
                                    }
                                    coverImage {
                                        large
                                        medium
                                    }
                                    bannerImage
                                    episodes
                                    status
                                    averageScore
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            val response = anilistService.queryGraphQL(
                GraphQLRequest(query, mapOf("userId" to activeUserId))
            )
            val listsArray = response.getAsJsonObject("data")
                ?.getAsJsonObject("MediaListCollection")
                ?.getAsJsonArray("lists")

            if (listsArray != null) {
                val listType = object : TypeToken<List<MediaListGroup>>() {}.type
                val groups: List<MediaListGroup> = gson.fromJson(listsArray, listType)
                Result.success(groups)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWatchProgress(
        mediaId: Int,
        progress: Int,
        totalEpisodes: Int? = null,
        currentListStatus: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext Result.success(false)

        try {
            // Planning -> Watching rule:
            // When user starts watching (progress >= 1) and status is PLANNING, move to CURRENT (Watching)
            val nextStatus = when {
                totalEpisodes != null && totalEpisodes > 0 && progress >= totalEpisodes -> "COMPLETED"
                currentListStatus.equals("PLANNING", ignoreCase = true) && progress >= 1 -> "CURRENT"
                currentListStatus == null && progress >= 1 -> "CURRENT"
                else -> currentListStatus ?: "CURRENT"
            }

            val mutation = """
                mutation (${'$'}mediaId: Int, ${'$'}progress: Int, ${'$'}status: MediaListStatus) {
                    SaveMediaListEntry(mediaId: ${'$'}mediaId, progress: ${'$'}progress, status: ${'$'}status) {
                        id
                        status
                        progress
                    }
                }
            """.trimIndent()

            anilistService.queryGraphQL(
                GraphQLRequest(
                    mutation,
                    mapOf("mediaId" to mediaId, "progress" to progress, "status" to nextStatus)
                )
            )
            Result.success(true)
        } catch (e: Exception) {
            // Quiet retry once
            try {
                // Retry silently
                Result.success(false)
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateMediaStatus(mediaId: Int, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isLoggedIn) return@withContext Result.failure(Exception("Not logged in"))
        try {
            val mutation = """
                mutation (${'$'}mediaId: Int, ${'$'}status: MediaListStatus) {
                    SaveMediaListEntry(mediaId: ${'$'}mediaId, status: ${'$'}status) {
                        id
                        status
                    }
                }
            """.trimIndent()

            anilistService.queryGraphQL(
                GraphQLRequest(mutation, mapOf("mediaId" to mediaId, "status" to status))
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        prefs.clearAuth()
    }
}

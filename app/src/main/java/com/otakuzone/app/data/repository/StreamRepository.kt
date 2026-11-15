package com.otakuzone.app.data.repository

import com.otakuzone.app.data.api.AniKageApiService
import com.otakuzone.app.data.local.PreferencesManager
import com.otakuzone.app.data.models.AniKageStreamResponse
import com.otakuzone.app.data.models.AudioMode
import com.otakuzone.app.data.models.StreamServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class CachedStream(
    val timestamp: Long,
    val servers: List<StreamServer>
)

class StreamRepository(
    private val aniKageService: AniKageApiService,
    private val prefs: PreferencesManager
) {

    // 5-minute memory cache
    private val cache = ConcurrentHashMap<String, CachedStream>()
    private val CACHE_DURATION_MS = 5 * 60 * 1000L

    suspend fun getStreams(
        anilistId: Int,
        episode: Int,
        idMal: Int?
    ): Result<List<StreamServer>> = withContext(Dispatchers.IO) {
        val cacheKey = "$anilistId:$episode"
        val cached = cache[cacheKey]
        if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_DURATION_MS) {
            return@withContext Result.success(cached.servers)
        }

        // Auto-retry once on timeout/server error
        var lastException: Exception? = null
        for (attempt in 1..2) {
            try {
                val response: AniKageStreamResponse = aniKageService.getEpisodeStreams(anilistId, episode)
                val rawServers = response.servers ?: emptyList()

                // Process servers according to Zoko rules and MAL ID resolution
                val processedServers = processServers(rawServers, anilistId, episode, idMal)

                cache[cacheKey] = CachedStream(
                    timestamp = System.currentTimeMillis(),
                    servers = processedServers
                )
                return@withContext Result.success(processedServers)
            } catch (e: Exception) {
                lastException = e
                if (attempt == 1) {
                    delay(800) // Brief delay before auto-retry
                }
            }
        }

        Result.failure(lastException ?: Exception("Failed to fetch stream servers after retry"))
    }

    private fun processServers(
        rawServers: List<StreamServer>,
        anilistId: Int,
        episode: Int,
        idMal: Int?
    ): List<StreamServer> {
        val list = rawServers.toMutableList()

        // Critical Zoko Rule:
        // Must resolve AniList ID -> MAL ID before building Zoko URL.
        // Always use MAL route (/stream/mal/{malId}/{episode}/{sub|dub}). AniList route 404s!
        // If an anime has no MAL ID, drop Zoko as default for it and use next server.
        if (idMal != null && idMal > 0) {
            val zokoSubIndex = list.indexOfFirst { it.name.equals("Zoko", ignoreCase = true) && it.isSub }
            val zokoDubIndex = list.indexOfFirst { it.name.equals("Zoko", ignoreCase = true) && it.isDub }

            val zokoSubUrl = "https://api.anikage.workers.dev/stream/mal/$idMal/$episode/sub"
            val zokoDubUrl = "https://api.anikage.workers.dev/stream/mal/$idMal/$episode/dub"

            if (zokoSubIndex != -1) {
                list[zokoSubIndex] = list[zokoSubIndex].copy(
                    url = zokoSubUrl,
                    isDefault = true
                )
            } else {
                list.add(0, StreamServer(
                    name = "Zoko",
                    type = "sub",
                    format = "v-sub",
                    url = zokoSubUrl,
                    isDefault = true
                ))
            }

            if (zokoDubIndex != -1) {
                list[zokoDubIndex] = list[zokoDubIndex].copy(
                    url = zokoDubUrl,
                    isDefault = true
                )
            } else {
                list.add(1.coerceAtMost(list.size), StreamServer(
                    name = "Zoko",
                    type = "dub",
                    format = "v-dub",
                    url = zokoDubUrl,
                    isDefault = true
                ))
            }
        } else {
            // No MAL ID: Drop Zoko as default or remove if invalid, and designate next server
            for (i in list.indices) {
                if (list[i].name.equals("Zoko", ignoreCase = true)) {
                    list[i] = list[i].copy(isDefault = false)
                }
            }
            // Set next server as default for sub
            val nextSub = list.firstOrNull { it.isSub && !it.name.equals("Zoko", ignoreCase = true) }
            if (nextSub != null) {
                val idx = list.indexOf(nextSub)
                list[idx] = nextSub.copy(isDefault = true)
            }
        }

        return list
    }

    fun pickInitialServer(servers: List<StreamServer>, audioMode: AudioMode): StreamServer? {
        val lastPicked = prefs.lastServerName

        // Filter by chosen audio mode (SUB or DUB)
        val modeServers = servers.filter {
            if (audioMode == AudioMode.SUB) it.isSub else it.isDub
        }.filter { !it.isFailed }

        if (modeServers.isEmpty()) {
            return servers.firstOrNull { !it.isFailed }
        }

        // Try user's last picked server name first if available
        if (!lastPicked.isNullOrBlank()) {
            val matched = modeServers.firstOrNull { it.name.equals(lastPicked, ignoreCase = true) }
            if (matched != null) return matched
        }

        // Otherwise prefer default (Zoko or fallback default)
        return modeServers.firstOrNull { it.isDefault } ?: modeServers.firstOrNull()
    }

    fun getNextFallbackServer(
        currentServer: StreamServer,
        allServers: List<StreamServer>,
        audioMode: AudioMode
    ): StreamServer? {
        currentServer.isFailed = true

        val modeServers = allServers.filter {
            (if (audioMode == AudioMode.SUB) it.isSub else it.isDub) && !it.isFailed
        }

        return modeServers.firstOrNull() ?: allServers.firstOrNull { !it.isFailed }
    }

    fun saveLastPickedServer(serverName: String, audioMode: AudioMode) {
        prefs.lastServerName = serverName
        prefs.lastAudioMode = audioMode
    }

    fun getLastAudioMode(): AudioMode = prefs.lastAudioMode

    suspend fun prefetchNextEpisode(anilistId: Int, nextEpisode: Int, idMal: Int?) = withContext(Dispatchers.IO) {
        try {
            getStreams(anilistId, nextEpisode, idMal)
        } catch (_: Exception) {
            // Quiet prefetch
        }
    }
}

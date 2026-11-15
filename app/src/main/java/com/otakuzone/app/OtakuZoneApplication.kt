package com.otakuzone.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.otakuzone.app.data.api.ApiClient
import com.otakuzone.app.data.local.OtakuDatabase
import com.otakuzone.app.data.local.PreferencesManager
import com.otakuzone.app.data.repository.AniListSyncRepository
import com.otakuzone.app.data.repository.AnimeRepository
import com.otakuzone.app.data.repository.MangaRepository
import com.otakuzone.app.data.repository.StreamRepository
import com.otakuzone.app.worker.AiringCheckWorker

class OtakuZoneApplication : Application(), ImageLoaderFactory {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var database: OtakuDatabase
        private set

    lateinit var apiClient: ApiClient
        private set

    lateinit var animeRepository: AnimeRepository
        private set

    lateinit var streamRepository: StreamRepository
        private set

    lateinit var syncRepository: AniListSyncRepository
        private set

    lateinit var mangaRepository: MangaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferencesManager = PreferencesManager(this)
        database = OtakuDatabase.getInstance(this)
        apiClient = ApiClient(preferencesManager)

        animeRepository = AnimeRepository(apiClient.anilistService, apiClient.gson)
        streamRepository = StreamRepository(apiClient.aniKageService, preferencesManager)
        syncRepository = AniListSyncRepository(apiClient.anilistService, preferencesManager, apiClient.gson)
        mangaRepository = MangaRepository(apiClient, database.mangaBookmarkDao(), apiClient.gson)

        // Schedule periodic notification check for airing anime
        AiringCheckWorker.schedule(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB
                    .build()
            }
            .respectCacheHeaders(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: OtakuZoneApplication
            private set
    }
}

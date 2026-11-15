package com.otakuzone.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.otakuzone.app.data.api.ApiClient
import com.otakuzone.app.data.local.MangaBookmarkDao
import com.otakuzone.app.data.local.MangaBookmarkEntity
import com.otakuzone.app.data.models.ChapterImageItem
import com.otakuzone.app.data.models.MangaChapter
import com.otakuzone.app.data.models.MangaDetails
import com.otakuzone.app.data.models.MangaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MangaRepository(
    private val apiClient: ApiClient,
    private val bookmarkDao: MangaBookmarkDao,
    private val gson: Gson
) {

    fun getBookmarksFlow(): Flow<List<MangaBookmarkEntity>> = bookmarkDao.getAllBookmarksFlow()

    suspend fun getMangaHome(): Result<List<MangaItem>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.getMangaVaultService().getNatoHome()
            val dataObj = response.getAsJsonObject("data")

            val popularArray = dataObj?.getAsJsonArray("popular")
                ?: dataObj?.getAsJsonArray("trending")
                ?: dataObj?.getAsJsonArray("latest")

            if (popularArray != null) {
                val listType = object : TypeToken<List<MangaItem>>() {}.type
                val items: List<MangaItem> = gson.fromJson(popularArray, listType)
                Result.success(items)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchManga(keyword: String): Result<List<MangaItem>> = withContext(Dispatchers.IO) {
        try {
            // Try Comix search first, then Atsu search as fallback
            val service = apiClient.getMangaVaultService()
            try {
                val comixResp = service.searchComix(keyword)
                val itemsArray = comixResp.getAsJsonObject("data")?.getAsJsonArray("items")
                    ?: comixResp.getAsJsonObject("data")?.getAsJsonArray("result")
                if (itemsArray != null && itemsArray.size() > 0) {
                    val listType = object : TypeToken<List<MangaItem>>() {}.type
                    val list: List<MangaItem> = gson.fromJson(itemsArray, listType)
                    return@withContext Result.success(list)
                }
            } catch (_: Exception) {}

            val atsuResp = service.searchAtsu(keyword)
            val atsuData = atsuResp.getAsJsonArray("data")
            if (atsuData != null) {
                val listType = object : TypeToken<List<MangaItem>>() {}.type
                val list: List<MangaItem> = gson.fromJson(atsuData, listType)
                Result.success(list)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMangaDetails(slug: String): Result<MangaDetails> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.getMangaVaultService().getNatoDetails(slug)
            val dataObj = response.getAsJsonObject("data")
            if (dataObj != null) {
                val details: MangaDetails = gson.fromJson(dataObj, MangaDetails::class.java)
                Result.success(details)
            } else {
                Result.failure(Exception("Failed to parse manga details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChapterImages(mangaSlug: String, chapterSlug: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiClient.getMangaVaultService().getNatoChapterImages(mangaSlug, chapterSlug)
            val dataArray = response.getAsJsonArray("data")
            if (dataArray != null) {
                val urls = mutableListOf<String>()
                for (elem in dataArray) {
                    if (elem.isJsonPrimitive) {
                        urls.add(elem.asString)
                    } else if (elem.isJsonObject) {
                        val item = gson.fromJson(elem, ChapterImageItem::class.java)
                        val url = item.getImageUrl()
                        if (url.isNotBlank()) urls.add(url)
                    }
                }
                Result.success(urls)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReadingProgress(
        mangaSlug: String,
        mangaTitle: String,
        cover: String,
        chapterTitle: String,
        chapterSlug: String,
        pageIndex: Int,
        totalChapters: Int = 0
    ) = withContext(Dispatchers.IO) {
        bookmarkDao.saveBookmark(
            MangaBookmarkEntity(
                slug = mangaSlug,
                title = mangaTitle,
                cover = cover,
                lastReadChapterTitle = chapterTitle,
                lastReadChapterSlug = chapterSlug,
                lastReadPageIndex = pageIndex,
                totalChapters = totalChapters,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getBookmark(mangaSlug: String): MangaBookmarkEntity? = withContext(Dispatchers.IO) {
        bookmarkDao.getBookmark(mangaSlug)
    }

    suspend fun removeBookmark(mangaSlug: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(mangaSlug)
    }
}

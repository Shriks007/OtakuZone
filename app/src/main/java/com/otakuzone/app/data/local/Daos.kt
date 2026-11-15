package com.otakuzone.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {

    @Query("SELECT * FROM watch_progress ORDER BY updatedAt DESC")
    fun getAllProgressFlow(): Flow<List<WatchProgressEntity>>

    @Query("SELECT * FROM watch_progress WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    suspend fun getProgressForAnime(animeId: Int): List<WatchProgressEntity>

    @Query("SELECT * FROM watch_progress WHERE animeId = :animeId AND episodeNumber = :episodeNumber LIMIT 1")
    suspend fun getProgress(animeId: Int, episodeNumber: Int): WatchProgressEntity?

    @Query("SELECT * FROM watch_progress WHERE isCompleted = 0 ORDER BY updatedAt DESC LIMIT 20")
    fun getContinueWatchingFlow(): Flow<List<WatchProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(entity: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE animeId = :animeId")
    suspend fun clearAnimeProgress(animeId: Int)
}

@Dao
interface MangaBookmarkDao {

    @Query("SELECT * FROM manga_bookmarks ORDER BY updatedAt DESC")
    fun getAllBookmarksFlow(): Flow<List<MangaBookmarkEntity>>

    @Query("SELECT * FROM manga_bookmarks WHERE slug = :slug LIMIT 1")
    suspend fun getBookmark(slug: String): MangaBookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBookmark(entity: MangaBookmarkEntity)

    @Query("DELETE FROM manga_bookmarks WHERE slug = :slug")
    suspend fun deleteBookmark(slug: String)
}

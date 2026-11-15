package com.otakuzone.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_progress", primaryKeys = ["animeId", "episodeNumber"])
data class WatchProgressEntity(
    val animeId: Int,
    val episodeNumber: Int,
    val positionMs: Long,
    val durationMs: Long,
    val isCompleted: Boolean,
    val title: String,
    val coverUrl: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "manga_bookmarks")
data class MangaBookmarkEntity(
    @PrimaryKey val slug: String,
    val title: String,
    val cover: String,
    val lastReadChapterTitle: String? = null,
    val lastReadChapterSlug: String? = null,
    val lastReadPageIndex: Int = 0,
    val totalChapters: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

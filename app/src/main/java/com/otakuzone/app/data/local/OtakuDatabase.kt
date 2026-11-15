package com.otakuzone.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WatchProgressEntity::class, MangaBookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OtakuDatabase : RoomDatabase() {

    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun mangaBookmarkDao(): MangaBookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: OtakuDatabase? = null

        fun getInstance(context: Context): OtakuDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OtakuDatabase::class.java,
                    "otakuzone.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

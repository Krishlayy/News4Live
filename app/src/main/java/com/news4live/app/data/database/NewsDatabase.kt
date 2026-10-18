package com.news4live.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.news4live.app.data.database.dao.ArticleDao
import com.news4live.app.data.database.dao.BookmarkDao
import com.news4live.app.data.database.dao.CategoryDao
import com.news4live.app.data.database.dao.RecentSearchDao
import com.news4live.app.data.database.entities.ArticleEntity
import com.news4live.app.data.database.entities.BookmarkEntity
import com.news4live.app.data.database.entities.CategoryEntity
import com.news4live.app.data.database.entities.RecentSearchEntity

@Database(
    entities = [
        ArticleEntity::class,
        BookmarkEntity::class,
        CategoryEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getInstance(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news4live_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

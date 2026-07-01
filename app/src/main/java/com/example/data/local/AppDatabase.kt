package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CitizenReport
import com.example.data.model.DirectoryItem
import com.example.data.model.UserActivity

@Database(
    entities = [
        DirectoryItem::class,
        CitizenReport::class,
        UserActivity::class,
        com.example.data.model.SmartRoad::class,
        com.example.data.model.IptvChannel::class,
        com.example.data.model.DynamicButton::class,
        com.example.data.model.BrowserBookmark::class,
        com.example.data.model.BrowserHistory::class,
        com.example.data.model.DownloadTask::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun directoryDao(): DirectoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barishal_connect_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

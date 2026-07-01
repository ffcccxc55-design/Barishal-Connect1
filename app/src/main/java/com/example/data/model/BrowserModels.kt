package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "browser_bookmarks")
data class BrowserBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String
) : Serializable

@Entity(tableName = "browser_history")
data class BrowserHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey val id: String, // unique ID, e.g. UUID string or URL hash
    val title: String,
    val url: String,
    val filePath: String,
    val fileType: String, // "Video" or "Music"
    val resolutionOrQuality: String, // "360P", "128K", etc.
    val sizeBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val progress: Float = 0f, // 0.0 to 1.0
    val speed: String = "0 KB/s",
    val status: String = "DOWNLOADING" // DOWNLOADING, COMPLETED, FAILED, PAUSED
) : Serializable

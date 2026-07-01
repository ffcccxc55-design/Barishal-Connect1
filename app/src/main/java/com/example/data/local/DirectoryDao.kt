package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CitizenReport
import com.example.data.model.DirectoryItem
import com.example.data.model.UserActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface DirectoryDao {
    // Directory Items
    @Query("SELECT * FROM directory_items")
    fun getAllItems(): Flow<List<DirectoryItem>>

    @Query("SELECT * FROM directory_items WHERE category = :category")
    fun getItemsByCategory(category: String): Flow<List<DirectoryItem>>

    @Query("SELECT * FROM directory_items WHERE title LIKE '%' || :query || '%' OR subtitle LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<DirectoryItem>>

    @Query("SELECT * FROM directory_items WHERE isFavorite = 1")
    fun getFavoriteItems(): Flow<List<DirectoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<DirectoryItem>)

    @Query("UPDATE directory_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("DELETE FROM directory_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT COUNT(*) FROM directory_items")
    suspend fun getItemsCount(): Int

    // Citizen Reports
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCitizenReport(report: CitizenReport)

    @Update
    suspend fun updateCitizenReport(report: CitizenReport)

    @Query("SELECT * FROM citizen_reports ORDER BY timestamp DESC")
    fun getAllCitizenReports(): Flow<List<CitizenReport>>

    @Query("DELETE FROM citizen_reports WHERE id = :id")
    suspend fun deleteCitizenReport(id: Int)

    // User Activities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivity)

    @Query("SELECT * FROM user_activities ORDER BY timestamp DESC LIMIT 50")
    fun getAllActivities(): Flow<List<UserActivity>>

    @Query("DELETE FROM user_activities")
    suspend fun clearActivities()

    // Smart Roads
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartRoad(road: com.example.data.model.SmartRoad)

    @Query("SELECT * FROM smart_roads ORDER BY lastUpdated DESC")
    fun getAllSmartRoads(): Flow<List<com.example.data.model.SmartRoad>>

    @Query("DELETE FROM smart_roads WHERE id = :id")
    suspend fun deleteSmartRoad(id: String)

    // IPTV Channels
    @Query("SELECT * FROM iptv_channels")
    fun getAllIptvChannels(): Flow<List<com.example.data.model.IptvChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIptvChannels(channels: List<com.example.data.model.IptvChannel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIptvChannel(channel: com.example.data.model.IptvChannel)

    @Query("DELETE FROM iptv_channels WHERE id = :id")
    suspend fun deleteIptvChannel(id: Int)

    @Query("DELETE FROM iptv_channels")
    suspend fun clearIptvChannels()

    // Dynamic Buttons
    @Query("SELECT * FROM dynamic_buttons")
    fun getAllDynamicButtons(): Flow<List<com.example.data.model.DynamicButton>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDynamicButton(button: com.example.data.model.DynamicButton)

    @Query("DELETE FROM dynamic_buttons WHERE id = :id")
    suspend fun deleteDynamicButton(id: Int)

    // Browser Bookmarks
    @Query("SELECT * FROM browser_bookmarks")
    fun getAllBookmarks(): Flow<List<com.example.data.model.BrowserBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: com.example.data.model.BrowserBookmark)

    @Query("DELETE FROM browser_bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    // Browser History
    @Query("SELECT * FROM browser_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<com.example.data.model.BrowserHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: com.example.data.model.BrowserHistory)

    @Query("DELETE FROM browser_history")
    suspend fun clearHistory()

    // Download Tasks
    @Query("SELECT * FROM download_tasks ORDER BY id DESC")
    fun getAllDownloads(): Flow<List<com.example.data.model.DownloadTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(task: com.example.data.model.DownloadTask)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("UPDATE download_tasks SET progress = :progress, downloadedBytes = :downloadedBytes, speed = :speed, status = :status WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, progress: Float, downloadedBytes: Long, speed: String, status: String)
}

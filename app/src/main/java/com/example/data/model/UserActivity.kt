package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_activities")
data class UserActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityType: String, // SEARCH, VIEW
    val content: String, // search query or item name
    val timestamp: Long = System.currentTimeMillis()
)

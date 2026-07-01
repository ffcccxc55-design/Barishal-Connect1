package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "dynamic_buttons")
data class DynamicButton(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "url" or "iptv"
    val target: String, // Web link or Stream link
    val iconName: String = "Link"
) : Serializable

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "iptv_channels")
data class IptvChannel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val logo: String = "",
    val category: String = "Sports"
) : Serializable

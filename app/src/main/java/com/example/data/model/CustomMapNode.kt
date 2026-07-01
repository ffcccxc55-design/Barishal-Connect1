package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "custom_map_nodes")
data class CustomMapNode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fullName: String,
    val category: String, // "জেলা", "উপজেলা", "থানা"
    val description: String,
    val roadCondition: String,
    val distanceFromDhaka: String,
    val transitMedium: String,
    val hotspots: String,
    val xOffsetPercent: Float,
    val yOffsetPercent: Float,
    val isApproved: Boolean = false
) : Serializable

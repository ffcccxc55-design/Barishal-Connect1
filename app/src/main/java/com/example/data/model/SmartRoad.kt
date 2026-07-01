package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "smart_roads")
data class SmartRoad(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // Paved Road, Brick Road, Dirt Road, Semi Paved
    val width: String, // e.g. 12 feet, 20 feet
    val condition: String, // Excellent, Good, Damaged, Under Construction
    val description: String,
    val startPoint: String,
    val endPoint: String,
    val coordinatesJson: String, // Serialized list of coordinates
    val district: String,
    val upazila: String,
    val unionName: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectReason: String = "",
    val contributor: String = "Saki Ahmed",
    val approvedDate: String = "",
    val lastUpdated: String = "",
    val distance: Double = 0.0, // in km
    val durationSeconds: Int = 0,
    val imagesJson: String = "", // Serialized image list
    val reportsJson: String = "" // Serialized community report updates list
) : Serializable

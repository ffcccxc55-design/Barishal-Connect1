package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "directory_items")
data class DirectoryItem(
    @PrimaryKey val id: String,
    val category: String, // hospital, doctor, school, job, worker, bus, launch, tourist, market, road, gov, emergency
    val title: String, // e.g. Sher-e-Bangla Medical College
    val subtitle: String, // e.g. SBMCH Road, Barishal
    val description: String, // e.g. The largest government hospital in Barishal Division.
    val contactPhone: String = "",
    val location: String = "",
    val rating: Float = 4.5f,
    val priceOrFee: String = "", // e.g. 500 BDT or 68 BDT/kg
    val statusOrSchedule: String = "", // e.g. "8:30 PM" or "Smooth Traffic"
    val imageUrl: String = "",
    var isFavorite: Boolean = false,
    val status: String = "APPROVED", // PENDING, APPROVED, REJECTED
    val rejectReason: String = "",
    val contributor: String = "System",
    val district: String = "Barishal",
    val upazila: String = "Sadar",
    val unionName: String = "",
    val extraDataJson: String = "" // JSON representation of extra info
) : Serializable

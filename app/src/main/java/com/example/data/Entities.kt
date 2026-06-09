package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmer_profile")
data class FarmerProfile(
    @PrimaryKey val id: Int = 1,
    val fullName: String,
    val mobileNumber: String,
    val village: String,
    val district: String,
    val landArea: Double,
    val primaryCrops: String,
    val selectedLanguage: String = "MARATHI"
)

@Entity(tableName = "crop_records")
data class CropRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val sowingDate: String,
    val expectedHarvest: Double,
    val actualHarvest: Double = 0.0,
    val status: String = "ACTIVE" // "ACTIVE" or "HARVESTED"
)

@Entity(tableName = "financial_logs")
data class FinancialLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val date: String,
    val description: String
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorVillage: String,
    val questionText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val commentsJson: String = "[]" // JSON array of Comments
)

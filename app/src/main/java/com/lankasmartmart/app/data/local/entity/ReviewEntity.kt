package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long
)

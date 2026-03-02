package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_filters")
data class SearchFilterEntity(
    @PrimaryKey
    val filterId: String,
    val name: String,
    val type: String, // e.g., "brand", "price_range", "color"
    val value: String
)

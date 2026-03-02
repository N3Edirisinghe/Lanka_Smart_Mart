package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey
    val wishlistItemId: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val productPrice: Double,
    val userId: String,
    val addedAt: Long = System.currentTimeMillis()
)

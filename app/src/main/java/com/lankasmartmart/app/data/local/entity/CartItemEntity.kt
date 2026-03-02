package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val cartItemId: String,
    val productId: String,
    val productName: String,
    val productImage: String,
    val productPrice: Double,
    val quantity: Int,
    val userId: String,
    val addedAt: Long = System.currentTimeMillis()
)

package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promo_codes")
data class PromoCodeEntity(
    @PrimaryKey
    val code: String,
    val discountPercentage: Int,
    val expiryTimestamp: Long
)

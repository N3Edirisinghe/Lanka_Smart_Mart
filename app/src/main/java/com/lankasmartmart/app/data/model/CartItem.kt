package com.lankasmartmart.app.data.model

import com.google.firebase.Timestamp

data class CartItem(
    val cartItemId: String = "",
    val productId: String = "",
    val product: Product? = null,
    val quantity: Int = 1,
    val userId: String = "",
    val addedAt: Timestamp = Timestamp.now()
) {
    fun getTotalPrice(): Double {
        return (product?.price ?: 0.0) * quantity
    }
}

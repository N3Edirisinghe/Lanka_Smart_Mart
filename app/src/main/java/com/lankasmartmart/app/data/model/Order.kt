package com.lankasmartmart.app.data.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "Pending", // Pending, Processing, Delivered, Cancelled
    val timestamp: Long = System.currentTimeMillis(),
    val shippingAddress: Address = Address(),
    val paymentMethod: String = "COD",
    val isPaid: Boolean = false,
    val discount: Double = 0.0,
    val pointsRedeemed: Int = 0
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = ""
)



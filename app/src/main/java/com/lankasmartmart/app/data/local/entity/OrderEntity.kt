package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lankasmartmart.app.data.local.Converters
import com.lankasmartmart.app.data.model.Address
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.data.model.OrderItem

@Entity(tableName = "orders")
@TypeConverters(Converters::class)
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val userId: String,
    val items: List<OrderItem>,
    val totalPrice: Double,
    val status: String,
    val timestamp: Long,
    val paymentMethod: String,
    val isPaid: Boolean,
    val discount: Double = 0.0,
    val pointsRedeemed: Int = 0,
    
    // Address fields flattened or we can use the converter if we prefer. 
    // Using Converter for simplicity if Address structure changes often, 
    // but Embedded is better for querying if needed.
    // Given the plan to use Embedded in plan but I implemented a Converter for Address in Converters.kt.
    // Let's stick to Converter for Address for now to avoid field name conflicts with Embedded
    // or we can just use the Converter already defined.
    val shippingAddress: Address,
    
    // Sync status
    val isSynced: Boolean = false
) {
    fun toOrder(): Order {
        return Order(
            orderId = orderId,
            userId = userId,
            items = items,
            totalPrice = totalPrice,
            status = status,
            timestamp = timestamp,
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            isPaid = isPaid,
            discount = discount,
            pointsRedeemed = pointsRedeemed
        )
    }
}

fun Order.toEntity(isSynced: Boolean = false): OrderEntity {
    return OrderEntity(
        orderId = orderId,
        userId = userId,
        items = items,
        totalPrice = totalPrice,
        status = status,
        timestamp = timestamp,
        shippingAddress = shippingAddress,
        paymentMethod = paymentMethod,
        isPaid = isPaid,
        discount = discount,
        pointsRedeemed = pointsRedeemed,
        isSynced = isSynced
    )
}

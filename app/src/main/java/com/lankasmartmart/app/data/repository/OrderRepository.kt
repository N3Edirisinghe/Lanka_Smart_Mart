package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(order: Order): Resource<String>
    fun getOrders(userId: String): Flow<Resource<List<Order>>>
    
    suspend fun getOrderById(orderId: String): Resource<Order>

    // Admin Methods
    fun getAllOrders(): Flow<Resource<List<Order>>>
    suspend fun updateOrderStatus(orderId: String, status: String): Resource<Boolean>
    
    // Sync
    suspend fun syncPendingOrders()
}

package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItemEntity>>
    suspend fun addToCart(cartItem: CartItemEntity)
    suspend fun removeFromCart(productId: String)
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun clearCart()
    suspend fun syncCart()
    val contextUserId: String
    
    val isRedeemingPoints: kotlinx.coroutines.flow.StateFlow<Boolean>
    fun setRedeemingPoints(redeem: Boolean)
}

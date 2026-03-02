package com.lankasmartmart.app.data.local.dao

import androidx.room.*
import com.lankasmartmart.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: String): Flow<List<CartItemEntity>>
    
    @Query("SELECT * FROM cart_items WHERE productId = :productId AND userId = :userId")
    suspend fun getCartItemByProductId(productId: String, userId: String): CartItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId AND userId = :userId")
    suspend fun updateQuantity(productId: String, quantity: Int, userId: String)
    
    @Query("DELETE FROM cart_items WHERE productId = :productId AND userId = :userId")
    suspend fun deleteCartItem(productId: String, userId: String)
    
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)
}

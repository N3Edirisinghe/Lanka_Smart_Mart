package com.lankasmartmart.app.data.local.dao

import androidx.room.*
import com.lankasmartmart.app.data.local.entity.WishlistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId")
    fun getWishlistItems(userId: String): Flow<List<WishlistItemEntity>>
    
    @Query("SELECT * FROM wishlist_items WHERE productId = :productId AND userId = :userId")
    suspend fun getWishlistItemByProductId(productId: String, userId: String): WishlistItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(wishlistItem: WishlistItemEntity)
    
    @Delete
    suspend fun deleteWishlistItem(wishlistItem: WishlistItemEntity)
    
    @Query("DELETE FROM wishlist_items WHERE productId = :productId AND userId = :userId")
    suspend fun deleteWishlistItemByProductId(productId: String, userId: String)
    
    @Query("DELETE FROM wishlist_items WHERE userId = :userId")
    suspend fun clearWishlist(userId: String)
}

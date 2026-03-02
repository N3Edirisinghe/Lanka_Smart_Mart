package com.lankasmartmart.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lankasmartmart.app.data.local.dao.CartDao
import com.lankasmartmart.app.data.local.entity.CartItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CartRepository {

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "guest"

    override val contextUserId: String
        get() = currentUserId

    override fun getCartItems(): Flow<List<CartItemEntity>> {
        return cartDao.getCartItems(currentUserId)
    }

    override suspend fun addToCart(cartItem: CartItemEntity) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            val userId = currentUserId
            // 1. Update Local
            val existingItem = cartDao.getCartItemByProductId(cartItem.productId, userId)
            val finalQuantity = if (existingItem != null) {
                existingItem.quantity + cartItem.quantity
            } else {
                cartItem.quantity
            }
            
            val itemToSave = if (existingItem != null) {
                 existingItem.copy(quantity = finalQuantity)
            } else {
                 cartItem.copy(userId = userId)
            }
            
            cartDao.insertCartItem(itemToSave)
            
            // 2. Update Cloud (if logged in)
            if (userId != "guest") {
                try {
                    firestore.collection("users").document(userId)
                        .collection("cartItems").document(itemToSave.productId)
                        .set(itemToSave, SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun removeFromCart(productId: String) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            // 1. Update Local
            cartDao.deleteCartItem(productId, currentUserId)
            
            // 2. Update Cloud
            if (currentUserId != "guest") {
                try {
                    firestore.collection("users").document(currentUserId)
                        .collection("cartItems").document(productId)
                        .delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            // 1. Update Local
            cartDao.updateQuantity(productId, quantity, currentUserId)
            
            // 2. Update Cloud
            if (currentUserId != "guest") {
                try {
                    firestore.collection("users").document(currentUserId)
                        .collection("cartItems").document(productId)
                        .update("quantity", quantity)
                } catch (e: Exception) {
                     e.printStackTrace()
                }
            }
        }
    }

    override suspend fun clearCart() {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            // 1. Update Local
            cartDao.clearCart(currentUserId)
            
            // 2. Update Cloud
            if (currentUserId != "guest") {
                try {
                    val batch = firestore.batch()
                    val snapshot = firestore.collection("users").document(currentUserId)
                        .collection("cartItems").get().await()
                    
                    snapshot.documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun syncCart() {
        if (currentUserId == "guest") return
        
        try {
            android.util.Log.d("CartRepository", "Syncing cart for user $currentUserId")
            val snapshot = firestore.collection("users").document(currentUserId)
                .collection("cartItems").get().await()
            
            if (!snapshot.isEmpty) {
                // Server wins strategy: fetch cloud cart and replace/merge local
                // Or merge: combine local (guest cart) + cloud cart?
                // For simplicity, let's say we fetch cloud cart and add it to local.
                
                val cloudItems = snapshot.toObjects(CartItemEntity::class.java)
                
                cloudItems.forEach { item ->
                    // Insert or Update local
                    // We might want to handle conflicts (e.g. same product in local session and cloud)
                    // Simple merge: if exists, sum quantities? Or overwrite? 
                    // Overwrite is standard "restore" behavior.
                    cartDao.insertCartItem(item)
                }
                android.util.Log.d("CartRepository", "Synced ${cloudItems.size} items from cloud")
            }
        } catch (e: Exception) {
            android.util.Log.e("CartRepository", "Error syncing cart", e)
        }
    }

    private val _isRedeemingPoints = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isRedeemingPoints: kotlinx.coroutines.flow.StateFlow<Boolean> = _isRedeemingPoints

    override fun setRedeemingPoints(redeem: Boolean) {
        _isRedeemingPoints.value = redeem
    }
}

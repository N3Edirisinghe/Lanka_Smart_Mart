package com.lankasmartmart.app.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.lankasmartmart.app.data.model.Address
import com.lankasmartmart.app.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    
    suspend fun signIn(email: String, password: String): Result<AuthResult>
    
    suspend fun signUp(email: String, password: String, name: String): Result<AuthResult>
    
    suspend fun signOut()
    
    suspend fun getUserProfile(userId: String): Result<User>
    
    suspend fun resetPassword(email: String): Result<Unit>
    
    suspend fun signInWithGoogle(idToken: String): Result<AuthResult>
    
    suspend fun addAddress(userId: String, address: Address): Result<Unit>
    suspend fun removeAddress(userId: String, addressId: String): Result<Unit>
    
    suspend fun updateUserProfile(userId: String, name: String, phone: String, imageUrl: String? = null): Result<Unit>
    
    suspend fun reloadCurrentUser(): Result<Unit>
    
    suspend fun toggleFavorite(userId: String, productId: String): Result<Boolean> // Returns true if added, false if removed
    
    fun getUserAddressesFlow(userId: String): Flow<List<Address>>
    
    fun getUserFavoritesFlow(userId: String): Flow<List<String>>
    
    fun getUserFlow(userId: String): Flow<User?>
    
    suspend fun updateLoyaltyPoints(userId: String, newPoints: Int): Result<Unit>
}

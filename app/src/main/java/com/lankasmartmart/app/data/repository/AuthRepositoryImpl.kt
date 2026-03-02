package com.lankasmartmart.app.data.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lankasmartmart.app.data.model.Address
import com.lankasmartmart.app.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    private val LOGIN_TIMEOUT = 20_000L
    private val TIMEOUT = 60_000L // 60s for sign-up / Google sign-in (slow networks)

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return try {
            kotlinx.coroutines.withTimeout(LOGIN_TIMEOUT) {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Result.success(result)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Connection timed out. Check your internet and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<AuthResult> {
        return try {
            kotlinx.coroutines.withTimeout(TIMEOUT) {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Set Display Name on Firebase User
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()

                    // Create user profile in Firestore
                    val user = User(
                        userId = firebaseUser.uid,
                        name = name,
                        email = email
                    )
                    // We don't want to fail the whole signup if firestore fails, but we should try
                    try {
                        firestore.collection("users").document(firebaseUser.uid).set(user).await()
                    } catch (e: Exception) {
                        // Log failure but allow auth success
                    }
                }
                
                Result.success(result)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Connection timed out. Check your internet and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            kotlinx.coroutines.withTimeout(TIMEOUT) {
                val document = firestore.collection("users").document(userId).get().await()
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Connection timed out. Check your internet and try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val apiUrl = "https://lanka-smart-mart.vercel.app/api/send-reset-email"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                }.toString()

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Result.success(Unit)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("AuthRepository", "Failed to send custom email: response code $responseCode, error: $errorResponse")
                    // If custom Vercel email fails for some reason, fallback to Firebase default
                    auth.sendPasswordResetEmail(email).await()
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error in resetPassword", e)
            try {
                // Ultimate Fallback
                auth.sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (fallbackEx: Exception) {
                Result.failure(fallbackEx)
            }
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthResult> {
        return try {
            Log.d("Auth", "signInWithGoogle: starting (timeout ${TIMEOUT}ms)")
            kotlinx.coroutines.withTimeout(TIMEOUT) {
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                Log.d("Auth", "signInWithGoogle: success ${result.user?.uid}")
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    // Launch Firestore update in a separate coroutine so it doesn't block login
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try {
                            val userRef = firestore.collection("users").document(firebaseUser.uid)
                            val userDoc = userRef.get().await()

                            if (!userDoc.exists()) {
                                val user = User(
                                    userId = firebaseUser.uid,
                                    name = firebaseUser.displayName ?: "User",
                                    email = firebaseUser.email ?: ""
                                )
                                userRef.set(user).await()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Result.success(result)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.w("Auth", "signInWithGoogle timed out", e)
            Result.failure(Exception("Connection timed out. Check your internet and try again."))
        } catch (e: Exception) {
            Log.e("Auth", "signInWithGoogle failed", e)
            Result.failure(e)
        }
    }

    override suspend fun addAddress(userId: String, address: Address): Result<Unit> {
        return try {
            kotlinx.coroutines.withTimeout(TIMEOUT) {
                val userRef = firestore.collection("users").document(userId)
                val snapshot = userRef.get().await()
                val user = snapshot.toObject(User::class.java)
                
                if (user != null) {
                    val updatedAddresses = user.addresses + address
                    userRef.update("addresses", updatedAddresses).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("User not found"))
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Connection timed out. Check your internet and try again."))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error adding address", e)
            Result.failure(e)
        }
    }

    override suspend fun removeAddress(userId: String, addressId: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val snapshot = userRef.get().await()
            val user = snapshot.toObject(User::class.java)
            
            if (user != null) {
                val updatedAddresses = user.addresses.filter { it.id != addressId }
                userRef.update("addresses", updatedAddresses).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(userId: String, name: String, phone: String, imageUrl: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "phoneNumber" to phone
            )
            if (imageUrl != null) {
                updates["profileImageUrl"] = imageUrl
            }
            
            firestore.collection("users").document(userId).set(updates, SetOptions.merge()).await()
            
            // Also update Firebase Auth profile if name or photo changed
            val user = auth.currentUser
            if (user != null) {
                val profileUpdatesBuilder = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                
                if (imageUrl != null) {
                    profileUpdatesBuilder.setPhotoUri(android.net.Uri.parse(imageUrl))
                }
                
                user.updateProfile(profileUpdatesBuilder.build()).await()
                user.reload().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reloadCurrentUser(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.reload().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleFavorite(userId: String, productId: String): Result<Boolean> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val snapshot = userRef.get().await()
            val user = snapshot.toObject(User::class.java)

            if (user != null) {
                if (user.favorites.contains(productId)) {
                    // Remove
                    userRef.update("favorites", com.google.firebase.firestore.FieldValue.arrayRemove(productId)).await()
                    Result.success(false)
                } else {
                    // Add
                    userRef.update("favorites", com.google.firebase.firestore.FieldValue.arrayUnion(productId)).await()
                    Result.success(true)
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserAddressesFlow(userId: String): kotlinx.coroutines.flow.Flow<List<Address>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    trySend(user?.addresses ?: emptyList())
                } else {
                    trySend(emptyList()) // User doc doesn't exist yet
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }

    override fun getUserFavoritesFlow(userId: String): kotlinx.coroutines.flow.Flow<List<String>> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    trySend(user?.favorites ?: emptyList())
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }

    override fun getUserFlow(userId: String): kotlinx.coroutines.flow.Flow<User?> = kotlinx.coroutines.flow.callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(User::class.java))
                } else {
                    trySend(null)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun updateLoyaltyPoints(userId: String, newPoints: Int): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("loyaltyPoints", newPoints).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

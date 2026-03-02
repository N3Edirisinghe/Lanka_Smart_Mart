package com.lankasmartmart.app.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.tasks.await
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun uploadImage(imageUri: Uri, productId: String): Resource<String> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val storageRef = storage.reference.child("product_images/$productId/${System.currentTimeMillis()}.jpg")
                
                // Put file
                storageRef.putFile(imageUri).await()
                
                // Get download URL with retry for eventual consistency
                var downloadUrl: android.net.Uri? = null
                var attempt = 0
                while (attempt < 3) {
                    try {
                        downloadUrl = storageRef.downloadUrl.await()
                        break
                    } catch (e: Exception) {
                        attempt++
                        if (attempt >= 3) throw e
                        kotlinx.coroutines.delay(1000)
                    }
                }
                
                Resource.Success(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error(e.message ?: "Image upload failed")
            }
        }
    }

    override suspend fun uploadImageStream(inputStream: java.io.InputStream, productId: String): Resource<String> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val storageRef = storage.reference.child("product_images/$productId/${System.currentTimeMillis()}.jpg")
                
                // Put Stream
                storageRef.putStream(inputStream).await()
                
                // Get download URL with retry for eventual consistency
                var downloadUrl: android.net.Uri? = null
                var attempt = 0
                while (attempt < 3) {
                    try {
                        downloadUrl = storageRef.downloadUrl.await()
                        break
                    } catch (e: Exception) {
                        attempt++
                        if (attempt >= 3) throw e
                        kotlinx.coroutines.delay(1000)
                    }
                }
                
                Resource.Success(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error(e.message ?: "Image upload failed")
            }
        }
    }

    override suspend fun uploadProfileImage(imageUri: Uri, userId: String): Resource<String> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val storageRef = storage.reference.child("profile_images/${userId}_${System.currentTimeMillis()}.jpg")
                
                // Use putFile instead of byte array to cleanly handle empty or invalid files
                // and to prevent 'Object does not exist at location' errors on subsequent downloadUrl calls.
                storageRef.putFile(imageUri).await()
                
                // Get the download URL after successful upload with retry
                var downloadUrl: android.net.Uri? = null
                var attempt = 0
                while (attempt < 3) {
                    try {
                        downloadUrl = storageRef.downloadUrl.await()
                        break
                    } catch (e: Exception) {
                        attempt++
                        if (attempt >= 3) throw e
                        kotlinx.coroutines.delay(1000)
                    }
                }
                
                Resource.Success(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                val friendlyMessage = if (e.message?.contains("Object does not exist at location", ignoreCase = true) == true) {
                    "Profile image not found on server. Please choose a different photo."
                } else {
                    e.message ?: "Profile image upload failed"
                }
                Resource.Error(friendlyMessage)
            }
        }
    }
}

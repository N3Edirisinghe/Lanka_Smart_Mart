package com.lankasmartmart.app.data.repository

import android.net.Uri
import com.lankasmartmart.app.util.Resource

interface ImageRepository {
    suspend fun uploadImage(imageUri: Uri, productId: String): Resource<String>
    suspend fun uploadImageStream(inputStream: java.io.InputStream, productId: String): Resource<String>
    suspend fun uploadProfileImage(imageUri: Uri, userId: String): Resource<String>
}

package com.lankasmartmart.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.repository.AuthRepository
import com.lankasmartmart.app.data.repository.ImageRepository
import com.lankasmartmart.app.data.model.User
import android.net.Uri
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState.asStateFlow()
    
    // Hold selected image URI temporarily
    var selectedImageUri: Uri? = null

    init {
        loadUserProfile()
    }

    fun retryLoading() {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            val user = authRepository.currentUser
            
            if (user != null) {
                // 1. Emit optimistic data from Firebase Auth immediately
                val authUser = User(
                    userId = user.uid,
                    name = user.displayName ?: "",
                    email = user.email ?: "",
                    phoneNumber = user.phoneNumber ?: "",
                    profileImageUrl = user.photoUrl?.toString() ?: ""
                )
                _userState.value = Resource.Success(authUser)

                // 2. Fetch full profile from Firestore in background
                try {
                    val result = authRepository.getUserProfile(user.uid)
                    if (result.isSuccess) {
                        _userState.value = Resource.Success(result.getOrNull()!!)
                    }
                    // If failed, we just keep the auth data
                } catch (e: Exception) {
                    // Ignore
                }
            } else {
                _userState.value = Resource.Error("User not logged in")
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            val user = authRepository.currentUser
            
            if (user != null) {
                try {
                    var imageUrl: String? = null

                    // Upload image if selected.
                    // If the upload fails, we continue and still update name/phone,
                    // leaving the existing profile image unchanged.
                    if (selectedImageUri != null) {
                        val imageResult = imageRepository.uploadProfileImage(selectedImageUri!!, user.uid)
                        if (imageResult is Resource.Success) {
                            imageUrl = imageResult.data
                        }
                        // If image upload fails, we silently skip it and proceed with profile update
                        // This prevents "Object does not exist at location" errors from blocking saves
                    }

                    val result = authRepository.updateUserProfile(user.uid, name, phone, imageUrl)
                    
                    if (result.isSuccess) {
                        _updateState.value = Resource.Success(Unit)
                        loadUserProfile() // Refresh data
                    } else {
                        _updateState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Update failed")
                    }
                } catch (e: Exception) {
                     _updateState.value = Resource.Error(e.message ?: "An unexpected error occurred")
                }
            } else {
                _updateState.value = Resource.Error("User not logged in")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = null
        selectedImageUri = null
    }
}

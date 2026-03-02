package com.lankasmartmart.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.User
import com.lankasmartmart.app.data.repository.AuthRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userState: StateFlow<Resource<User>> = _userState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _userState.value = Resource.Loading()
            
            // Reload user to get latest photo from Auth (in case it was just updated)
            authRepository.reloadCurrentUser()
            
            val user = authRepository.currentUser
            if (user != null) {
                // 1. Emit optimistic data from Firebase Auth immediately (including photo from Auth)
                val authUser = User(
                    userId = user.uid,
                    name = user.displayName ?: "User",
                    email = user.email ?: "",
                    phoneNumber = user.phoneNumber ?: "",
                    profileImageUrl = user.photoUrl?.toString() ?: ""
                )
                _userState.value = Resource.Success(authUser)

                // 2. Fetch full profile from Firestore and merge with Auth (so photo always shows)
                try {
                    val result = authRepository.getUserProfile(user.uid)
                    if (result.isSuccess) {
                        val firestoreUser = result.getOrNull()!!
                        // Always prefer Firestore's profileImageUrl if it exists (it's the source of truth)
                        // Only fall back to Auth's photoUrl if Firestore doesn't have one
                        val photoUrl = when {
                            !firestoreUser.profileImageUrl.isNullOrBlank() -> firestoreUser.profileImageUrl
                            !authUser.profileImageUrl.isNullOrBlank() -> authUser.profileImageUrl
                            else -> ""
                        }
                        _userState.value = Resource.Success(
                            firestoreUser.copy(profileImageUrl = photoUrl)
                        )
                    }
                } catch (e: Exception) {
                    // Ignore failure, keep showing auth data
                }
            } else {
                _userState.value = Resource.Error("User not logged in")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

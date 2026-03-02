package com.lankasmartmart.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.repository.AuthRepository
import com.lankasmartmart.app.data.repository.CartRepository
import com.lankasmartmart.app.data.repository.ProductRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val loginState: StateFlow<Resource<Boolean>> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val signupState: StateFlow<Resource<Boolean>> = _signupState.asStateFlow()

    private val _currentUserState = MutableStateFlow(repository.currentUser != null)
    val currentUserState: StateFlow<Boolean> = _currentUserState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("Please enter email and password")
            return
        }

        // Safety timeout: clear "Authenticating..." if Firebase never returns (main thread stays free)
        val timeoutJob: Job = viewModelScope.launch {
            delay(30_000)
            if (_loginState.value is Resource.Loading) {
                _loginState.value = Resource.Error("Connection timed out. Check your internet and try again.")
            }
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            try {
                val result = withContext(Dispatchers.IO) { repository.signIn(email, password) }
                timeoutJob.cancel()
                if (_loginState.value is Resource.Loading) {
                    if (result.isSuccess) {
                        _loginState.value = Resource.Success(true)
                        _currentUserState.value = true
                        syncUserData()
                    } else {
                        _loginState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Login failed")
                    }
                }
            } catch (e: Exception) {
                timeoutJob.cancel()
                if (_loginState.value is Resource.Loading) {
                    _loginState.value = Resource.Error(e.message ?: "Connection timed out. Try again.")
                }
            }
        }
    }

    fun signup(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _signupState.value = Resource.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _signupState.value = Resource.Loading()
            val result = repository.signUp(email, password, name)
            if (result.isSuccess) {
                _signupState.value = Resource.Success(true)
                _currentUserState.value = true
                syncUserData()
            } else {
                _signupState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            cartRepository.clearCart() // Clear local cart for privacy
            // productRepository.clearLocalData() // Optional: clear cached products or just reload
            // productRepository.refreshProducts() // To update favorites
            _currentUserState.value = false
        }
    }
    
    fun resetState() {
        _loginState.value = Resource.Success(false)
        _signupState.value = Resource.Success(false)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            _signupState.value = Resource.Loading()

            val result = repository.signInWithGoogle(idToken)
            
            if (result.isSuccess) {
                _currentUserState.value = true
                _loginState.value = Resource.Success(true)
                _signupState.value = Resource.Success(true)
                syncUserData()
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                _loginState.value = Resource.Error(errorMessage)
                _signupState.value = Resource.Error(errorMessage)
            }
        }
    }

    private val _resetPasswordState = MutableStateFlow<Resource<Boolean>>(Resource.Success(false))
    val resetPasswordState: StateFlow<Resource<Boolean>> = _resetPasswordState.asStateFlow()

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = Resource.Error("Please enter your email")
            return
        }
        viewModelScope.launch {
            _resetPasswordState.value = Resource.Loading()
            val result = repository.resetPassword(email)
            if (result.isSuccess) {
                _resetPasswordState.value = Resource.Success(true)
            } else {
                _resetPasswordState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }
    
    fun resetPasswordStateReset() {
        _resetPasswordState.value = Resource.Success(false)
    }

    private suspend fun syncUserData() {
        try {
            cartRepository.syncCart()
            productRepository.refreshProducts()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

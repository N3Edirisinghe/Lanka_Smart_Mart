package com.lankasmartmart.app.presentation.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Address
import com.lankasmartmart.app.data.repository.AuthRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _addressesState = MutableStateFlow<Resource<List<Address>>>(Resource.Loading())
    val addressesState: StateFlow<Resource<List<Address>>> = _addressesState

    private val _addAddressState = MutableStateFlow<Resource<Unit>?>(null)
    val addAddressState: StateFlow<Resource<Unit>?> = _addAddressState

    val primaryAddress: StateFlow<Address?> = _addressesState
        .map { if (it is Resource.Success) it.data?.firstOrNull() else null }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        observeAddresses()
    }

    private fun observeAddresses() {
        val user = authRepository.currentUser
        if (user != null) {
            viewModelScope.launch {
                authRepository.getUserAddressesFlow(user.uid)
                    .catch { e ->
                        _addressesState.value = Resource.Error(e.message ?: "Failed to load addresses")
                    }
                    .collect { addresses ->
                        _addressesState.value = Resource.Success(addresses)
                    }
            }
        } else {
            _addressesState.value = Resource.Error("User not logged in")
        }
    }

    // loadAddresses is used for Retry functionality
    fun loadAddresses() {
        observeAddresses()
    }

    fun addAddress(label: String, name: String, street: String, city: String, phone: String) {
        val user = authRepository.currentUser
        if (user != null) {
            viewModelScope.launch {
                _addAddressState.value = Resource.Loading()
                val newAddress = Address(
                    id = UUID.randomUUID().toString(),
                    label = label,
                    name = name,
                    street = street,
                    city = city,
                    phone = phone
                )
                val result = authRepository.addAddress(user.uid, newAddress)
                if (result.isSuccess) {
                    _addAddressState.value = Resource.Success(Unit)
                    loadAddresses() // Refresh list
                } else {
                    _addAddressState.value = Resource.Error(result.exceptionOrNull()?.message ?: "Failed to add address")
                }
            }
        } else {
             _addAddressState.value = Resource.Error("User not logged in")
        }
    }

    fun deleteAddress(addressId: String) {
         val user = authRepository.currentUser
        if (user != null) {
            viewModelScope.launch {
                 val result = authRepository.removeAddress(user.uid, addressId)
                 if (result.isSuccess) {
                     loadAddresses()
                 }
            }
        }
    }
    
    fun resetAddAddressState() {
        _addAddressState.value = null
    }
}

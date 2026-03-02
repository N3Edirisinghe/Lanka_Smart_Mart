package com.lankasmartmart.app.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.data.repository.CartRepository
import com.lankasmartmart.app.data.repository.OrderRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository // To get userId
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<Order>>> = _ordersState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadOrders()
    }

    fun refreshOrders() {
        loadOrders(isRefresh = true)
    }

    private fun loadOrders(isRefresh: Boolean = false) {
        val userId = cartRepository.contextUserId
        if (userId.isNotEmpty()) {
            viewModelScope.launch {
                if (isRefresh) _isRefreshing.value = true
                else _ordersState.value = Resource.Loading()

                try {
                    orderRepository.getOrders(userId).collect { resource ->
                        _ordersState.value = resource
                        
                        // Hide refresh indicator when we get data
                        if (resource !is Resource.Loading) {
                            _isRefreshing.value = false
                        }
                    }
                } catch (e: Exception) {
                    _ordersState.value = Resource.Error(e.message ?: "An error occurred")
                    _isRefreshing.value = false
                }
            }
        } else {
            _ordersState.value = Resource.Error("User not logged in")
        }
    }
}

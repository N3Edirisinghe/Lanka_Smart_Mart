package com.lankasmartmart.app.presentation.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.data.repository.OrderRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _orderState = MutableStateFlow<Resource<Order>>(Resource.Loading())
    val orderState: StateFlow<Resource<Order>> = _orderState

    private val orderId: String = savedStateHandle["orderId"] ?: ""

    init {
        if (orderId.isNotEmpty()) {
            getOrderDetails(orderId)
        }
    }

    private fun getOrderDetails(id: String) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading()
            try {
                kotlinx.coroutines.withTimeout(15000L) {
                    val result = orderRepository.getOrderById(id)
                    _orderState.value = result
                }
            } catch (e: Exception) {
                _orderState.value = Resource.Error("Connection timed out.")
            }
        }
    }
}

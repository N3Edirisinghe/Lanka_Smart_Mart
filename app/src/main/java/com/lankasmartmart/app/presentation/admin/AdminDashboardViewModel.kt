package com.lankasmartmart.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.data.repository.OrderRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<Order>>> = _ordersState.asStateFlow()

    private val _updateStatusState = MutableStateFlow<Resource<Boolean>?>(null)
    val updateStatusState: StateFlow<Resource<Boolean>?> = _updateStatusState.asStateFlow()

    init {
        fetchAllOrders()
    }

    fun fetchAllOrders() {
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { result ->
                _ordersState.value = result
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _updateStatusState.value = Resource.Loading()
            val result = orderRepository.updateOrderStatus(orderId, newStatus)
            _updateStatusState.value = result
            
            if (result is Resource.Success) {
                // Refresh list or update locally
                fetchAllOrders()
            }
        }
    }
    
    fun resetUpdateStatus() {
        _updateStatusState.value = null
    }
}

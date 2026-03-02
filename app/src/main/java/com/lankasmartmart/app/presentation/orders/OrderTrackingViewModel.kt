package com.lankasmartmart.app.presentation.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

// Simple data class to replace Google Maps LatLng
data class LatLng(val latitude: Double, val longitude: Double)

@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: com.lankasmartmart.app.data.repository.OrderRepository
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""

    // User Location (e.g., Colombo)
    private val _userLocation = MutableStateFlow(LatLng(6.9271, 79.8612))
    val userLocation = _userLocation.asStateFlow()

    // Rider Location (Starts at Store/Start of route)
    private val _riderLocation = MutableStateFlow(LatLng(6.9350, 79.8500))
    val riderLocation = _riderLocation.asStateFlow()
    
    // Status
    private val _deliveryStatus = MutableStateFlow("Loading...")
    val deliveryStatus = _deliveryStatus.asStateFlow()
    
    private val _estimatedTime = MutableStateFlow("--")
    val estimatedTime = _estimatedTime.asStateFlow()

    // Route Path for Polyline
    private val _routePath = MutableStateFlow<List<LatLng>>(emptyList())
    val routePath = _routePath.asStateFlow()
    
    // Simulation Job
    private var simulationJob: kotlinx.coroutines.Job? = null

    init {
        setupDeliveryRoute()
        loadOrderDetails()
    }

    private fun setupDeliveryRoute() {
        // Mock a realistic route in Colombo
        val fullPath = listOf(
            LatLng(6.9350, 79.8500), // Start (Pettah area)
            LatLng(6.9345, 79.8510),
            LatLng(6.9340, 79.8520),
            LatLng(6.9335, 79.8530),
            LatLng(6.9330, 79.8540), // Moving south-east
            LatLng(6.9325, 79.8550),
            LatLng(6.9320, 79.8560),
            LatLng(6.9315, 79.8570),
            LatLng(6.9310, 79.8580), // Near destination
            LatLng(6.9300, 79.8590),
            LatLng(6.9290, 79.8600),
            LatLng(6.9280, 79.8605),
            LatLng(6.9275, 79.8610),
            LatLng(6.9271, 79.8612) // User Location
        )
        _routePath.value = fullPath
    }
    
    private fun loadOrderDetails() {
        viewModelScope.launch {
            repository.getOrderById(orderId).let { result ->
                if (result is com.lankasmartmart.app.util.Resource.Success) {
                    val order = result.data
                    if (order != null) {
                        updateStatusUI(order.status)
                    }
                } else {
                     _deliveryStatus.value = "Order not found"
                }
            }
        }
    }
    
    private fun updateStatusUI(status: String) {
        _deliveryStatus.value = status
        
        when (status) {
            "Pending" -> {
                _deliveryStatus.value = "Order Placed"
                _estimatedTime.value = "Waiting..."
                stopSimulation()
                resetRiderPosition()
            }
            "Processing" -> {
                _deliveryStatus.value = "Preparing your order"
                _estimatedTime.value = "Packing..."
                stopSimulation()
                resetRiderPosition()
            }
            "Out for Delivery", "On Way", "Shipped" -> {
                _deliveryStatus.value = "Out for delivery"
                startSimulation()
            }
            "Delivered" -> {
                _deliveryStatus.value = "Delivered"
                _estimatedTime.value = "Arrived"
                stopSimulation()
                // Move rider to end
                _routePath.value.lastOrNull()?.let { _riderLocation.value = it }
            }
            "Cancelled" -> {
                _deliveryStatus.value = "Order Cancelled"
                _estimatedTime.value = "-"
                stopSimulation()
            }
            else -> {
                _deliveryStatus.value = status
                stopSimulation()
            }
        }
    }
    
    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }
    
    private fun resetRiderPosition() {
        // Reset to start of route
        _routePath.value.firstOrNull()?.let { _riderLocation.value = it }
    }

    private fun startSimulation() {
        if (simulationJob?.isActive == true) return // Already running
        
        simulationJob = viewModelScope.launch {
            val path = _routePath.value
            var totalTimeSteps = path.size
            
            for ((index, point) in path.withIndex()) {
                 _riderLocation.value = point
                 
                 // Update Status (Dynamic within simulation)
                 if (index == path.size - 2) _deliveryStatus.value = "Arriving soon"
                 
                 // Update ETA
                 val remainingSteps = totalTimeSteps - index
                 if (remainingSteps <= 0) {
                     _estimatedTime.value = "Now"
                 } else {
                     _estimatedTime.value = "${remainingSteps * 2} min" // Mock 2 min per step
                 }

                 delay(2000) // Update every 2 seconds
            }
            _deliveryStatus.value = "Arrived at your location"
        }
    }
}

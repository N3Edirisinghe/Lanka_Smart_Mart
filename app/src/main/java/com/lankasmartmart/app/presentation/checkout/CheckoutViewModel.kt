package com.lankasmartmart.app.presentation.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Address
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.data.model.OrderItem
import com.lankasmartmart.app.data.repository.AuthRepository
import com.lankasmartmart.app.data.repository.CartRepository
import com.lankasmartmart.app.data.repository.OrderRepository
import com.lankasmartmart.app.data.repository.StripeRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val stripeRepository: StripeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var name by mutableStateOf("")
    var street by mutableStateOf("")
    var city by mutableStateOf("")
    var phone by mutableStateOf("")
    
    var paymentMethod by mutableStateOf("COD") // Options: COD, Card, Bybit
    var bybitTransactionId by mutableStateOf("")
    // Stripe Params
    var cardParams: com.stripe.android.model.PaymentMethodCreateParams? by mutableStateOf(null)

    private val _orderState = MutableStateFlow<Resource<String>?>(null)
    val orderState: StateFlow<Resource<String>?> = _orderState.asStateFlow()

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()

    private val _addressesState = MutableStateFlow<List<Address>>(emptyList())
    val addressesState: StateFlow<List<Address>> = _addressesState.asStateFlow()

    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()
    
    fun selectAddress(address: Address) {
        _selectedAddress.value = address
        name = address.name.ifBlank { name } // Keep existing if blank, or update? usually update.
        street = address.street
        city = address.city
        phone = address.phone
    }

    init {
        calculateTotal()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val userId = cartRepository.contextUserId
            if (userId != "guest") {
                val result = authRepository.getUserProfile(userId)
                result.onSuccess { user ->
                    val addresses = user.addresses
                    _addressesState.value = addresses
                    if (addresses.isNotEmpty()) {
                        _selectedAddress.value = addresses.find { it.isDefault } ?: addresses.first()
                    } else {
                         // Pre-fill name/phone if no address
                        name = user.name
                        phone = user.phoneNumber
                    }
                }
            }
        }
    }

    private fun calculateTotal() {
        viewModelScope.launch {
            val items = cartRepository.getCartItems().first()
            val subtotal = items.sumOf { it.productPrice * it.quantity }
            
            // Apply Discount if applicable
            val isRedeeming = cartRepository.isRedeemingPoints.first()
            var discount = 0.0
            
            if (isRedeeming) {
                // Fetch latest points directly or rely on flow if we had it observed.
                // For safety in Checkout, fetch fresh user data or assume AuthRepository flows are fast.
                // Let's rely on authRepository.
                val userId = cartRepository.contextUserId
                if (userId != "guest") {
                   val userResult = authRepository.getUserProfile(userId)
                    userResult.onSuccess { user ->
                        val points = user.loyaltyPoints
                        val maxDiscount = points.toDouble()
                        discount = if (maxDiscount > subtotal) subtotal else maxDiscount
                    }
                }
            }
            
            _cartTotal.value = if (subtotal - discount < 0) 0.0 else subtotal - discount
        }
    }

    fun placeOrder() {
        if (name.isBlank() || street.isBlank() || city.isBlank() || phone.isBlank()) {
            _orderState.value = Resource.Error("Please fill all fields")
            return
        }
        
        // Ensure total is calculated correctly before placing
        // Recalculating here to be sure, or trusting _cartTotal
        
        if (paymentMethod == "COD") {
            submitOrder(isPaid = false, paymentId = "COD")
        } else if (paymentMethod == "Bybit") {
            if (bybitTransactionId.isBlank()) {
                _orderState.value = Resource.Error("Please enter Transaction ID")
                return
            }
            submitOrder(isPaid = false, paymentId = "Bybit: $bybitTransactionId")
        } else {
             _orderState.value = Resource.Error("Please click Pay to proceed with card payment")
        }
    }
    
    // Call this when PaymentSheet completes successfully
    fun onPaymentSuccess(paymentId: String) {
        submitOrder(isPaid = true, paymentId = "Card: $paymentId")
    }

    fun onPaymentFailure(message: String) {
        _orderState.value = Resource.Error("Payment Failed: $message")
    }

    private fun submitOrder(isPaid: Boolean, paymentId: String) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading()
            
            val cartItems = cartRepository.getCartItems().first()
            if (cartItems.isEmpty()) {
                _orderState.value = Resource.Error("Cart is empty")
                return@launch
            }
            
            // Calculate Discount
            val subtotal = cartItems.sumOf { it.productPrice * it.quantity }
            val isRedeeming = cartRepository.isRedeemingPoints.first()
            var discount = 0.0
            var pointsRedeemed = 0
            
             if (isRedeeming) {
                val userId = cartRepository.contextUserId
                if (userId != "guest") {
                   val userResult = authRepository.getUserProfile(userId)
                    userResult.onSuccess { user ->
                        val points = user.loyaltyPoints
                        val maxDiscount = points.toDouble()
                        discount = if (maxDiscount > subtotal) subtotal else maxDiscount
                        pointsRedeemed = discount.toInt()
                    }
                }
            }
            
            val finalTotal = if (subtotal - discount < 0) 0.0 else subtotal - discount

            val orderItems = cartItems.map { 
                OrderItem(
                    productId = it.productId,
                    productName = it.productName,
                    productPrice = it.productPrice,
                    quantity = it.quantity,
                    imageUrl = it.productImage
                ) 
            }

            val order = Order(
                userId = cartRepository.contextUserId,
                items = orderItems,
                totalPrice = finalTotal,
                shippingAddress = _selectedAddress.value ?: Address(name = name, street = street, city = city, phone = phone),
                status = if (isPaid) "Processing" else "Pending",
                paymentMethod = paymentId,
                isPaid = isPaid,
                discount = discount,
                pointsRedeemed = pointsRedeemed
            )

            val result = orderRepository.placeOrder(order)
            android.util.Log.e("CheckoutViewModel", "Order Result: ${result::class.simpleName} - Data/Error: ${result.data} / ${result.message}")
            if (result is Resource.Success) {
                cartRepository.clearCart()
                // Reset redeeming state
                cartRepository.setRedeemingPoints(false)
                
                // Deduct Points
                if (pointsRedeemed > 0) {
                     deductUserPoints(cartRepository.contextUserId, pointsRedeemed)
                }
            }
            _orderState.value = result
        }
    }
    
    private suspend fun deductUserPoints(userId: String, points: Int) {
         val userResult = authRepository.getUserProfile(userId)
         userResult.onSuccess { user ->
             val newPoints = (user.loyaltyPoints - points).coerceAtLeast(0)
             authRepository.updateLoyaltyPoints(userId, newPoints)
         }
    }
    
    // New Function to fetch Payment Intent
    fun fetchPaymentIntent(onResult: (clientSecret: String) -> Unit) {
        viewModelScope.launch {
            _orderState.value = Resource.Loading()
            // Recalculate total for Stripe
             val items = cartRepository.getCartItems().first()
            val subtotal = items.sumOf { it.productPrice * it.quantity }
            val isRedeeming = cartRepository.isRedeemingPoints.first()
            var discount = 0.0
            
             if (isRedeeming) {
                val userId = cartRepository.contextUserId
                if (userId != "guest") {
                   val userResult = authRepository.getUserProfile(userId)
                    userResult.onSuccess { user ->
                        val points = user.loyaltyPoints
                        val maxDiscount = points.toDouble()
                        discount = if (maxDiscount > subtotal) subtotal else maxDiscount
                    }
                }
            }
            val total = if (subtotal - discount < 0) 0.0 else subtotal - discount
            
            if (total <= 0) {
                 _orderState.value = Resource.Error("Invalid Total")
                 return@launch
            }
            
            // "lkr" is currency
            val result = stripeRepository.createPaymentIntent(total, "lkr")
            if (result is Resource.Success) {
                _orderState.value = null // Reset loading
                onResult(result.data!!)
            } else {
                _orderState.value = Resource.Error(result.message ?: "Failed to initialize payment")
            }
        }
    }
    
    fun resetOrderState() {
        _orderState.value = null
    }
}

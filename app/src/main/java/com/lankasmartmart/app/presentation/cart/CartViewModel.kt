package com.lankasmartmart.app.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.local.entity.CartItemEntity
import com.lankasmartmart.app.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.lankasmartmart.app.data.model.Product
import java.util.UUID

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: com.lankasmartmart.app.data.repository.CartRepository,
    private val authRepository: com.lankasmartmart.app.data.repository.AuthRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CartItemEntity>> =  repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.productPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User Points
    val userPoints: StateFlow<Int> = authRepository.getUserFlow(repository.contextUserId)
        .map { it?.loyaltyPoints ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isRedeemingPoints: StateFlow<Boolean> = repository.isRedeemingPoints
    
    // Calculate Discount and Grand Total
    val discountAmount: StateFlow<Double> = kotlinx.coroutines.flow.combine(
        totalPrice,
        userPoints,
        isRedeemingPoints
    ) { total, points, isRedeeming ->
        if (isRedeeming) {
            // 1 Point = 1 LKR logic
            // Cannot redeem more than the total price
            val maxDiscount = points.toDouble()
            if (maxDiscount > total) total else maxDiscount
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val grandTotal: StateFlow<Double> = kotlinx.coroutines.flow.combine(
        totalPrice,
        discountAmount
    ) { total, discount ->
        if (total - discount < 0) 0.0 else total - discount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun toggleRedeemPoints(redeem: Boolean) {
        repository.setRedeemingPoints(redeem)
    }

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            val cartItem = CartItemEntity(
                cartItemId = UUID.randomUUID().toString(),
                productId = product.productId,
                productName = product.name,
                productImage = product.imageUrl,
                productPrice = product.price,
                quantity = quantity,
                userId = repository.contextUserId
            )
            repository.addToCart(cartItem)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            if (quantity > 0) {
                repository.updateQuantity(productId, quantity)
            } else {
                repository.removeFromCart(productId)
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }
}

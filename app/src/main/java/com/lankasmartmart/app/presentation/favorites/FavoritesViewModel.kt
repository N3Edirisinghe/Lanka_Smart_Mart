package com.lankasmartmart.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            productRepository.getFavoriteProducts().collect { resource ->
                if (resource is com.lankasmartmart.app.util.Resource.Success) {
                    _favorites.value = resource.data ?: emptyList()
                }
            }
        }
    }
    
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            productRepository.toggleFavorite(product.productId)
        }
    }
}

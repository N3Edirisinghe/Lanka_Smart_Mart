package com.lankasmartmart.app.presentation.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.repository.ProductRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<Product>>> = _productsState.asStateFlow()

    private val categoryName: String = savedStateHandle["categoryName"] ?: ""

    init {
        if (categoryName.isNotEmpty()) {
            getProductsByCategory(categoryName)
        }
    }

    private fun getProductsByCategory(category: String) {
        viewModelScope.launch {
            productRepository.getProductsByCategory(category).collectLatest {
                _productsState.value = it
            }
        }
    }
}

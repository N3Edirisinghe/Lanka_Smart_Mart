package com.lankasmartmart.app.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.repository.ProductRepository
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val favoritesState: StateFlow<Resource<List<Product>>> = _favoritesState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavoriteProducts().collect {
                _favoritesState.value = it
            }
        }
    }
}

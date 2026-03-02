package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<Product>>>
    
    fun getProductsByCategory(category: String): Flow<Resource<List<Product>>>

    fun getFavoriteProducts(): Flow<Resource<List<Product>>>
    
    suspend fun getProductById(productId: String): Resource<Product>
    
    fun searchProducts(query: String): Flow<Resource<List<Product>>>
    
    suspend fun refreshProducts(): Result<Unit>
    
    suspend fun clearLocalData()
    
    suspend fun storeLocally(products: List<Product>)

    suspend fun updateProduct(product: Product): Resource<Unit>

    suspend fun toggleFavorite(productId: String): Resource<Unit>
}

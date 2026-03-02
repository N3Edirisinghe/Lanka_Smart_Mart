package com.lankasmartmart.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lankasmartmart.app.data.local.dao.ProductDao
import com.lankasmartmart.app.data.local.entity.ProductEntity
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.model.ProductCategory
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val productDao: ProductDao,
    private val auth: FirebaseAuth
) : ProductRepository {

    override fun getProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Observe local DB
            productDao.getAllProducts().collect { entities ->
                val products = entities.map { it.toProduct() }
                android.util.Log.d("ProductRepository", "Emitting ${products.size} products from local DB")
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error reading local DB", e)
            emit(Resource.Error(e.message ?: "Local DB Error"))
        }
    }

    override fun getProductsByCategory(category: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        try {
            // Observe local DB (Offline First)
            productDao.getProductsByCategory(category).collect { entities ->
                val products = entities.map { it.toProduct() }
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error reading local DB for category $category", e)
            emit(Resource.Error(e.message ?: "Local DB Error"))
        }
    }

    override fun getFavoriteProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        try {
            productDao.getFavoriteProducts().collect { entities ->
                val products = entities.map { it.toProduct() }
                // No need to fetch from cloud specifically for favorites, local source logic is sufficient for this offline-first app
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error reading favorites", e)
            emit(Resource.Error(e.message ?: "Error loading favorites"))
        }
    }

    override suspend fun getProductById(productId: String): Resource<Product> {
        return try {
            // Try local first
            val localEntity = productDao.getProductById(productId)
            if (localEntity != null) {
                return Resource.Success(localEntity.toProduct())
            }
            
            // Try remote if missing
            val doc = firestore.collection("products").document(productId).get().await()
            val product = doc.toObject(Product::class.java)?.copy(productId = doc.id)
            
            if (product != null) {
                Resource.Success(product)
            } else {
                Resource.Error("Product not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching product")
        }
    }

    override fun searchProducts(query: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        try {
            productDao.searchProducts(query).collect { entities ->
                val products = entities.map { it.toProduct() }
                emit(Resource.Success(products))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error searching products", e)
            emit(Resource.Error(e.message ?: "Search failed"))
        }
    }

    // DTO for safe Firestore parsing
    private data class NetworkProduct(
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null,
        val category: String? = null,
        val imageUrl: String? = null,
        val rating: Double? = null,
        val stock: Int? = null,
        @get:com.google.firebase.firestore.PropertyName("favorite")
        @set:com.google.firebase.firestore.PropertyName("favorite")
        var isFavorite: Boolean? = null
    )

    private fun NetworkProduct.toProduct(id: String): Product {
        // Safe mapping with defaults
        val safeCategory = try {
            if (category != null) ProductCategory.valueOf(category) else ProductCategory.OTHER
        } catch (e: Exception) {
            // Try formatting (e.g. "Vegetables" -> "VEGETABLES") or fallback
            try {
                ProductCategory.valueOf(category!!.uppercase())
            } catch (e2: Exception) {
                ProductCategory.OTHER
            }
        }

        return Product(
            productId = id,
            name = name ?: "Unknown Product",
            description = description ?: "",
            price = price ?: 0.0,
            category = safeCategory,
            imageUrl = imageUrl ?: "",
            rating = rating ?: 0.0,
            stock = stock ?: 0,
            isFavorite = isFavorite ?: false
        )
    }

    override suspend fun refreshProducts(): Result<Unit> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                android.util.Log.d("ProductRepository", "Refreshing products from Firestore (Robust Mode)...")
                
                // 1. Fetch Products
                val productSnapshot = firestore.collection("products").get().await()
                android.util.Log.d("ProductRepository", "Firestore fetched ${productSnapshot.size()} documents")
                
                // 2. Fetch User Favorites (if logged in)
                val user = auth.currentUser
                val favoriteIds = if (user != null) {
                    try {
                        val userDoc = firestore.collection("users").document(user.uid).get().await()
                        // Safe cast to List<String>
                        val favList = userDoc.get("favorites") as? List<String> ?: emptyList()
                        favList.toSet()
                    } catch (e: Exception) {
                        android.util.Log.e("ProductRepository", "Error fetching favorites", e)
                        emptySet()
                    }
                } else {
                    emptySet()
                }

                val products = productSnapshot.documents.mapNotNull { doc ->
                    try {
                        // Use lenient DTO first
                        val networkProduct = doc.toObject(NetworkProduct::class.java)
                        val product = networkProduct?.toProduct(doc.id)
                        
                        // Override favorite status with user-specific data
                        product?.copy(isFavorite = favoriteIds.contains(doc.id))
                    } catch (e: Exception) {
                        android.util.Log.e("ProductRepository", "Error deserializing product ${doc.id}", e)
                        null
                    }
                }
                
                // Save to local DB
                val entities = products.map { it.toEntity() }
                productDao.insertProducts(entities)
                android.util.Log.d("ProductRepository", "Inserted ${entities.size} products into local DB")
                
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("ProductRepository", "Error refreshing products", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun clearLocalData() {
        try {
           productDao.deleteAllProducts()
           android.util.Log.d("ProductRepository", "Cleared local database")
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error clearing local DB", e)
        }
    }

    override suspend fun storeLocally(products: List<Product>) {
        try {
            val entities = products.map { it.toEntity() }
            productDao.insertProducts(entities)
            android.util.Log.d("ProductRepository", "Stored ${entities.size} products locally via storeLocally")
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error storing locally", e)
        }
    }

    override suspend fun updateProduct(product: Product): Resource<Unit> {
        return try {
            // Update local DB
            productDao.insertProducts(listOf(product.toEntity()))
            
            // Update Firestore Global Config (Should only be done by Admin in real app, but key for this implementation)
             firestore.collection("products").document(product.productId).set(product).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update product")
        }
    }

    override suspend fun toggleFavorite(productId: String): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("User not logged in")
            
            // 1. Get current local state
            val localEntity = productDao.getProductById(productId) ?: return Resource.Error("Product not found locally")
            val newParams = !localEntity.isFavorite
            
            // 2. Optimistic Update Local
            productDao.insertProducts(listOf(localEntity.copy(isFavorite = newParams)))
            
            // 3. Update Firestore User Favorites
            val userRef = firestore.collection("users").document(user.uid)
            
            if (newParams) {
                userRef.update("favorites", com.google.firebase.firestore.FieldValue.arrayUnion(productId)).await()
            } else {
                userRef.update("favorites", com.google.firebase.firestore.FieldValue.arrayRemove(productId)).await()
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ProductRepository", "Error toggling favorite", e)
            // Revert local change if needed, but for now just error
            Resource.Error(e.message ?: "Failed to toggle favorite")
        }
    }

    // Mappers
    private fun ProductEntity.toProduct(): Product {
        return Product(
            productId = productId,
            name = name,
            description = description,
            price = price,
            category = try { ProductCategory.valueOf(category) } catch (e: Exception) { ProductCategory.OTHER },
            imageUrl = imageUrl,
            rating = rating,
            stock = stock,
            isFavorite = isFavorite
        )
    }

    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            productId = productId,
            name = name,
            description = description,
            price = price,
            category = category.name,
            imageUrl = imageUrl,
            rating = rating,
            stock = stock,
            isFavorite = isFavorite
        )
    }
}

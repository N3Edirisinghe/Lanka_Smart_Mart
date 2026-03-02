package com.lankasmartmart.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.model.ProductCategory
import com.lankasmartmart.app.data.repository.ProductRepository
import com.lankasmartmart.app.data.repository.ImageRepository // New import
import com.lankasmartmart.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val imageRepository: ImageRepository,
    private val firestore: FirebaseFirestore // Injecting directly for seeding helper
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<Product>>> = _productsState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String>("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        loadProducts()
    }

    private var isSeeding = false

    fun loadProducts() {
        android.util.Log.d("ProductViewModel", "loadProducts called")
        
        // Cancel previous observation
        searchJob?.cancel()
        
        // Trigger network refresh in background (independent of UI flow)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val result = repository.refreshProducts()
                if (result.isFailure) {
                    android.util.Log.e("ProductViewModel", "Refresh failed", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductViewModel", "Refresh exception", e)
            }
        }

        searchJob = viewModelScope.launch {
            repository.getProducts().collect { resource ->
                if (resource is Resource.Success && resource.data.isNullOrEmpty() && !isSeeding) {
                    // Only seed if we successfully loaded but have no data AND are not already seeding
                    performHardReset()
                }
                _productsState.value = resource
            }
        }
    }

    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        _searchQuery.value = "" // Clear search when category changes
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (category == "All" || category == "All Products") {
                android.util.Log.d("ProductViewModel", "Filtering: Showing All")
                repository.getProducts().collect { _productsState.value = it }
            } else {
                android.util.Log.d("ProductViewModel", "Filtering: Showing category $category")
                repository.getProductsByCategory(category).collect { _productsState.value = it }
            }
        }
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
        _selectedCategory.value = "All" // Reset category when searching
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                repository.getProducts().collect { _productsState.value = it }
            } else {
                repository.searchProducts(query).collect { _productsState.value = it }
            }
        }
    }
    
    // forceful reset to ensure bad data is gone
    fun performHardReset() {
        if (isSeeding) return // Double check
        isSeeding = true
        android.util.Log.d("ProductViewModel", "PERFORMING HARD RESET OF DATA...")
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 0. Clear local DB first to remove stale data with old IDs
                try {
                    repository.clearLocalData()
                } catch (e: Exception) {
                    android.util.Log.e("ProductViewModel", "Error clearing local DB during reset", e)
                }

                // 1. Define sample data with IDs
                val sampleProducts = listOf(
                    Product(
                        productId = "p1",
                        name = "Sony WH-1000XM5",
                        description = "Industry-leading noise canceling headphones with Auto NC Optimizer",
                        price = 85000.0,
                        category = ProductCategory.ELECTRONICS,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_sony_headphones",
                        rating = 4.8,
                        stock = 15
                    ),
                    Product(
                        productId = "p2",
                        name = "Samsung Galaxy S24 Ultra",
                        description = "AI-powered smartphone with 200MP camera and S Pen",
                        price = 450000.0,
                        category = ProductCategory.ELECTRONICS,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_samsung_s24",
                        rating = 4.9,
                        stock = 10
                    ),
                    Product(
                        productId = "p3",
                        name = "Atlas Chooty Pen Blue (Box)",
                        description = "Box of 20 Atlas Chooty pens, smooth writing for students",
                        price = 800.0,
                        category = ProductCategory.STATIONERY,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_atlas_pen",
                        rating = 4.5,
                        stock = 100
                    ),
                    Product(
                        productId = "p4",
                        name = "Munchee Super Cream Cracker 490g",
                        description = "The favorite biscuit of Sri Lanka, perfect with tea",
                        price = 450.0,
                        category = ProductCategory.GROCERIES,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_munchee_cracker",
                        rating = 4.7,
                        stock = 50
                    ),
                    Product(
                        productId = "p5",
                        name = "Men's Batik Shirt",
                        description = "Traditional Sri Lankan Batik shirt, pure cotton, casual wear",
                        price = 4500.0,
                        category = ProductCategory.FASHION,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_batik_shirt",
                        rating = 4.3,
                        stock = 25
                    ),
                    Product(
                        productId = "p6",
                        name = "LG 43-inch 4K Smart TV",
                        description = "Ultra HD LED Smart TV with webOS and Magic Remote",
                        price = 125000.0,
                        category = ProductCategory.ELECTRONICS,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_lg_tv",
                        rating = 4.6,
                        stock = 8
                    ),
                    Product(
                        productId = "p7",
                        name = "Fresh Banana 1kg",
                        description = "Organic fresh bananas",
                        price = 250.0,
                        category = ProductCategory.FRUITS,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_banana",
                        rating = 4.8,
                        stock = 50
                    ),
                    Product(
                        productId = "p8",
                        name = "Red Apple",
                        description = "Fresh red apples imported",
                        price = 120.0,
                        category = ProductCategory.FRUITS,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_apple",
                        rating = 4.9,
                        stock = 30
                    ),
                    Product(
                        productId = "p9",
                        name = "Bell Pepper",
                        description = "Fresh green and red bell peppers",
                        price = 300.0,
                        category = ProductCategory.VEGETABLES,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_bell_pepper",
                        rating = 4.5,
                        stock = 20
                    ),
                    Product(
                        productId = "p10",
                        name = "Fresh Milk 1L",
                        description = "Highland Fresh Milk",
                        price = 450.0,
                        category = ProductCategory.MILK_AND_EGG,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_highland_milk",
                        rating = 4.7,
                        stock = 50
                    ),
                    Product(
                        productId = "p11",
                        name = "Orange Juice",
                        description = "Freshly squeezed orange juice",
                        price = 600.0,
                        category = ProductCategory.BEVERAGES,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_orange_juice",
                        rating = 4.6,
                        stock = 15
                    ),
                    Product(
                        productId = "p12",
                        name = "Surf Excel 1kg",
                        description = "Washing powder for laundry",
                        price = 850.0,
                        category = ProductCategory.LAUNDRY,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_surf_excel",
                        rating = 4.8,
                        stock = 40
                    ),
                    Product(
                        productId = "p13",
                        name = "Farm Eggs (10 Pack)",
                        description = "Fresh brown eggs",
                        price = 650.0,
                        category = ProductCategory.MILK_AND_EGG,
                        imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_farm_eggs",
                        rating = 4.5,
                        stock = 60
                    ),
                    // Requested Vegetables
                    Product(productId = "v1", name = "Spinach", description = "Fresh green leafy spinach", price = 150.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_spinach", rating = 4.8, stock = 40),
                    Product(productId = "v2", name = "Lettuce", description = "Crisp green lettuce", price = 300.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_lettuce", rating = 4.7, stock = 30),
                    Product(productId = "v3", name = "Cabbage", description = "Fresh cabbage head", price = 200.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_cabbage", rating = 4.6, stock = 50),
                    Product(productId = "v4", name = "Kale", description = "Nutritious kale leaves", price = 350.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_kale", rating = 4.9, stock = 20),
                    Product(productId = "v5", name = "Carrot 500g", description = "Organic orange carrots", price = 220.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_carrot", rating = 4.8, stock = 60),
                    Product(productId = "v6", name = "Beetroot 500g", description = "Fresh red beetroot", price = 180.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_beetroot", rating = 4.7, stock = 45),
                    Product(productId = "v7", name = "Radish", description = "White radish", price = 160.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_radish", rating = 4.5, stock = 30),
                    Product(productId = "v8", name = "Turnip", description = "Fresh turnip", price = 190.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_turnip", rating = 4.4, stock = 25),
                    Product(productId = "v9", name = "Potato 1kg", description = "Local potatoes", price = 280.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_potato", rating = 4.8, stock = 100),
                    Product(productId = "v10", name = "Coriander", description = "Fresh coriander leaves", price = 120.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_coriander", rating = 4.6, stock = 40),
                    Product(productId = "v11", name = "Red Onion 500g", description = "Local red onions", price = 450.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_onion", rating = 4.7, stock = 80),
                    Product(productId = "v12", name = "Garlic 250g", description = "Fresh garlic bulbs", price = 300.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_garlic", rating = 4.8, stock = 60),
                    Product(productId = "v13", name = "Leek", description = "Fresh leeks", price = 240.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_leek", rating = 4.6, stock = 40),
                    Product(productId = "v14", name = "Spring Onion", description = "Fresh spring onions", price = 180.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_spring_onion", rating = 4.5, stock = 35),
                    Product(productId = "v15", name = "Asparagus", description = "Green asparagus bundle", price = 850.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_asparagus", rating = 4.9, stock = 15),
                    Product(productId = "v16", name = "Celery", description = "Fresh celery stalks", price = 400.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_celery", rating = 4.7, stock = 20),
                    Product(productId = "v17", name = "Mint", description = "Fresh mint leaves", price = 100.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_mint", rating = 4.8, stock = 50),
                    Product(productId = "v18", name = "Cauliflower", description = "Fresh cauliflower head", price = 550.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_cauliflower", rating = 4.7, stock = 25),
                    Product(productId = "v19", name = "Broccoli", description = "Green broccoli", price = 600.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_broccoli", rating = 4.8, stock = 20),
                    Product(productId = "v20", name = "Green Beans 500g", description = "Fresh green beans", price = 320.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_green_beans", rating = 4.6, stock = 45),
                    Product(productId = "v21", name = "Green Peas", description = "Fresh peas in pod", price = 480.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_peas", rating = 4.5, stock = 30),
                    Product(productId = "v22", name = "Okra 500g", description = "Fresh okra (ladies finger)", price = 250.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_okra", rating = 4.6, stock = 40),
                    Product(productId = "v23", name = "Bitter Gourd", description = "Fresh bitter gourd", price = 280.0, category = ProductCategory.VEGETABLES, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_bitter_gourd", rating = 4.4, stock = 35),
                    
                    // Household
                    Product(productId = "h1_v3", name = "Broom (Pol Kossa)", description = "Traditional coconut ekel broom", price = 350.0, category = ProductCategory.HOUSEHOLD, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_broom_real", rating = 4.2, stock = 50),
                    Product(productId = "h2_v3", name = "Mop with Handle", description = "Cotton mop with long handle", price = 650.0, category = ProductCategory.HOUSEHOLD, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_mop_real", rating = 4.0, stock = 30),
                    Product(productId = "h3_v3", name = "Dishwashing Sponge (3 Pack)", description = "Durable sponges for dishwashing", price = 150.0, category = ProductCategory.HOUSEHOLD, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_sponge_real", rating = 4.5, stock = 100),

                    // Personal Care
                    Product(productId = "pc1_v3", name = "Lifebuoy Soap 100g", description = "Germ protection soap bar", price = 110.0, category = ProductCategory.PERSONAL_CARE, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_soap_real", rating = 4.7, stock = 200),
                    Product(productId = "pc2_v3", name = "Sunsilk Shampoo 180ml", description = "Soft and smooth hair shampoo", price = 450.0, category = ProductCategory.PERSONAL_CARE, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_shampoo_real", rating = 4.6, stock = 80),
                    Product(productId = "pc3_v3", name = "Signal Toothpaste 160g", description = "Fluoride toothpaste for strong teeth", price = 220.0, category = ProductCategory.PERSONAL_CARE, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_toothpaste_real", rating = 4.8, stock = 150),
                    Product(productId = "pc4_v3", name = "Toothbrush", description = "Soft bristle toothbrush", price = 80.0, category = ProductCategory.PERSONAL_CARE, imageUrl = "android.resource://com.lankasmartmart.app/drawable/img_toothbrush_real", rating = 4.5, stock = 100)
                )

                // 2. ALWAYS seed local DB and UI FIRST (Offline Priority)
                try {
                    repository.storeLocally(sampleProducts)
                    // Force UI update from local (Repository emits it, but we can also set state)
                    _productsState.value = Resource.Success(sampleProducts)
                    android.util.Log.d("ProductViewModel", "Stored products locally and updated UI immediately")
                } catch (e: Exception) {
                    android.util.Log.e("ProductViewModel", "Error storing sample products locally", e)
                }

                // 3. Best-effort Firestore Sync (Background)
                try {
                    // Clear old firestore data
                    val snapshot = firestore.collection("products").get().await()
                    if (!snapshot.isEmpty) {
                        val batch = firestore.batch()
                        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
                        batch.commit().await()
                        android.util.Log.d("ProductViewModel", "Deleted ${snapshot.size()} existing products from Firestore")
                    }

                    // Sync new data
                    sampleProducts.forEach { product ->
                        firestore.collection("products").document(product.productId).set(product).await()
                        android.util.Log.d("ProductViewModel", "Synced to Firestore: ${product.name}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProductViewModel", "Firestore sync failed (continuing with local data)", e)
                }
            } finally {
                isSeeding = false
            }
        }
    }

    fun updateProductImage(productId: String, imageUri: android.net.Uri) {
        viewModelScope.launch {
            if (productId.isEmpty()) return@launch
            
            android.util.Log.d("ProductViewModel", "Starting image upload for $productId")
            
            // 1. Upload
            when (val uploadResult = imageRepository.uploadImage(imageUri, productId)) {
                is Resource.Success -> {
                    val downloadUrl = uploadResult.data
                    if (downloadUrl != null) {
                         android.util.Log.d("ProductViewModel", "Upload success: $downloadUrl")
                         
                         // 2. Update Product
                         val productResult = repository.getProductById(productId)
                         if (productResult is Resource.Success && productResult.data != null) {
                             val product = productResult.data!!
                             val updatedProduct = product.copy(imageUrl = downloadUrl)
                             
                             repository.updateProduct(updatedProduct)
                             android.util.Log.d("ProductViewModel", "Product updated with new URL")
                             
                             // Refresh list to show change
                             loadProducts()
                         } else {
                             android.util.Log.e("ProductViewModel", "Failed to find product to update")
                         }
                    }
                }
                is Resource.Error -> {
                    android.util.Log.e("ProductViewModel", "Upload failed: ${uploadResult.message}")
                }
                else -> {}
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            // Optimistic Local Update (Handled by Repository + DB Flow)
            repository.toggleFavorite(productId)
        }
    }

    fun seedCloudImages(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            android.util.Log.d("ProductViewModel", "STARTING CLOUD SEEDING...")
            
            val currentProducts = (productsState.value as? Resource.Success)?.data ?: return@launch
            
            // Filter products that have local android.resource URIs
            val productsToSeed = currentProducts.filter { it.imageUrl.startsWith("android.resource://") }
            
            android.util.Log.d("ProductViewModel", "Found ${productsToSeed.size} products to seed")
            
            productsToSeed.forEach { product ->
                try {
                    val uri = android.net.Uri.parse(product.imageUrl)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    
                    if (inputStream != null) {
                        android.util.Log.d("ProductViewModel", "Uploading ${product.name}...")
                        when (val result = imageRepository.uploadImageStream(inputStream, product.productId)) {
                            is Resource.Success -> {
                                val cloudUrl = result.data!!
                                android.util.Log.d("ProductViewModel", "Success: $cloudUrl")
                                
                                val updatedProduct = product.copy(imageUrl = cloudUrl)
                                repository.updateProduct(updatedProduct)
                            }
                            is Resource.Error -> {
                                android.util.Log.e("ProductViewModel", "Failed to upload ${product.name}: ${result.message}")
                            }
                            else -> {}
                        }
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProductViewModel", "Error seeding ${product.name}", e)
                }
            }
            
            android.util.Log.d("ProductViewModel", "CLOUD SEEDING COMPLETE")
            loadProducts() // Refresh to show new URLs (though they look the same)
        }
    }
}

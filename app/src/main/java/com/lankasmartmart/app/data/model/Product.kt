package com.lankasmartmart.app.data.model

import com.google.firebase.Timestamp

data class Product(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: ProductCategory = ProductCategory.OTHER,
    val imageUrl: String = "",
    val rating: Double = 0.0,
    val stock: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    @get:com.google.firebase.firestore.PropertyName("favorite")
    @set:com.google.firebase.firestore.PropertyName("favorite")
    var isFavorite: Boolean = false
)

// Category constants
enum class ProductCategory(val displayName: String) {
    ELECTRONICS("Electronics"),
    HOUSEHOLD("Household"),
    STATIONERY("Stationery"),
    PERSONAL_CARE("Personal Care"),
    FASHION("Fashion"),
    GROCERIES("Groceries"),
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    BEVERAGES("Beverages"),
    LAUNDRY("Laundry"),
    MILK_AND_EGG("Milk & Egg"),
    OTHER("Other"),
    ALL("All Products")
}

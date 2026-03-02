package com.lankasmartmart.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lankasmartmart.app.data.local.dao.CartDao
import com.lankasmartmart.app.data.local.dao.ProductDao
import com.lankasmartmart.app.data.local.dao.WishlistDao
import com.lankasmartmart.app.data.local.entity.CartItemEntity
import com.lankasmartmart.app.data.local.entity.ProductEntity
import com.lankasmartmart.app.data.local.entity.WishlistItemEntity
import com.lankasmartmart.app.data.local.entity.OrderEntity
import com.lankasmartmart.app.data.local.entity.UserEntity
import com.lankasmartmart.app.data.local.entity.AddressEntity
import com.lankasmartmart.app.data.local.entity.CategoryEntity
import com.lankasmartmart.app.data.local.entity.ReviewEntity
import com.lankasmartmart.app.data.local.entity.NotificationEntity
import com.lankasmartmart.app.data.local.entity.SearchHistoryEntity
import com.lankasmartmart.app.data.local.entity.PromoCodeEntity
import com.lankasmartmart.app.data.local.entity.StoreLocationEntity
import com.lankasmartmart.app.data.local.entity.SupportTicketEntity
import com.lankasmartmart.app.data.local.entity.SearchFilterEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        OrderEntity::class,
        UserEntity::class,
        AddressEntity::class,
        CategoryEntity::class,
        ReviewEntity::class,
        NotificationEntity::class,
        SearchHistoryEntity::class,
        PromoCodeEntity::class,
        StoreLocationEntity::class,
        SupportTicketEntity::class,
        SearchFilterEntity::class
    ],
    version = 6,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun orderDao(): com.lankasmartmart.app.data.local.dao.OrderDao
}

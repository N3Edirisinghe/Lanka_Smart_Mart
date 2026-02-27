package com.lankasmartmart.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lankasmartmart.app.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lankasmartmart_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase) = database.productDao()
    
    @Provides
    fun provideCartDao(database: AppDatabase) = database.cartDao()
    
    @Provides
    fun provideWishlistDao(database: AppDatabase) = database.wishlistDao()
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): com.lankasmartmart.app.data.repository.AuthRepository {
        return com.lankasmartmart.app.data.repository.AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        firestore: FirebaseFirestore,
        productDao: com.lankasmartmart.app.data.local.dao.ProductDao,
        auth: FirebaseAuth
    ): com.lankasmartmart.app.data.repository.ProductRepository {
        return com.lankasmartmart.app.data.repository.ProductRepositoryImpl(firestore, productDao, auth)
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        cartDao: com.lankasmartmart.app.data.local.dao.CartDao,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): com.lankasmartmart.app.data.repository.CartRepository {
        return com.lankasmartmart.app.data.repository.CartRepositoryImpl(cartDao, auth, firestore)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        storage: FirebaseStorage,
        @ApplicationContext context: Context
    ): com.lankasmartmart.app.data.repository.ImageRepository {
        return com.lankasmartmart.app.data.repository.ImageRepositoryImpl(storage, context)
    }

    @Provides
    @Singleton
    fun provideStripeRepository(
        @ApplicationContext context: Context
    ): com.lankasmartmart.app.data.repository.StripeRepository {
        return com.lankasmartmart.app.data.repository.StripeRepositoryImpl(context)
    }

    @Provides
    fun provideOrderDao(database: AppDatabase) = database.orderDao()

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        orderDao: com.lankasmartmart.app.data.local.dao.OrderDao,
        auth: FirebaseAuth
    ): com.lankasmartmart.app.data.repository.OrderRepository {
        return com.lankasmartmart.app.data.repository.OrderRepositoryImpl(firestore, orderDao, auth)
    }
}



package com.lankasmartmart.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LankaSmartMartApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        PaymentConfiguration.init(
            applicationContext,
            getString(R.string.stripe_publishable_key)
        )
        createNotificationChannels()
        setupWorkManager()
    }
    
    private fun setupWorkManager() {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()
            
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.lankasmartmart.app.worker.SyncWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
            
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "OfflineSyncWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_OFFERS,
                    "Offers & Promotions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "New offers, discounts, and flash sales"
                },
                NotificationChannel(
                    CHANNEL_ORDERS,
                    "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Order status and delivery updates"
                }
            )
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    companion object {
        const val CHANNEL_OFFERS = "offers_channel"
        const val CHANNEL_ORDERS = "orders_channel"
    }
}

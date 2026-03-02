package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.delay
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor() : PaymentRepository {
    override suspend fun createPaymentIntent(amount: Double, currency: String): Resource<String> {
        // simulate network delay
        delay(2000)
        
        // Return a mock client secret for frontend UI testing.
        // In a real app, this MUST come from your backend (Cloud Function).
        // This key will fail if used with Stripe SDK unless it's a valid Test Key from YOUR dashboard.
        
        // This is a placeholder format. Real one looks like "pi_3Qxxxxx_secret_yyyyy"
        return Resource.Success("pi_mock_secret_123456789")
    }
}

package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.util.Resource

interface PaymentRepository {
    suspend fun createPaymentIntent(amount: Double, currency: String): Resource<String>
}

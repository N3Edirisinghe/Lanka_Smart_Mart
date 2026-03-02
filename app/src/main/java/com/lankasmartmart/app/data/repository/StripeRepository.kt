package com.lankasmartmart.app.data.repository

import com.lankasmartmart.app.util.Resource
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams

interface StripeRepository {
    suspend fun createPaymentMethod(params: PaymentMethodCreateParams): Resource<PaymentMethod>
    suspend fun createPaymentIntent(amount: Double, currency: String): Resource<String>
}

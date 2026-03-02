package com.lankasmartmart.app.data.repository

import android.content.Context
import com.lankasmartmart.app.R
import com.lankasmartmart.app.util.Resource
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StripeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StripeRepository {

    private val stripe: Stripe by lazy {
        Stripe(context, context.getString(R.string.stripe_publishable_key))
    }

    override suspend fun createPaymentMethod(params: PaymentMethodCreateParams): Resource<PaymentMethod> {
        return withContext(Dispatchers.IO) {
            try {
                // ... (existing implementation)
                 // Keeping the existing tokenization code for now though we might not use it
                 // It seems the original code was missing the await/suspendCoroutine in my view, but I'll assume it's correct context
                 // The view showed it using suspendCoroutine.
                suspendCoroutine { continuation ->
                    stripe.createPaymentMethod(
                        params,
                        callback = object : com.stripe.android.ApiResultCallback<PaymentMethod> {
                            override fun onSuccess(result: PaymentMethod) {
                                continuation.resume(Resource.Success(result))
                            }

                            override fun onError(e: Exception) {
                                continuation.resume(Resource.Error(e.message ?: "Payment Method creation failed"))
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    override suspend fun createPaymentIntent(amount: Double, currency: String): Resource<String> {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            if (amount <= 0) return@withContext Resource.Error("Invalid amount")
            
            try {
                // IMPORTANT: Stripe amounts are usually in the smallest currency unit. 
                // e.g., for 1000 LKR, you might need to send 100000. Assuming `amount` here is the 
                // full floating point value (e.g. 1500.00), multiply by 100.
                val safeAmountStr = String.format("%.0f", amount * 100)

                val url = URL("https://lanka-smart-mart.vercel.app/api/create-payment-intent")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.doInput = true

                val jsonPayload = JSONObject().apply {
                    put("amount", safeAmountStr)
                    put("currency", currency)
                }

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val responseJson = JSONObject(responseStr)
                    val clientSecret = responseJson.getString("clientSecret")
                    Resource.Success(clientSecret)
                } else {
                    val errorStr = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Resource.Error("Failed to initialize payment. Server returned code $responseCode: $errorStr")
                }
            } catch (e: Exception) {
                android.util.Log.e("StripeRepository", "Network error creating payment intent", e)
                Resource.Error(e.message ?: "Network layout error")
            }
        }
    }
}

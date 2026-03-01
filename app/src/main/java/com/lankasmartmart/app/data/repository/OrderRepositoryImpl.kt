package com.lankasmartmart.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import java.util.UUID
import com.lankasmartmart.app.data.local.entity.toEntity
import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray
import com.lankasmartmart.app.data.local.entity.toEntity

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val orderDao: com.lankasmartmart.app.data.local.dao.OrderDao,
    private val auth: FirebaseAuth
) : OrderRepository {

    override suspend fun placeOrder(order: Order): Resource<String> {
        return kotlinx.coroutines.withContext(Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
            try {
                // 1. Generate ID locally if not present
                val newOrderId = if (order.orderId.isNotEmpty()) order.orderId else UUID.randomUUID().toString()
                val finalOrder = order.copy(orderId = newOrderId)
                
                // 2. Save locally first (marked as unsynced)
                val entity = finalOrder.toEntity(isSynced = false)
                orderDao.insertOrder(entity)
                
                // 3. Try to sync with Firestore
                try {
                    val docRef = firestore.collection("orders").document(newOrderId)
                    docRef.set(finalOrder).await()
                    
                    // Mark as synced if successful
                    orderDao.markOrderAsSynced(newOrderId)

                    // Award Loyalty Points
                    try {
                        val points = (finalOrder.totalPrice / 100).toLong()
                        if (points > 0 && finalOrder.userId.isNotEmpty()) {
                            firestore.collection("users").document(finalOrder.userId)
                                .update("loyaltyPoints", com.google.firebase.firestore.FieldValue.increment(points))
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("OrderRepository", "Failed to update loyalty points", e)
                    }

                    // Send Order Confirmation Email via Vercel Backend
                    try {
                        val userEmail = auth.currentUser?.email
                        if (!userEmail.isNullOrEmpty()) {
                            try {
                                val apiUrl = "https://lanka-smart-mart.vercel.app/api/send-order-email"
                                android.util.Log.d("OrderRepository", "Initiating API call to $apiUrl for Order ID $newOrderId")
                                val url = URL(apiUrl)
                                val connection = url.openConnection() as HttpURLConnection
                                
                                connection.requestMethod = "POST"
                                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                                connection.setRequestProperty("Accept", "application/json")
                                connection.doOutput = true

                                val itemsArray = JSONArray()
                                finalOrder.items.forEach { item ->
                                    val itemJson = JSONObject().apply {
                                        put("productName", item.productName)
                                        put("quantity", item.quantity)
                                        put("productPrice", item.productPrice)
                                    }
                                    itemsArray.put(itemJson)
                                }

                                val jsonInputString = JSONObject().apply {
                                    put("email", userEmail)
                                    put("orderId", newOrderId)
                                    put("totalPrice", finalOrder.totalPrice)
                                    put("paymentMethod", finalOrder.paymentMethod)
                                    put("items", itemsArray)
                                }.toString()

                                connection.outputStream.use { os ->
                                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                                    os.write(input, 0, input.size)
                                }

                                val responseCode = connection.responseCode
                                val responseStr = if (responseCode == HttpURLConnection.HTTP_OK) {
                                    connection.inputStream.bufferedReader().use { it.readText() }
                                } else {
                                    connection.errorStream?.bufferedReader()?.use { it.readText() }
                                }
                                android.util.Log.d("OrderRepository", "Email API Response ($responseCode): $responseStr")
                                
                                if (responseCode != HttpURLConnection.HTTP_OK) {
                                    android.util.Log.e("OrderRepository", "Failed to send order email: response code $responseCode. Body: $responseStr")
                                }
                            } catch (emailEx: Exception) {
                                android.util.Log.e("OrderRepository", "Error sending order email request", emailEx)
                            }
                        }
                    } catch (e: Exception) {
                         android.util.Log.e("OrderRepository", "Failed to initiate email sending logic", e)
                    }

                } catch (e: Exception) {
                    android.util.Log.e("OrderRepository", "Failed to sync order to cloud, saved locally", e)
                    // If Firestore fails (e.g., Quota Exceeded), we MUST return an error so the UI knows.
                    // We also revert the local save so the cart is not empty without a real order.
                    orderDao.deleteOrderById(newOrderId)
                    return@withContext Resource.Error(e.message ?: "Server Error: Quota Exceeded or Offline")
                }
                
                Resource.Success(newOrderId)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Failed to place order")
            }
        }
    }

    override fun getOrders(userId: String): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        
        try {
            // 1. Trigger background sync (Generic sync for this user)
            // We launch this in a separate coroutine scope to not block the flow emission from local DB
            
            suspend fun fetchFromCloud() {
                try {
                    val snapshot = firestore.collection("orders")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                    
                    val entities = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                    }.map { it.toEntity(isSynced = true) }
                    
                    if (entities.isNotEmpty()) {
                        orderDao.insertOrders(entities)
                    }
                    
                    // Also try to push unsynced orders
                    syncPendingOrders()
                    
                } catch (e: Exception) {
                    android.util.Log.e("OrderRepository", "Cloud sync failed", e)
                }
            }
            
            // Emit local data immediately and observe changes
            kotlinx.coroutines.coroutineScope {
                launch { fetchFromCloud() }
                
                orderDao.getOrders(userId).collect { entities ->
                    val orders = entities.map { it.toOrder() }
                    emit(Resource.Success(orders))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load orders"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncPendingOrders() {
        val pendingOrders = orderDao.getUnsyncedOrders()
        for (entity in pendingOrders) {
            try {
                val order = entity.toOrder()
                firestore.collection("orders").document(order.orderId).set(order).await()
                orderDao.markOrderAsSynced(order.orderId)
            } catch (e: Exception) {
                android.util.Log.e("OrderRepository", "Failed to sync pending order ${entity.orderId}", e)
            }
        }
    }

    override suspend fun getOrderById(orderId: String): Resource<Order> {
        return try {
            // Local First
            val localEntity = orderDao.getOrderById(orderId)
            if (localEntity != null) {
                return Resource.Success(localEntity.toOrder())
            }
            
            // Remote fallback
            val document = firestore.collection("orders").document(orderId).get().await()
            val order = document.toObject(Order::class.java)?.copy(orderId = document.id)
            if (order != null) {
                // Save to local for future
                orderDao.insertOrder(order.toEntity(isSynced = true))
                Resource.Success(order)
            } else {
                Resource.Error("Order not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch order")
        }
    }

    override fun getAllOrders(): Flow<Resource<List<Order>>> = flow {
         emit(Resource.Loading())
         // Admin method: Similar strategy, observe local, fetch remote
         try {
             kotlinx.coroutines.coroutineScope {
                 launch {
                     try {
                         val snapshot = firestore.collection("orders").get().await()
                         val entities = snapshot.documents.mapNotNull { doc ->
                             doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                         }.map { it.toEntity(isSynced = true) }
                         orderDao.insertOrders(entities)
                     } catch (e: Exception) {
                         android.util.Log.e("OrderRepository", "Admin sync failed", e)
                     }
                 }
                 
                 orderDao.getAllOrders().collect { entities ->
                     emit(Resource.Success(entities.map { it.toOrder() }))
                 }
             }
         } catch (e: Exception) {
             emit(Resource.Error(e.message ?: "Failed to load all orders"))
         }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateOrderStatus(orderId: String, status: String): Resource<Boolean> {
        return try {
            // Update Local
            val localEntity = orderDao.getOrderById(orderId)
            if (localEntity != null) {
                 orderDao.insertOrder(localEntity.copy(status = status, isSynced = false)) // Mark dirty to sync later? 
                 // Updating status is an Admin implementation usually, checking if we handle push for status updates.
                 // For now, let's just do optimistic update + cloud push
            }

            firestore.collection("orders").document(orderId)
                .update("status", status)
                .await()
            
            // If cloud success, mark synced (or just update local again with correct status)
             if (localEntity != null) {
                 orderDao.insertOrder(localEntity.copy(status = status, isSynced =true))
             }
             
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }
}

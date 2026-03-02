package com.lankasmartmart.app.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    onBackClick: () -> Unit,
    onTrackOrderClick: (String) -> Unit = {},
    viewModel: OrderDetailsViewModel = hiltViewModel()
) {
    val orderState by viewModel.orderState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F6FA),
        bottomBar = {
            if (orderState is Resource.Success) {
                val order = (orderState as Resource.Success).data
                if (order != null && (order.status == "Processing" || order.status == "Out for Delivery" || order.status == "Pending" || order.status == "Shipped" || order.status == "On Way")) {
                     Surface(
                        color = Color.White,
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Button(
                                onClick = { onTrackOrderClick(order.orderId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WelcomeScreenGreen
                                )
                            ) {
                                Text(
                                    "Track Order",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = orderState) {
                is Resource.Loading -> {
                     Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = WelcomeScreenGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading details...", color = Color.Gray)
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message ?: "Error loading order",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is Resource.Success -> {
                    val order = state.data
                    if (order != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 16.dp, end = 16.dp), // Added bottom padding for button
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Card
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        Text(
                                            "Order #${order.orderId.takeLast(6).uppercase()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Placed on ${SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp))}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Status", fontWeight = FontWeight.SemiBold, color = Color.Black)
                                            Surface(
                                                color = when(order.status) {
                                                    "Pending" -> Color(0xFFFFF3E0)
                                                    "Processing" -> Color(0xFFE3F2FD)
                                                    "Delivered" -> Color(0xFFE8F5E9)
                                                    "Cancelled" -> Color(0xFFFFEBEE)
                                                    else -> Color.LightGray.copy(alpha = 0.2f)
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when(order.status) {
                                                        "Pending" -> Color(0xFFEF6C00)
                                                        "Processing" -> Color(0xFF1976D2)
                                                        "Delivered" -> Color(0xFF2E7D32)
                                                        "Cancelled" -> Color(0xFFC62828)
                                                        else -> Color.Black
                                                    }
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                             Text("Payment", fontWeight = FontWeight.SemiBold, color = Color.Black)
                                             Text(order.paymentMethod, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            // 2. Shipping Address
                            item {
                                Text(
                                    "Delivery Address",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp),
                                    color = Color.Black
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        Text(
                                            text = order.shippingAddress.name.ifBlank { "User" },
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = buildString {
                                                append(order.shippingAddress.street)
                                                if (order.shippingAddress.city.isNotBlank()) append(", ${order.shippingAddress.city}")
                                            },
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = order.shippingAddress.phone,
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // 3. Items List
                            item {
                                Text(
                                    "Items",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }
                            
                            items(order.items) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEFF3))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Image Placeholder
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF0F0F0),
                                            modifier = Modifier.size(60.dp)
                                        ) {
                                             coil.compose.AsyncImage(
                                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                    .data(item.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                item.productName,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 2,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Qty: ${item.quantity}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        
                                        Text(
                                            "LKR ${String.format("%,.0f", item.productPrice * item.quantity)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            // 4. Payment Summary
                            item {
                                Text(
                                    "Payment Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                        val realSubtotal = order.totalPrice + order.discount
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Subtotal", color = Color.Gray)
                                            Text("LKR ${String.format("%,.2f", realSubtotal)}", fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        if (order.discount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Discount (Points)", color = WelcomeScreenGreen)
                                                Text("- LKR ${String.format("%,.2f", order.discount)}", fontWeight = FontWeight.SemiBold, color = WelcomeScreenGreen)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Delivery Fee", color = Color.Gray)
                                            Text("LKR 0.00", fontWeight = FontWeight.SemiBold, color = Color.Black) // Assuming free for now
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color(0xFFF0F0F0))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Total Amount", fontWeight = FontWeight.Bold, color = Color.Black)
                                            Text(
                                                "LKR ${String.format("%,.2f", order.totalPrice)}",
                                                fontWeight = FontWeight.Bold,
                                                color = WelcomeScreenGreen,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                }
                            }
                            
                             // 5. Payment Method
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(0.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Payment Method", color = Color.Gray)
                                        Text(order.paymentMethod, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        Text("Order not found", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

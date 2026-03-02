package com.lankasmartmart.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.data.model.Order
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource
import java.text.SimpleDateFormat
import java.util.*

// ... imports ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val ordersState by viewModel.ordersState.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled")
    
    // Auto-refresh when entering screen
    LaunchedEffect(Unit) {
        viewModel.fetchAllOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Store Manager", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Overview & Orders", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchAllOrders() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Black)
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize() // Ensure column takes full space
        ) {
            
            when (val state = ordersState) {
                is Resource.Loading -> {
                     Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator(color = WelcomeScreenGreen)
                     }
                }
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Failed to load data", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text(state.message ?: "Unknown error", color = Color.Gray, fontSize = 12.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.fetchAllOrders() }, colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen)) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    val allOrders = state.data ?: emptyList()
                    val filteredOrders = if (selectedTab == "All") allOrders else allOrders.filter { it.status == selectedTab }
                    
                    // --- Dashboard Stats ---
                    DashboardStats(orders = allOrders)
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- Tabs ---
                    ScrollableTabRow(
                        selectedTabIndex = tabs.indexOf(selectedTab),
                        containerColor = Color.Transparent,
                        contentColor = WelcomeScreenGreen,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab).coerceIn(0, tabs.lastIndex)]),
                                    color = WelcomeScreenGreen,
                                    height = 3.dp
                                )
                            }
                        },
                        divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f)) }
                    ) {
                        tabs.forEach { title ->
                            Tab(
                                selected = selectedTab == title,
                                onClick = { selectedTab = title },
                                text = { 
                                    Text(
                                        title, 
                                        fontWeight = if(selectedTab == title) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    ) 
                                },
                                selectedContentColor = WelcomeScreenGreen,
                                unselectedContentColor = Color.Gray
                            )
                        }
                    }
                    
                    if (filteredOrders.isEmpty()) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f), // Take remaining space
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No ${selectedTab.lowercase()} orders found", color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f), // Crucial fix for "display venne na"
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredOrders) { order ->
                                AdminOrderCard(
                                    order = order,
                                    onUpdateStatus = { newStatus ->
                                        viewModel.updateOrderStatus(order.orderId, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStats(orders: List<Order>) {
    val totalRevenue = orders.filter { it.status != "Cancelled" }.sumOf { it.totalPrice }
    val totalOrders = orders.size
    val pendingOrders = orders.count { it.status == "Pending" || it.status == "Processing" || it.status == "Paid" }
    val deliveredOrders = orders.count { it.status == "Delivered" }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Revenue Card (Big)
            StatCard(
                title = "Total Revenue",
                value = "Rs.${String.format("%.0f", totalRevenue)}",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF4CAF50), // Green
                backgroundColor = Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f)
            )
            
            // Orders Card
             StatCard(
                title = "Total Orders",
                value = totalOrders.toString(),
                icon = Icons.Default.ShoppingBag,
                color = Color(0xFF2196F3), // Blue
                backgroundColor = Color(0xFFE3F2FD),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pending Card
            StatCard(
                title = "Active / Pending",
                value = pendingOrders.toString(),
                icon = Icons.Default.PendingActions,
                color = Color(0xFFFF9800), // Orange
                backgroundColor = Color(0xFFFFF3E0),
                modifier = Modifier.weight(1f)
            )
            
            // Delivered Card
            StatCard(
                title = "Delivered",
                value = deliveredOrders.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF009688), // Teal
                backgroundColor = Color(0xFFE0F2F1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order, onUpdateStatus: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(Modifier.padding(16.dp)) {
            // Top Row: Order ID and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF5F6FA), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Order #${order.orderId.takeLast(5).uppercase()}", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(order.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rs. ${String.format("%.0f", order.totalPrice)}", 
                        fontWeight = FontWeight.Bold, 
                        color = WelcomeScreenGreen,
                        style = MaterialTheme.typography.titleMedium
                    )
                    StatusChip(order.status)
                }
            }
            
            // Expanded Details
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(modifier = Modifier.height(16.dp))
                
                // Customer Info
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Customer", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(order.shippingAddress.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(order.shippingAddress.phone, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Address Info
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Delivery Address", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("${order.shippingAddress.street}, ${order.shippingAddress.city}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                 
                Spacer(modifier = Modifier.height(12.dp))
                 
                // Payment Info
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Payment Method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            text = order.paymentMethod, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = if(order.isPaid) WelcomeScreenGreen else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // Actions
                Text("Update Order Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(), 
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (order.status) {
                        "Pending" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionButton("Accept Order", WelcomeScreenGreen) { onUpdateStatus("Processing") }
                                ActionButton("Reject", Color.Red, isOutlined = true) { onUpdateStatus("Cancelled") }
                            }
                        }
                        "Processing", "Paid" -> {
                            Row {
                                ActionButton("Mark as Shipped", Color(0xFFFF9800)) { onUpdateStatus("Shipped") }
                            }
                        }
                        "Shipped" -> {
                            Row {
                                ActionButton("Mark as Delivered", WelcomeScreenGreen) { onUpdateStatus("Delivered") }
                            }
                        }
                        else -> {
                             // No actions for Delivered/Cancelled
                             Text("No further actions available", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            } else {
                 Spacer(modifier = Modifier.height(12.dp))
                 Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.LightGray)
                 }
            }
        }
    }
}


@Composable
fun LabelValueRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.width(100.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
fun RowScope.ActionButton(text: String, color: Color, isOutlined: Boolean = false, onClick: () -> Unit) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
            border = androidx.compose.foundation.BorderStroke(1.dp, color),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, text) = when (status) {
        "Pending" -> Color(0xFFFFC107) to "Pending"
        "Processing" -> Color(0xFF2196F3) to "Processing"
        "Shipped" -> Color(0xFFFF9800) to "On Way"
        "Delivered" -> WelcomeScreenGreen to "Completed"
        "Paid" -> Color(0xFF4CAF50) to "Paid"
        "Cancelled" -> Color.Red to "Cancelled"
        else -> Color.Gray to status
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}


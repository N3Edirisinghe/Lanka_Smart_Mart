package com.lankasmartmart.app.presentation.checkout

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val orderState by viewModel.orderState.collectAsState()
    val total by viewModel.cartTotal.collectAsState()
    val addresses by viewModel.addressesState.collectAsState()
    val context = LocalContext.current
    
    // Delivery Option State (Mock)
    var deliveryOption by remember { mutableStateOf("Standard") }
    var showAddressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderState) {
        when (val state = orderState) {
            is Resource.Success -> {
                viewModel.resetOrderState()
                onOrderSuccess(state.data ?: "")
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetOrderState()
            }
            else -> {}
        }
    }

    // Stripe Payment Sheet
    val paymentSheet = com.stripe.android.paymentsheet.rememberPaymentSheet { paymentSheetResult ->
        when (paymentSheetResult) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess("Stripe_Card")
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                // User canceled
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                viewModel.onPaymentFailure(paymentSheetResult.error.message ?: "Payment Failed")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Checkout", 
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold
                    ) 
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
                )
            )
        },
        bottomBar = {
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
                        onClick = { 
                            if (viewModel.paymentMethod == "Card") {
                                viewModel.fetchPaymentIntent { clientSecret ->
                                    // PaymentSheet UI MUST be presented on the Main thread
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        paymentSheet.presentWithPaymentIntent(
                                            clientSecret,
                                            com.stripe.android.paymentsheet.PaymentSheet.Configuration("LankaSmartMart")
                                        )
                                    }
                                }
                            } else {
                                viewModel.placeOrder() 
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WelcomeScreenGreen
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = orderState !is Resource.Loading
                    ) {
                        if (orderState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            val buttonText = if (viewModel.paymentMethod == "Card") {
                                "Pay & Place Order - Rs.${String.format("%.2f", total)}"
                            } else {
                                "Place Order - Rs.${String.format("%.2f", total)}"
                            }
                            Text(
                                buttonText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F6FA) 
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- Contact Details ---
            SectionHeader("Contact Info")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfessionalInput(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it },
                        label = "Full Name",
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfessionalInput(
                        value = viewModel.phone,
                        onValueChange = { viewModel.phone = it },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            // --- Delivery Address ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Shipping Address")
                if (addresses.isNotEmpty()) {
                    TextButton(onClick = { showAddressDialog = true }) {
                        Text("Select Saved", color = WelcomeScreenGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (showAddressDialog) {
                AlertDialog(
                    onDismissRequest = { showAddressDialog = false },
                    title = { Text("Select Delivery Address") },
                    text = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        ) {
                            addresses.forEach { address ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectAddress(address)
                                            showAddressDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF5F6FA),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WelcomeScreenGreen)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(address.label.ifBlank { "Address" }, fontWeight = FontWeight.Bold)
                                        Text("${address.street}, ${address.city}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                Divider(color = Color(0xFFEEEEEE))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAddressDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    textContentColor = Color.Black
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfessionalInput(
                        value = viewModel.street,
                        onValueChange = { viewModel.street = it },
                        label = "Street Address",
                        icon = Icons.Default.LocationOn
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfessionalInput(
                        value = viewModel.city,
                        onValueChange = { viewModel.city = it },
                        label = "City",
                        icon = Icons.Default.LocationOn
                    )
                }
            }

            // --- Delivery Option ---
            SectionHeader("Delivery Method")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeliveryOptionCard(
                    title = "Priority Delivery",
                    subtitle = "10-20 mins",
                    price = "Rs. 250.00",
                    icon = Icons.Outlined.LocalShipping,
                    isSelected = deliveryOption == "Priority",
                    onClick = { deliveryOption = "Priority" }
                )
                DeliveryOptionCard(
                    title = "Standard Delivery",
                    subtitle = "30-45 mins",
                    price = "Free",
                    icon = Icons.Outlined.LocalShipping,
                    isSelected = deliveryOption == "Standard",
                    onClick = { deliveryOption = "Standard" }
                )
            }

            // --- Payment Method ---
            SectionHeader("Payment Method")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Online Payment (Card / GPay)
                PaymentMethodOption(
                    title = "Online Payment",
                    subtitle = "Visa, Master, Google Pay",
                    icon = Icons.Default.CreditCard,
                    isSelected = viewModel.paymentMethod == "Card",
                    onClick = { viewModel.paymentMethod = "Card" }
                )
                
                // Bybit / Crypto
                PaymentMethodOption(
                    title = "Bybit / Crypto",
                    subtitle = "Pay via USDT/Crypto Transfer",
                    icon = Icons.Default.AccountBalanceWallet,
                    isSelected = viewModel.paymentMethod == "Bybit",
                    onClick = { viewModel.paymentMethod = "Bybit" }
                )
                
                // Cash on Delivery
                PaymentMethodOption(
                    title = "Cash on Delivery",
                    subtitle = "Pay when you receive",
                    icon = Icons.Default.Money,
                    isSelected = viewModel.paymentMethod == "COD",
                    onClick = { viewModel.paymentMethod = "COD" }
                )
            }
            
            // --- Payment Details Section ---
            if (viewModel.paymentMethod == "Card") {
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                     border = BorderStroke(1.dp, WelcomeScreenGreen.copy(alpha = 0.3f)),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = WelcomeScreenGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Secured by Stripe", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Supports Visa, MasterCard, Google Pay.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Visual Indicators (Icons)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentIconPlaceholder("VISA", Color(0xFF1A1F71))
                            PaymentIconPlaceholder("Master", Color(0xFFEB001B))
                            PaymentIconPlaceholder("GPay", Color.Black)
                        }
                    }
                }
            } else if (viewModel.paymentMethod == "Bybit") {
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color.White),
                     border = BorderStroke(1.dp, Color(0xFFFF9800)),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bybit / Crypto Transfer", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Send USDT (TRC20) to:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        // Valid Wallet Address Info
                        SelectionContainer {
                             Text(
                                "TTrK43...ExampleWalletAddress", 
                                fontWeight = FontWeight.Bold, 
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(8.dp)
                             )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Enter Transaction ID (TXID):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = viewModel.bybitTransactionId,
                            onValueChange = { viewModel.bybitTransactionId = it },
                            placeholder = { Text("Paste TXID here") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            // --- Order Summary ---
            SectionHeader("Order Summary")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SummaryRow(label = "Subtotal", value = "Rs. ${String.format("%.2f", total)}")
                    SummaryRow(label = "Delivery Fee", value = if(deliveryOption == "Standard") "Free" else "Rs. 250.00") 
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Rs. ${String.format("%.2f", total + (if(deliveryOption == "Priority") 250.0 else 0.0))}", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = WelcomeScreenGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        color = Color.DarkGray
    )
}

@Composable
fun ProfessionalInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = WelcomeScreenGreen) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WelcomeScreenGreen,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = WelcomeScreenGreen,
            cursorColor = WelcomeScreenGreen,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun DeliveryOptionCard(
    title: String,
    subtitle: String,
    price: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
        ),
        border = if (isSelected) BorderStroke(1.5.dp, WelcomeScreenGreen) else BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if(isSelected) WelcomeScreenGreen else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = if(isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            Text(price, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if(isSelected) WelcomeScreenGreen else Color.Black)
        }
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(100.dp), // Fixed height for uniformity
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
        ),
        border = if (isSelected) BorderStroke(1.5.dp, WelcomeScreenGreen) else BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if(isSelected) WelcomeScreenGreen else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title, 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if(isSelected) WelcomeScreenGreen else Color.Gray
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = WelcomeScreenGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}

@Composable
fun PaymentMethodOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White
        ),
        border = if (isSelected) BorderStroke(1.5.dp, WelcomeScreenGreen) else BorderStroke(1.dp, Color(0xFFEEEEEE)),
        elevation = CardDefaults.cardElevation(defaultElevation = if(isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if(isSelected) WelcomeScreenGreen else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = if(isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WelcomeScreenGreen)
            }
        }
    }
}

@Composable
fun PaymentIconPlaceholder(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text, 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Bold, 
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

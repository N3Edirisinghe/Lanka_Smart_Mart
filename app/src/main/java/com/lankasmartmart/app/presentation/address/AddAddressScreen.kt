package com.lankasmartmart.app.presentation.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    onBackClick: () -> Unit,
    onAddressAdded: () -> Unit,
    viewModel: AddressViewModel = hiltViewModel()
) {
    var label by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Validation states
    var isSubmitted by remember { mutableStateOf(false) }

    val addAddressState by viewModel.addAddressState.collectAsState()

    LaunchedEffect(addAddressState) {
        if (addAddressState is Resource.Success) {
            viewModel.resetAddAddressState()
            onAddressAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Address", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFFF5F6FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Label (e.g. Home, Work)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = WelcomeScreenGreen,
                    cursorColor = WelcomeScreenGreen,
                    focusedBorderColor = WelcomeScreenGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipient Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isSubmitted && name.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = WelcomeScreenGreen,
                    cursorColor = WelcomeScreenGreen,
                    focusedBorderColor = WelcomeScreenGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (isSubmitted && name.isBlank()) {
                Text(
                    text = "Name is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Street
            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Street Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                isError = isSubmitted && street.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = WelcomeScreenGreen,
                    cursorColor = WelcomeScreenGreen,
                    focusedBorderColor = WelcomeScreenGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
             if (isSubmitted && street.isBlank()) {
                Text(
                    text = "Street address is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // City
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isSubmitted && city.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = WelcomeScreenGreen,
                    cursorColor = WelcomeScreenGreen,
                    focusedBorderColor = WelcomeScreenGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
             if (isSubmitted && city.isBlank()) {
                Text(
                    text = "City is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = isSubmitted && phone.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = WelcomeScreenGreen,
                    cursorColor = WelcomeScreenGreen,
                    focusedBorderColor = WelcomeScreenGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
             if (isSubmitted && phone.isBlank()) {
                Text(
                    text = "Phone number is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    isSubmitted = true
                    if (name.isNotBlank() && street.isNotBlank() && city.isNotBlank() && phone.isNotBlank()) {
                        viewModel.addAddress(label, name, street, city, phone)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = addAddressState !is Resource.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen)
            ) {
                 if (addAddressState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Address", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            if (addAddressState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                // Error card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                            text = (addAddressState as Resource.Error).message ?: "Error saving address",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

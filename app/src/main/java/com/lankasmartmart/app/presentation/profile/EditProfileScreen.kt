package com.lankasmartmart.app.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onProfileUpdated: (() -> Unit)? = null,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    val userState by viewModel.userState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            viewModel.selectedImageUri = uri
        }
    }

    LaunchedEffect(userState) {
        if (!isInitialized && userState is Resource.Success) {
            val user = (userState as Resource.Success).data
            if (user != null) {
                name = user.name
                phone = user.phoneNumber
                email = user.email
                // If we had a stored profile picture URL, we could load it here too
                // But for now we just handle new uploads
                isInitialized = true
            }
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            onProfileUpdated?.invoke()
            viewModel.resetUpdateState()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userState is Resource.Loading) {
                CircularProgressIndicator(color = WelcomeScreenGreen)
            } else if (userState is Resource.Error) {
                // Show error state with retry
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (userState as Resource.Error).message ?: "Error loading profile",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retryLoading() },
                        colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen)
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))

                // Profile Image Section
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { launcher.launch("image/*") }
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, WelcomeScreenGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val user = (userState as? Resource.Success)?.data
                            if (!user?.profileImageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(user!!.profileImageUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    // Edit Icon Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WelcomeScreenGreen)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form Fields
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WelcomeScreenGreen,
                                focusedLabelColor = WelcomeScreenGreen,
                                cursorColor = WelcomeScreenGreen,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        if (name.isBlank()) {
                            Text(
                                "Name cannot be empty",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Email (Read Only)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WelcomeScreenGreen,
                                focusedLabelColor = WelcomeScreenGreen,
                                cursorColor = WelcomeScreenGreen,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.updateProfile(name, phone)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = updateState !is Resource.Loading && name.isNotBlank(), // Disable if loading OR name empty
                    colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen)
                ) {
                     if (updateState is Resource.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                if (updateState is Resource.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (updateState as Resource.Error).message ?: "Error updating profile",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

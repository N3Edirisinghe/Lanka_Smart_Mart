package com.lankasmartmart.app.presentation.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    profileBackStackEntry: NavBackStackEntry?,
    onLogout: () -> Unit,
    onOrderClick: () -> Unit,
    onAddressClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeViewModel: com.lankasmartmart.app.presentation.theme.ThemeViewModel = hiltViewModel(context as androidx.activity.ComponentActivity)
    
    val userState by viewModel.userState.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()
    var showThemeDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val scrollState = rememberScrollState()

    // Reload profile when screen is composed (including when navigating back from Edit Profile)
    LaunchedEffect(profileBackStackEntry) {
        viewModel.loadUserProfile()
        profileBackStackEntry?.savedStateHandle?.remove<Boolean>("profile_updated")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Material dynamic background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Curved Header Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WelcomeScreenGreen,
                                Color(0xFF2E5B23) // Slightly darker green
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                // Profile Section
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Profile Image & Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageUrl = (userState as? Resource.Success)?.data?.profileImageUrl.orEmpty()
                            if (imageUrl.isNotBlank()) {
                                key(imageUrl) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageUrl),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(50.dp),
                                    tint = WelcomeScreenGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (val state = userState) {
                            is Resource.Loading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = WelcomeScreenGreen
                                )
                            }
                            is Resource.Success -> {
                                val user = state.data
                                Text(
                                    text = user?.name ?: "User Name",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = user?.email ?: "email@example.com",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Loyalty Points Badge
                                Surface(
                                    color = Color(0xFFFFF8E1), // Light Amber
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC107))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Points",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${user?.loyaltyPoints ?: 0} Points",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF6F00) // Darker Amber/Orange
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Text(
                                    text = "Failed to load profile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = { viewModel.loadUserProfile() }) {
                                    Text("Retry", color = WelcomeScreenGreen)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Menu Section
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Change your name and phone number",
                        onClick = onEditProfileClick
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.List,
                        title = "My Orders",
                        subtitle = "View order history and status",
                        onClick = onOrderClick
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.LocationOn,
                        title = "Delivery Addresses",
                        subtitle = "Manage your delivery locations",
                        onClick = onAddressClick
                    )
                    
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "App Settings",
                        subtitle = "Notifications, Language, Theme",
                        onClick = { showThemeDialog = true }
                    )
                    
                    // Admin Entry Point (Temporary)
                    // Admin Entry Point (Staff Only)
                    val currentUserEmail = (userState as? Resource.Success)?.data?.email
                    if (currentUserEmail.equals("10nilupulthisaranga@gmail.com", ignoreCase = true)) {
                        ProfileMenuItem(
                            icon = Icons.Default.Settings, 
                            title = "Store Manager",
                            subtitle = "Admin Dashboard",
                            onClick = onAdminClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                val context = androidx.compose.ui.platform.LocalContext.current
                OutlinedButton(
                    onClick = {
                        // 1. Sign out of Google
                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut()

                        // 2. Clear Local Prefs
                        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        
                        // 3. App Logout
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEBEE)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Admin Section (Hidden/Optional)
                if ((userState as? Resource.Success)?.data?.email.equals("10nilupulthisaranga@gmail.com", ignoreCase = true)) {
                    val productViewModel: com.lankasmartmart.app.presentation.home.ProductViewModel = hiltViewModel()
                    
                    TextButton(
                        onClick = { productViewModel.seedCloudImages(context) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Admin Data", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Theme Dialog
            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = { Text("Choose Theme") },
                    text = {
                        Column {
                            com.lankasmartmart.app.presentation.theme.ThemeMode.values().forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            themeViewModel.setTheme(mode)
                                            showThemeDialog = false 
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = themeMode == mode,
                                        onClick = { 
                                            themeViewModel.setTheme(mode)
                                            showThemeDialog = false 
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = WelcomeScreenGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when(mode) {
                                            com.lankasmartmart.app.presentation.theme.ThemeMode.LIGHT -> "Light Mode"
                                            com.lankasmartmart.app.presentation.theme.ThemeMode.DARK -> "Dark Mode"
                                            com.lankasmartmart.app.presentation.theme.ThemeMode.SYSTEM -> "System Default"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Cancel", color = WelcomeScreenGreen)
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F6FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WelcomeScreenGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}


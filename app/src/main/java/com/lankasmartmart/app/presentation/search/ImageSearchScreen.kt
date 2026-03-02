package com.lankasmartmart.app.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lankasmartmart.app.R
import com.lankasmartmart.app.data.model.Product
import com.lankasmartmart.app.data.model.ProductCategory
import com.lankasmartmart.app.presentation.components.ProductCard
import com.lankasmartmart.app.presentation.home.ProductViewModel
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.ui.theme.White
import com.lankasmartmart.app.util.Resource
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchScreen(
    navController: NavController,
    viewModel: ProductViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Int?>(null) }
    
    // Mock similar products
    val allProducts = (viewModel.productsState.collectAsState().value as? Resource.Success)?.data ?: emptyList()
    
    // Filter for "Vegetables" or "Fruits" as a simulation of finding similar items
    val similarProducts = remember(allProducts) {
        allProducts.filter { 
            it.category == ProductCategory.VEGETABLES || it.category == ProductCategory.FRUITS 
        }.take(4) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Product", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F6FA)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            if (!scanCompleted) {
                // Camera View Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (capturedImage != null) {
                         // Show captured image preview logic could go here
                         // For now just keep black or showing camera preview simulation
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Point your camera at a product",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Scanning Animation Overlay
                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(WelcomeScreenGreen)
                                .align(Alignment.Center) // Animate this in real app
                        )
                        Text(
                            text = "Scanning...",
                            color = WelcomeScreenGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                        )
                    }
                }
                
                // Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Button
                    IconButton(
                        onClick = { /* Open Gallery */ },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, CircleShape)
                            .shadow(elevation = 2.dp, shape = CircleShape)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.Gray)
                    }
                    
                    // Capture Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(WelcomeScreenGreen)
                            .clickable {
                                isScanning = true
                                // Simulate network delay
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isScanning = false
                                    scanCompleted = true
                                }, 2000)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    // Flash Button (Placeholder)
                    Spacer(modifier = Modifier.size(50.dp))
                }
            } else {
                // Results View
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                         Text("Image Captured", color = Color.White)
                         
                         // Re-scan button overlay
                         Button(
                             onClick = { scanCompleted = false },
                             colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                             modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                         ) {
                             Text("Retake", color = Color.White)
                         }
                    }
                    
                    Text(
                        text = "Similar Products Found",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    if (similarProducts.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(similarProducts) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { onProductClick(product.productId) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("No similar products found.")
                         }
                    }
                }
            }
        }
    }
}

package com.lankasmartmart.app.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchResults.collectAsState()
    
    // State for Search Discovery toggle
    var hideDiscovery by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxWidth()) {
                
                // Actual layout including back button for navigation safety
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() }, 
                        modifier = Modifier.size(36.dp).padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(28.dp)), // Very subtle light border instead of shadow if preferred, or just a background. The image has a very faint outline/shadow. Let's stick to a clean background with subtle border.
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = query,
                                onValueChange = viewModel::onQueryChange,
                                modifier = Modifier.weight(1f),
                                singleLine = false, // Allow wrapping as per image "Search products,\nbrands..."
                                maxLines = 2,
                                textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                                decorationBox = { innerTextField ->
                                    if (query.isEmpty()) {
                                        Text("Search products,\nbrands...", color = Color.Gray, fontSize = 14.sp, lineHeight = 18.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Camera Icon
                            IconButton(onClick = { /* Handle camera/scan */ }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan",
                                    tint = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
                                )
                            }
                            
                            // Mic Icon
                            IconButton(onClick = { /* Handle voice */ }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (query.isEmpty()) {
                // TODO: Implement actual recent searches history retrieval logic here
                // Show empty state or dynamic recent searches history when no query
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Empty or dynamic recent searches will go here
                }
            } else {
                // Show Search Results
                when (val state = searchState) {
                    is com.lankasmartmart.app.util.Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
                        )
                    }
                    is com.lankasmartmart.app.util.Resource.Error -> {
                        Text(
                            text = state.message ?: "Search Error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is com.lankasmartmart.app.util.Resource.Success -> {
                        val products = state.data ?: emptyList()
                        if (products.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No products found for \"$query\"", color = Color.Gray)
                            }
                        } else {
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(products.size) { index ->
                                    com.lankasmartmart.app.presentation.components.ProductCard(
                                        product = products[index],
                                        onClick = {
                                            navController.navigate(com.lankasmartmart.app.Screen.ProductDetails.createRoute(products[index].productId))
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
}

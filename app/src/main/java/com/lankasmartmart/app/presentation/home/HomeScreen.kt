package com.lankasmartmart.app.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Warning
import com.lankasmartmart.app.data.model.ProductCategory
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.presentation.auth.AuthViewModel
import com.lankasmartmart.app.presentation.components.ProductCard
import com.lankasmartmart.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onProductClick: (String) -> Unit,
    onAddressClick: () -> Unit,
    onCartClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
    cartViewModel: com.lankasmartmart.app.presentation.cart.CartViewModel = hiltViewModel(),
    addressViewModel: com.lankasmartmart.app.presentation.address.AddressViewModel = hiltViewModel()
) {
    val primaryAddress by addressViewModel.primaryAddress.collectAsState()
    val productsState by viewModel.productsState.collectAsState()
    val context = LocalContext.current
    val selectedCategoryStr by viewModel.selectedCategory.collectAsState()
    val selectedCategory = try { ProductCategory.valueOf(selectedCategoryStr) } catch (e: Exception) { ProductCategory.ALL }
    
    // Read location from AddressViewModel or SharedPreferences fallback
    var userLocation by remember { mutableStateOf("Select Location") }
    
    DisposableEffect(context, primaryAddress) {
        val prefs = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        
        val updateLocation = {
            val label = prefs.getString("location_label", null)
            val city = prefs.getString("selected_city", null)
            
            userLocation = label ?: primaryAddress?.let { 
                "${it.label}: ${it.street}, ${it.city}" 
            } ?: city ?: "Select Location"
        }
        
        // Initial setup
        updateLocation()
        
        // Listener for changes
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "location_label" || key == "selected_city") {
                updateLocation()
            }
        }
        
        prefs.registerOnSharedPreferenceChangeListener(listener)
        
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    
    val listState = rememberLazyListState()
    // The header (location tracker) should collapse if we scroll down beyond the first chunk of the feed
    val isHeaderVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(paddingValues)) {
            
            // Animated Header Area
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp)
            ) {
                // Location Tracker smoothly collapses
                AnimatedVisibility(
                    visible = isHeaderVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    HomeTopBar(
                        address = userLocation,
                        onCartClick = onCartClick,
                        onAddressClick = onAddressClick,
                        onResetClick = { 
                            viewModel.performHardReset()
                            Toast.makeText(context, "Reseeding Data...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                
                // Search bar remains pinned beautifully
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.searchProducts(it) },
                    onSearch = { /* Search happens on change */ },
                    onScanClick = onCameraClick,
                    onSearchClick = onSearchClick
                )
            }

            val productsState by viewModel.productsState.collectAsState()
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                    
                    // Only show banner and categories if NOT searching
                    if (searchQuery.isBlank()) {
                        // 1. Banner
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HomeBanner()
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // 2. Categories
                        item {
                            SectionHeader(title = "Categories", onSeeAllClick = {})
                            Spacer(modifier = Modifier.height(8.dp))
                            CategorySection(
                                selectedCategory = selectedCategory,
                                onCategorySelected = { 
                                    if (it == ProductCategory.ALL) {
                                        viewModel.filterByCategory("All")
                                    } else {
                                        viewModel.filterByCategory(it.name) 
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else {
                         item {
                            Text(
                                text = "Search Results for \"$searchQuery\"",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                            )
                        }
                    }


                    // 3. Section Header (Dynamic)
                    item {
                        SectionHeader(
                            title = if (searchQuery.isNotBlank()) "Products found" else selectedCategory.displayName,
                            onSeeAllClick = { /* TODO */ }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 4. Product Content Area (Optimized)
                    when (val state = productsState) {
                        is Resource.Loading -> {
                             item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen)
                                }
                             }
                        }
                        is Resource.Error -> {
                             item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = state.message ?: "Something went wrong", 
                                            color = Color.Gray
                                        )
                                    }
                                }
                             }
                        }
                        is Resource.Success -> {
                            val products = state.data ?: emptyList()
                            
                            if (products.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(300.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedCategory != ProductCategory.ALL) {
                                             Text(text = "No items in ${selectedCategory.displayName}", color = Color.Gray)
                                        } else if (searchQuery.isNotBlank()) {
                                             Text(text = "No items found", color = Color.Gray)
                                        } else {
                                             CircularProgressIndicator(color = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen)
                                        }
                                    }
                                }
                            } else {
                                // 1. Horizontal Scrollable Section (Featured)
                                if (products.size > 2) {
                                    item {
                                        Text(
                                            text = "Featured Items",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    item {
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items(products.take(5), key = { "featured_${it.productId}" }) { product ->
                                                Box(modifier = Modifier.width(160.dp)) {
                                                    ProductCard(
                                                        product = product,
                                                        onClick = { onProductClick(product.productId) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onAddToCart = {
                                                            cartViewModel.addToCart(product, 1)
                                                            Toast.makeText(context, "Added to Cart: ${product.name}", Toast.LENGTH_SHORT).show()
                                                        },
                                                        isFavorite = product.isFavorite,
                                                        onFavoriteToggle = { viewModel.toggleFavorite(product.productId) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 2. Vertical Scrollable Section (Grid)
                                item {
                                    Text(
                                        text = "All Items",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 12.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                
                                val displayProducts = if (products.size > 2) products.drop(5) else products
                                val chunks = displayProducts.chunked(2)
                                items(chunks.size) { index ->
                                    val pair = chunks[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        for (product in pair) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                ProductCard(
                                                    product = product,
                                                    onClick = { onProductClick(product.productId) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    onAddToCart = {
                                                        cartViewModel.addToCart(product, 1)
                                                        Toast.makeText(context, "Added to Cart: ${product.name}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    isFavorite = product.isFavorite,
                                                    onFavoriteToggle = { viewModel.toggleFavorite(product.productId) }
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                } // End LazyColumn
            } // End Box/Column
        } // End Scaffold padding block
    } // End Scaffold

package com.lankasmartmart.app.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic // Added Import
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lankasmartmart.app.R
import com.lankasmartmart.app.data.model.ProductCategory
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.PagerState

@Composable
fun HomeTopBar(
    address: String = "SLTC, Ingiriya Road, Meepe, Padukka",
    onCartClick: () -> Unit,
    onAddressClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location Icon (Truck/Delivery)
        Icon(
            imageVector = Icons.Default.LocalShipping,
            contentDescription = "Delivery Location",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Address with Dropdown
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onAddressClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select Location",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        // Reset/Refresh Icon
        IconButton(onClick = onResetClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Data",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

data class BannerData(
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val backgroundColor: Color,
    val buttonColor: Color,
    val buttonTextColor: Color,
    val titleColor: Color,
    val subtitleColor: Color,
    val imageRes: Int
)

@Composable
fun HomeBanner() {
    val banners = listOf(
        // 1. Pink Banner (Veggies Basket)
        BannerData(
            title = "Up to 30% offer",
            subtitle = "Enjoy our big offer",
            buttonText = "Shop Now",
            backgroundColor = Color(0xFFFCE4EC), // Pink
            buttonColor = Color(0xFFD32F2F), // Red
            buttonTextColor = Color.White,
            titleColor = Color.Black,
            subtitleColor = Color(0xFFD32F2F), // Red/Brownish
            imageRes = R.drawable.banner_veggies
        ),
        // 2. Green Banner (Mesh Bag)
        BannerData(
            title = "Up to 25% offer",
            subtitle = "On first buyers",
            buttonText = "Shop Now",
            backgroundColor = Color(0xFFE8F5E9), // Light Green
            buttonColor = Color(0xFF43A047), // Green
            buttonTextColor = Color.White,
            titleColor = Color.Black,
            subtitleColor = Color.Black,
            imageRes = R.drawable.banner_mesh_bag
        ),
        // 3. Yellow Banner (Grocery Bag)
        BannerData(
            title = "Get Same day Deliver",
            subtitle = "On orders above $20",
            buttonText = "Shop Now",
            backgroundColor = Color(0xFFFFF176), // Yellow
            buttonColor = Color.White,
            buttonTextColor = Color.Black,
            titleColor = Color.Black,
            subtitleColor = Color.Black,
            imageRes = R.drawable.banner_grocery_bag
        ),
         // 4. Pale Green Banner (Another Basket - reusing veggies for now or mesh bag)
        BannerData(
            title = "Up to 30% offer",
            subtitle = "Enjoy our big offer",
            buttonText = "Shop Now",
            backgroundColor = Color(0xFFE8F5E9), // Light Green
            buttonColor = Color(0xFF43A047), // Green
            buttonTextColor = Color.White,
            titleColor = Color.Black,
            subtitleColor = Color(0xFF43A047),
            imageRes = R.drawable.banner_veggies 
        )
    )

    val pagerState = rememberPagerState(pageCount = { banners.size })
    
    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000) // 4 seconds
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp) // Slightly taller
    ) { page ->
        val banner = banners[page]
        BannerItem(banner)
    }
}

@Composable
fun BannerItem(banner: BannerData) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = banner.backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp), // Padding only on left for text
                verticalAlignment = Alignment.Top // text align top-ish
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.55f) // Text takes bit more than half
                        .padding(top = 24.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = banner.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = banner.titleColor,
                        lineHeight = MaterialTheme.typography.titleLarge.fontSize * 1.2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = banner.subtitleColor, 
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(containerColor = banner.buttonColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = banner.buttonText, 
                            color = banner.buttonTextColor, 
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Image - Allow it to overlap/fill the right side
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                   Image(
                        painter = painterResource(id = banner.imageRes),
                        contentDescription = "Offer Image",
                        contentScale = ContentScale.Crop, // Crop to fill the right side effectively
                        alignment = Alignment.BottomEnd, // Anchor to bottom right
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp) // Push down slightly if needed, or allow full height
                            .clip(RoundedCornerShape(bottomEnd = 16.dp))
                    )
                }
            }
        }
    }
}



@Composable
fun CategorySection(
    selectedCategory: ProductCategory,
    onCategorySelected: (ProductCategory) -> Unit
) {
    val categories = listOf(
        ProductCategory.ALL,
        ProductCategory.FRUITS,
        ProductCategory.VEGETABLES,
        ProductCategory.MILK_AND_EGG,
        ProductCategory.BEVERAGES,
        ProductCategory.LAUNDRY
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        PaddingValues(horizontal = 24.dp)
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

// Map categories to local drawables
fun getCategoryIconRes(category: ProductCategory): Int {
    return when (category) {
        ProductCategory.FRUITS -> R.drawable.cat_fruits
        ProductCategory.VEGETABLES -> R.drawable.cat_veggies
        ProductCategory.LAUNDRY -> R.drawable.cat_laundry
        ProductCategory.MILK_AND_EGG -> R.drawable.cat_milk
        ProductCategory.BEVERAGES -> R.drawable.cat_beverages
        ProductCategory.ALL -> R.drawable.banner_grocery_bag
        ProductCategory.GROCERIES -> R.drawable.banner_grocery_bag
        else -> R.drawable.ic_launcher_foreground // Fallback
    }
}

@Composable
fun CategoryItem(
    category: ProductCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // Animated selection circle
        val backgroundColor = if (isSelected) WelcomeScreenGreen else Color(0xFFF5F6FA)
        val iconTint = if (isSelected) Color.White else Color.Unspecified // Or keep original colors
        
        // For images, we usually want original colors unless it's a monochrome icon system.
        // Assuming these are colored illustrations, let's keep a subtle background change.
        
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (isSelected) WelcomeScreenGreen.copy(alpha = 0.1f) else Color(0xFFF5F6FA),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, WelcomeScreenGreen) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = getCategoryIconRes(category)),
                    contentDescription = category.displayName,
                    contentScale = ContentScale.Crop, // Changed to Fit to avoid cutting off
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) WelcomeScreenGreen else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "See all",
            style = MaterialTheme.typography.labelLarge,
            color = WelcomeScreenGreen,
            modifier = Modifier.clickable(onClick = onSeeAllClick)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onScanClick: () -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Voice Search Launcher
    val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                onQueryChange(results[0])
                onSearch() // Optional: Trigger search immediately
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .clickable(onClick = onSearchClick), // Make the whole area clickable
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = WelcomeScreenGreen,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "Search products, brands...",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Scanner Icon
            IconButton(onClick = onScanClick) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan Barcode",
                    tint = WelcomeScreenGreen
                )
            }
            
            // Microphone Icon
            IconButton(onClick = {
                val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                }
                try {
                    voiceLauncher.launch(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Voice search not supported", android.widget.Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = WelcomeScreenGreen
                )
            }
        }
    }
}

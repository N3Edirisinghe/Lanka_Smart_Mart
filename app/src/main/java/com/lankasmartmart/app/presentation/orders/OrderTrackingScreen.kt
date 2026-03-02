package com.lankasmartmart.app.presentation.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.lankasmartmart.app.R
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.util.BoundingBox
import org.osmdroid.config.Configuration

@Composable
fun OrderTrackingScreen(
    onBackClick: () -> Unit,
    viewModel: OrderTrackingViewModel = hiltViewModel()
) {
    val userLocation by viewModel.userLocation.collectAsState()
    val riderLocation by viewModel.riderLocation.collectAsState()
    val status by viewModel.deliveryStatus.collectAsState()
    val eta by viewModel.estimatedTime.collectAsState()
    val routePath by viewModel.routePath.collectAsState()

    // Map Configuration
    val context = androidx.compose.ui.platform.LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            // Ensure user agent is set for OSM policy
            Configuration.getInstance().userAgentValue = context.packageName
        }
    }
    
    // Manage Lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }

    // Update Map Elements
    LaunchedEffect(userLocation, riderLocation, routePath) {
        mapView.overlays.clear()
        
        // 1. Route Polyline
        if (routePath.isNotEmpty()) {
            val polyline = Polyline().apply {
                outlinePaint.color = android.graphics.Color.parseColor("#1976D2")
                outlinePaint.strokeWidth = 15f
                setPoints(routePath.map { GeoPoint(it.latitude, it.longitude) })
            }
            mapView.overlays.add(polyline)
        }

        // 2. User Marker (Home)
        val userMarker = Marker(mapView).apply {
             position = GeoPoint(userLocation.latitude, userLocation.longitude)
             title = "Delivery Location"
             snippet = "Home"
             // Use original colors of the vector
             icon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_home_3d)
             setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(userMarker)
        
        // 3. Rider Marker (Bike)
        val riderMarker = Marker(mapView).apply {
             position = GeoPoint(riderLocation.latitude, riderLocation.longitude)
             title = "Rider"
             snippet = "On the way"
             // Use original colors of the vector
             icon = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_delivery_bike)
             setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER) // Center for the vehicle
        }
        mapView.overlays.add(riderMarker)
        
        mapView.invalidate() // Redraw
    }
    
    // Calculate Distance for UI Logic
    var isArrived by remember { mutableStateOf(false) }
    
    // Animate Camera to Rider
    LaunchedEffect(riderLocation) {
        val userPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
        val riderPoint = GeoPoint(riderLocation.latitude, riderLocation.longitude)
        
        // Calculate Bounding Box
        val boundingBox = BoundingBox.fromGeoPoints(listOf(userPoint, riderPoint))
        
        val distance = userPoint.distanceToAsDouble(riderPoint)
        
        // Update Arrived State
        isArrived = distance < 100 // Arrived if within 100m
        
        if (distance < 500) { // Less than 500 meters
             mapView.controller.animateTo(riderPoint)
             if (mapView.zoomLevelDouble < 17.0) {
                 mapView.controller.setZoom(17.0)
             }
        } else {
             // Zoom to show both with padding
             mapView.zoomToBoundingBox(boundingBox, true, 200)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Map View
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .size(45.dp)
                .background(Color.White, CircleShape)
                .shadow(elevation = 6.dp, shape = CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
        }

        // Bottom Info Sheet
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.LightGray)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Title Text Logic
                val mainTitle = when {
                    status == "Delivered" -> "Order Delivered"
                    isArrived -> "Rider Arrived!"
                    else -> "Arriving in $eta"
                }
                
                val subTitle = when {
                    status == "Delivered" -> "Enjoy your items!"
                    isArrived -> "Meet the rider at the gate"
                    else -> "Latest arrival estimate"
                }

                // Status & ETA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mainTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isArrived) com.lankasmartmart.app.ui.theme.WelcomeScreenGreen else Color.Black
                        )
                        Text(
                            text = subTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    
                    // Circular Progress / Timer Visual
                    if (status != "Delivered" && !isArrived) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD))
                        ) {
                             Text(
                                text = eta.replace(" min", "").trim(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                    } else if (isArrived) {
                         Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(com.lankasmartmart.app.ui.theme.WelcomeScreenGreen.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFF5F5F5))
                Spacer(modifier = Modifier.height(24.dp))
                
                // Rider Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rider Photo (Using a styled Person icon as placeholder for "Img")
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEEEEEE),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Rider Avatar",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kamal Perera",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Yamaha FZ • ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(" 4.8", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                        }
                    }
                    
                    // Call Action
                    val context = androidx.compose.ui.platform.LocalContext.current
                    FilledIconButton(
                        onClick = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:0771234567")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(50.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = com.lankasmartmart.app.ui.theme.WelcomeScreenGreen)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                    }
                }
            }
        }
    }
}




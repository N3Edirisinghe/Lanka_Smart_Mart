package com.lankasmartmart.app.presentation.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lankasmartmart.app.R
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.ui.theme.White

@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WelcomeScreenGreen) // Base background is Green
    ) {
        // Topographic Pattern Overlay
        com.lankasmartmart.app.presentation.components.TopographicBackground(
            modifier = Modifier.fillMaxSize(),
            color = White.copy(alpha = 0.1f)
        )

        // Draw the White Wavy Bottom
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val waveHeight = height * 0.55f // Wave starts around 55% down

            val path = Path().apply {
                moveTo(0f, height) // Bottom Left
                lineTo(width, height) // Bottom Right
                lineTo(width, waveHeight) // Top Right of the white area
                
                // Curve from Right to Left
                // We wanted a curve that goes slightly up then down then up? 
                // Looking at image: Right side is lower (more green), Left side is higher (less green)?
                // Actually image: Green covers top. White is at bottom.
                // Left edge: White starts at ~50%. 
                // Right edge: White starts at ~35%? (Green is deeper on right)
                // Let's approximate.
                
                cubicTo(
                    width * 0.6f, waveHeight + 200f, // Control point 1 (Low)
                    width * 0.3f, waveHeight - 150f, // Control point 2 (High)
                    0f, waveHeight + 50f             // End point at Left
                )
                close()
            }

            drawPath(
                path = path,
                color = White
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            
            Text(
                text = "Welcome",
                fontSize = 40.sp, // Large size
                fontWeight = FontWeight.Bold,
                color = WelcomeScreenGreen // Matching the green
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Fresh shopping made simple and stress-free.",
                fontSize = 16.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Continue Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("login") }, // Make whole row clickable for better UX or just button
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Continue",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Green Circle with Arrow
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WelcomeScreenGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward),
                        contentDescription = "Continue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

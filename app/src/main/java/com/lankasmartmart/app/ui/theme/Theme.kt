package com.lankasmartmart.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.ui.graphics.toArgb

private val DarkColorScheme = darkColorScheme(
    primary = SaffronOrange,
    onPrimary = BackgroundDark,
    primaryContainer = SaffronOrangeDark,
    onPrimaryContainer = OffWhite,
    
    secondary = EmeraldGreen,
    onSecondary = BackgroundDark,
    secondaryContainer = MaroonRed,
    onSecondaryContainer = OffWhite,
    
    tertiary = GoldenYellow,
    onTertiary = BackgroundDark,
    
    background = com.lankasmartmart.app.ui.theme.BackgroundDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    
    surface = com.lankasmartmart.app.ui.theme.SurfaceDark,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = com.lankasmartmart.app.ui.theme.DarkGray,
    onSurfaceVariant = com.lankasmartmart.app.ui.theme.LightGray,
    
    error = ErrorRed,
    onError = BackgroundDark,
    
    outline = MediumGray
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    onPrimary = SurfaceLight,
    primaryContainer = SaffronOrangeDark,
    onPrimaryContainer = SurfaceLight,
    
    secondary = EmeraldGreen,
    onSecondary = SurfaceLight,
    secondaryContainer = MaroonRed,
    onSecondaryContainer = OffWhite,
    
    tertiary = GoldenYellow,
    onTertiary = DarkGray,
    
    background = androidx.compose.ui.graphics.Color(0xFFF5F6FA), // The light gray professional background that we migrated away from hardcoding
    onBackground = com.lankasmartmart.app.ui.theme.DarkGray,
    
    surface = androidx.compose.ui.graphics.Color.White, // White cards
    onSurface = com.lankasmartmart.app.ui.theme.DarkGray,
    surfaceVariant = com.lankasmartmart.app.ui.theme.LightGray,
    onSurfaceVariant = com.lankasmartmart.app.ui.theme.DarkGray,
    
    error = ErrorRed,
    onError = SurfaceLight,
    
    outline = MediumGray
)

@Composable
fun LankaSmartMartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

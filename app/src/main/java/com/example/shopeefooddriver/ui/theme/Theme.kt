package com.example.shopeefooddriver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ShopeeOrange = Color(0xFFEE4D2D)
private val ShopeeGreen = Color(0xFF00A651)

private val LightColorScheme = lightColorScheme(
    primary = ShopeeOrange,
    secondary = ShopeeGreen,
    tertiary = ShopeeOrange
)

private val DarkColorScheme = darkColorScheme(
    primary = ShopeeOrange,
    secondary = ShopeeGreen,
    tertiary = ShopeeOrange
)

@Composable
fun ShopeeFoodDriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

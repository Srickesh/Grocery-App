package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenDarkPrimary,
    onPrimary = ForestGreenDarkOnPrimary,
    primaryContainer = ForestGreenDarkContainer,
    onPrimaryContainer = ForestGreenDarkOnContainer,
    secondary = HarvestOrangeDarkSecondary,
    onSecondary = HarvestOrangeDarkOnSecondary,
    secondaryContainer = HarvestOrangeDarkContainer,
    onSecondaryContainer = HarvestOrangeDarkOnContainer,
    tertiary = LeafGreenTertiary,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = HarvestOrangeSecondary,
    onSecondary = HarvestOrangeOnSecondary,
    secondaryContainer = HarvestOrangeContainer,
    onSecondaryContainer = HarvestOrangeOnContainer,
    tertiary = LeafGreenTertiary,
    onTertiary = LeafGreenOnTertiary,
    tertiaryContainer = LeafGreenContainer,
    onTertiaryContainer = LeafGreenOnContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun PratyushStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    LekhaliFreshTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun LekhaliFreshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to maintain consistent organic brand aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryContainerGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerOrange
import com.example.ui.theme.TertiaryContainerBlue

@Composable
fun ProductVisual(
    productId: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp
) {
    val (icon, bgColors, tintColor) = getProductVisualMeta(productId)

    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = bgColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getProductVisualMeta(productId: String): Triple<ImageVector, List<Color>, Color> {
    return when {
        productId.contains("rice") || productId.contains("atta") -> Triple(
            Icons.Default.RiceBowl,
            listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)),
            Color(0xFFF57F17)
        )
        productId.contains("milk") || productId.contains("egg") -> Triple(
            if (productId.contains("egg")) Icons.Default.EggAlt else Icons.Default.WaterDrop,
            listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
            TertiaryContainerBlue
        )
        productId.contains("apple") || productId.contains("fruit") -> Triple(
            Icons.Default.LocalFlorist,
            listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2)),
            Color(0xFFD32F2F)
        )
        productId.contains("juice") || productId.contains("beverage") -> Triple(
            Icons.Default.EmojiFoodBeverage,
            listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
            SecondaryContainerOrange
        )
        productId.contains("bread") || productId.contains("bakery") -> Triple(
            Icons.Default.BakeryDining,
            listOf(Color(0xFFEFEBE9), Color(0xFFD7CCC8)),
            Color(0xFF795548)
        )
        productId.contains("trout") || productId.contains("fish") || productId.contains("meat") -> Triple(
            Icons.Default.SetMeal,
            listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)),
            Color(0xFF00838F)
        )
        productId.contains("spinach") || productId.contains("veg") || productId.contains("tomato") -> Triple(
            Icons.Default.Spa,
            listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
            PrimaryGreen
        )
        else -> Triple(
            Icons.Default.Spa,
            listOf(Color(0xFFF1F8E9), Color(0xFFDCEDC8)),
            PrimaryContainerGreen
        )
    }
}

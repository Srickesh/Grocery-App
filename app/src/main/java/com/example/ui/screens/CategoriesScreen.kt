package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Category
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.components.TopAppHeader
import com.example.ui.theme.PrimaryContainerGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryContainerOrange
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    locationName: String,
    userName: String,
    categories: List<Category>,
    selectedCategoryId: String,
    products: List<Product>,
    wishlistIds: Set<String>,
    filterState: FilterState,
    onSelectCategory: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onLocationClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onUpdateOrganicFilter: (Boolean) -> Unit,
    onUpdateLocalFilter: (Boolean) -> Unit,
    onUpdateExpressFilter: (Boolean) -> Unit,
    onUpdateInStockFilter: (Boolean) -> Unit,
    onUpdateSortOption: (SortOption) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCategory = categories.find { it.id == selectedCategoryId } ?: categories.first()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Header
        TopAppHeader(
            locationName = locationName,
            userName = userName,
            showSearchBar = true,
            showBackButton = false,
            onLocationClick = onLocationClick,
            onNotificationClick = onNotificationClick,
            onSearchClick = onSearchClick,
            onBarcodeClick = onSearchClick
        )

        // Horizontal Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category.id == selectedCategoryId
                val categoryIcon = when (category.id) {
                    "veg" -> Icons.Default.Spa
                    "fruits" -> Icons.Default.LocalFlorist
                    "meat" -> Icons.Default.SetMeal
                    "dairy" -> Icons.Default.EggAlt
                    "bakery" -> Icons.Default.BakeryDining
                    "grains" -> Icons.Default.Grain
                    else -> Icons.Default.LocalDrink
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryContainerGreen else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .testTag("category_tab_${category.id}")
                        .clickable { onSelectCategory(category.id) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Product Catalog List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("category_products_column"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Category Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_veg_hero),
                                contentDescription = activeCategory.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                            )

                            // Text Content
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = activeCategory.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "${activeCategory.subtitle} ${products.size} items.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Toolbar: Filter & Sort Controls
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${products.size} products",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filters Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLowest,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .testTag("filter_dialog_button")
                                .clickable { showFilterSheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Filters",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        // Sort Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLowest,
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .testTag("sort_button")
                                .clickable {
                                    val nextSort = when (filterState.sortOption) {
                                        SortOption.POPULAR, SortOption.POPULARITY -> SortOption.PRICE_LOW_TO_HIGH
                                        SortOption.PRICE_LOW_TO_HIGH, SortOption.PRICE_LOW_HIGH -> SortOption.PRICE_HIGH_TO_LOW
                                        SortOption.PRICE_HIGH_TO_LOW, SortOption.PRICE_HIGH_LOW -> SortOption.RATING
                                        else -> SortOption.POPULAR
                                    }
                                    onUpdateSortOption(nextSort)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (filterState.sortOption) {
                                        SortOption.POPULAR, SortOption.POPULARITY -> "Sort: Popular"
                                        SortOption.PRICE_LOW_TO_HIGH, SortOption.PRICE_LOW_HIGH -> "Price: Low-High"
                                        SortOption.PRICE_HIGH_TO_LOW, SortOption.PRICE_HIGH_LOW -> "Price: High-Low"
                                        SortOption.RATING -> "Top Rated"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }

            // Products Grid
            items(products.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProductCard(
                        product = pair[0],
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart,
                        isWishlisted = wishlistIds.contains(pair[0].id),
                        onToggleWishlist = onToggleWishlist,
                        modifier = Modifier.weight(1f)
                    )

                    if (pair.size > 1) {
                        ProductCard(
                            product = pair[1],
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            isWishlisted = wishlistIds.contains(pair[1].id),
                            onToggleWishlist = onToggleWishlist,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Products",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onResetFilters) {
                        Text("Reset All", color = PrimaryGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateOrganicFilter(!filterState.organicOnly) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = filterState.organicOnly,
                        onCheckedChange = { onUpdateOrganicFilter(it) },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Organic Certified Only", style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateLocalFilter(!filterState.localOnly) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = filterState.localOnly,
                        onCheckedChange = { onUpdateLocalFilter(it) },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Locally Harvested (Nepal)", style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateExpressFilter(!filterState.expressOnly) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = filterState.expressOnly,
                        onCheckedChange = { onUpdateExpressFilter(it) },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Express Delivery (45 Mins)", style = MaterialTheme.typography.bodyLarge)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateInStockFilter(!filterState.inStockOnly) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = filterState.inStockOnly,
                        onCheckedChange = { onUpdateInStockFilter(it) },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("In Stock Items Only", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

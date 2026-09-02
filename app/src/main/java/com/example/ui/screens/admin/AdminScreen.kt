package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestOrangeSecondary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.GroceryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    groceryViewModel: GroceryViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var inputUsername by remember { mutableStateOf("admin@pratyushgrocery.com") }
    var inputPassword by remember { mutableStateOf("admin@123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    // Admin Tabs: 0 -> Overview, 1 -> Orders, 2 -> Inventory, 3 -> Coupons
    var selectedTab by remember { mutableIntStateOf(0) }

    val allOrders by groceryViewModel.allStoreOrders.collectAsState()
    val allProducts by groceryViewModel.allProductsFlow.collectAsState()
    val activeCoupons by groceryViewModel.activeCoupons.collectAsState()

    // Dialog States
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showEditProductDialog by remember { mutableStateOf<Product?>(null) }
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<Order?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    if (!isAuthenticated) {
        // Admin Login Gate Screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Security Portal", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pratyush Store Manager",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Store Operations & Live Inventory Control",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Admin Credentials",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputUsername,
                            onValueChange = {
                                inputUsername = it
                                loginErrorMessage = null
                            },
                            label = { Text("Admin Username / Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputPassword,
                            onValueChange = {
                                inputPassword = it
                                loginErrorMessage = null
                            },
                            label = { Text("Admin Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (loginErrorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loginErrorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val user = inputUsername.trim().lowercase()
                                val pass = inputPassword.trim()
                                val isValidUser = user == "admin@pratyushgrocery.com" || user == "admin" || user == "pratyush"
                                val isValidPass = pass == "admin@123" || pass == "7272" || pass == "admin123" || pass == "pratyush123"

                                if (isValidUser && isValidPass) {
                                    isAuthenticated = true
                                } else {
                                    loginErrorMessage = "Invalid credentials. Use: admin@pratyushgrocery.com / admin@123"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_login_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Access Admin Panel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick demo credentials helper box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ForestGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Store Admin Credentials:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Username: admin@pratyushgrocery.com\n• Password: admin@123 (or PIN 7272)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        inputUsername = "admin@pratyushgrocery.com"
                                        inputPassword = "admin@123"
                                        loginErrorMessage = null
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Demo 1-Tap Auto-Fill", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Authenticated Admin Dashboard
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pratyush Store Manager", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Admin: admin@pratyushgrocery.com",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isAuthenticated = false }) {
                        Icon(Icons.Default.Lock, contentDescription = "Lock Admin", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Bar
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                val tabTitles = listOf("📊 Overview", "📦 Orders (${allOrders.size})", "🥦 Products (${allProducts.size})", "🎟️ Coupons (${activeCoupons.size})")
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> AdminOverviewTab(
                    orders = allOrders,
                    products = allProducts,
                    coupons = activeCoupons,
                    onGoToOrders = { selectedTab = 1 },
                    onGoToProducts = { selectedTab = 2 },
                    onGoToCoupons = { selectedTab = 3 },
                    onAddNewProduct = { showAddProductDialog = true }
                )
                1 -> AdminOrdersTab(
                    orders = allOrders,
                    onUpdateStatus = { orderId, newStatus ->
                        groceryViewModel.adminUpdateOrderStatus(orderId, newStatus)
                    },
                    onDeleteOrder = { order ->
                        orderToDelete = order
                    }
                )
                2 -> AdminProductsTab(
                    products = allProducts,
                    categories = groceryViewModel.categories,
                    onAddNewProduct = { showAddProductDialog = true },
                    onEditProduct = { prod -> showEditProductDialog = prod },
                    onDeleteProduct = { prod -> productToDelete = prod }
                )
                3 -> AdminCouponsTab(
                    coupons = activeCoupons,
                    onAddCoupon = { showAddCouponDialog = true },
                    onDeleteCoupon = { code -> groceryViewModel.adminDeleteCoupon(code) }
                )
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            categories = groceryViewModel.categories,
            onDismiss = { showAddProductDialog = false },
            onAdd = { newProd ->
                groceryViewModel.adminAddProduct(newProd)
                showAddProductDialog = false
            }
        )
    }

    // Edit Product Dialog
    if (showEditProductDialog != null) {
        EditProductDialog(
            product = showEditProductDialog!!,
            onDismiss = { showEditProductDialog = null },
            onSave = { updated ->
                groceryViewModel.adminUpdateProduct(updated)
                showEditProductDialog = null
            }
        )
    }

    // Add Coupon Dialog
    if (showAddCouponDialog) {
        AddCouponDialog(
            onDismiss = { showAddCouponDialog = false },
            onAdd = { code, discount ->
                groceryViewModel.adminAddCoupon(code, discount)
                showAddCouponDialog = false
            }
        )
    }

    // Confirm Delete Order Dialog
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Delete Order ${orderToDelete!!.orderId}?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete this order from the system database?") },
            confirmButton = {
                Button(
                    onClick = {
                        groceryViewModel.adminDeleteOrder(orderToDelete!!.orderId)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Delete Product Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Remove Product?", fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${productToDelete!!.name}' from the store catalog?") },
            confirmButton = {
                Button(
                    onClick = {
                        groceryViewModel.adminDeleteProduct(productToDelete!!.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// Tab 0: Overview Dashboard
// -----------------------------------------------------------------------------------------
@Composable
fun AdminOverviewTab(
    orders: List<Order>,
    products: List<Product>,
    coupons: Map<String, Double>,
    onGoToOrders: () -> Unit,
    onGoToProducts: () -> Unit,
    onGoToCoupons: () -> Unit,
    onAddNewProduct: () -> Unit
) {
    val totalRevenue = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    val pendingOrders = orders.count { it.status == OrderStatus.PLACED || it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.PREPARING }
    val outForDeliveryOrders = orders.count { it.status == OrderStatus.OUT_FOR_DELIVERY }
    val deliveredOrders = orders.count { it.status == OrderStatus.DELIVERED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Revenue Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Gross Sales",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "NPR ${String.format(Locale.US, "%,.2f", totalRevenue)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Real-time revenue from ${orders.size} lifetime store orders",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending",
                    value = "$pendingOrders",
                    subtitle = "Action Needed",
                    color = HarvestOrangeSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = onGoToOrders
                )
                StatCard(
                    title = "Out for Delivery",
                    value = "$outForDeliveryOrders",
                    subtitle = "In Transit",
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = onGoToOrders
                )
                StatCard(
                    title = "Delivered",
                    value = "$deliveredOrders",
                    subtitle = "Completed",
                    color = ForestGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onGoToOrders
                )
            }
        }

        // Quick Management Shortcuts
        item {
            Text(
                text = "Store Operations",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OperationCard(
                    icon = Icons.Default.Add,
                    title = "Add Product",
                    subtitle = "New catalog item",
                    modifier = Modifier.weight(1f),
                    onClick = onAddNewProduct
                )
                OperationCard(
                    icon = Icons.Default.Inventory,
                    title = "Inventory",
                    subtitle = "${products.size} Products",
                    modifier = Modifier.weight(1f),
                    onClick = onGoToProducts
                )
                OperationCard(
                    icon = Icons.Default.ConfirmationNumber,
                    title = "Promo Codes",
                    subtitle = "${coupons.size} Active",
                    modifier = Modifier.weight(1f),
                    onClick = onGoToCoupons
                )
            }
        }

        // Recent Orders list in Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Customer Orders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onGoToOrders) {
                    Text("View All (${orders.size})")
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No customer orders placed yet.", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Place a test order from the Basket to inspect live updates here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(orders.take(3), key = { it.orderId }) { order ->
                OrderSummaryCard(order = order, onClick = onGoToOrders)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OperationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 1: Orders Management
// -----------------------------------------------------------------------------------------
@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    onDeleteOrder: (Order) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    val filteredOrders = if (selectedFilter == null) orders else orders.filter { it.status == selectedFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${orders.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                OrderStatus.values().forEach { status ->
                    val count = orders.count { it.status == status }
                    item {
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = if (selectedFilter == status) null else status },
                            label = { Text("${status.displayName} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No orders match this filter.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.orderId }) { order ->
                AdminOrderCard(
                    order = order,
                    onUpdateStatus = { newStatus -> onUpdateStatus(order.orderId, newStatus) },
                    onDelete = { onDeleteOrder(order) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onUpdateStatus: (OrderStatus) -> Unit,
    onDelete: () -> Unit
) {
    var showStatusDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID + Status + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge with Dropdown Trigger
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (order.status) {
                                OrderStatus.PLACED -> MaterialTheme.colorScheme.primaryContainer
                                OrderStatus.CONFIRMED -> Color(0xFFE0F2FE)
                                OrderStatus.PREPARING -> Color(0xFFFEF3C7)
                                OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFE0E7FF)
                                OrderStatus.DELIVERED -> Color(0xFFDCFCE7)
                                OrderStatus.CANCELLED -> Color(0xFFFEE2E2)
                            },
                            modifier = Modifier.clickable { showStatusDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.status.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (order.status) {
                                        OrderStatus.DELIVERED -> ForestGreenPrimary
                                        OrderStatus.CANCELLED -> Color.Red
                                        OrderStatus.PREPARING -> HarvestOrangeSecondary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("▼", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        DropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            OrderStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = {
                                        onUpdateStatus(status)
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Order",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Customer info
            Text(
                text = "Customer: ${order.customerName} (${order.customerPhone})",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Address: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Slot: ${order.deliveryDate} • ${order.deliverySlot} ${if (order.isExpress) "⚡ Express" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Items breakdown
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.emoji} ${item.productName} x ${item.quantity}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "NPR ${String.format(Locale.US, "%.0f", item.unitPrice * item.quantity)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment: ${order.paymentMethod.title}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: NPR ${String.format(Locale.US, "%.2f", order.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                )
            }
        }
    }
}

@Composable
fun OrderSummaryCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Order #${order.orderId}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "${order.customerName} • ${order.items.size} items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "NPR ${String.format(Locale.US, "%.0f", order.totalAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary))
                Text(text = order.status.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 2: Inventory & Products Management
// -----------------------------------------------------------------------------------------
@Composable
fun AdminProductsTab(
    products: List<Product>,
    categories: List<com.example.data.model.Category>,
    onAddNewProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }

    val filteredProducts = products.filter { prod ->
        (selectedCategory == "all" || prod.categoryId == selectedCategory) &&
        (searchQuery.isBlank() || prod.name.contains(searchQuery, ignoreCase = true) || prod.nepaliName.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Product Action Button & Search
        item {
            Button(
                onClick = onAddNewProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_add_product_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Product to Store Catalog", fontWeight = FontWeight.Bold)
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search catalog products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category scroll
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        label = { Text("All (${products.size})") }
                    )
                }
                categories.filter { it.id != "all" }.forEach { cat ->
                    val count = products.count { it.categoryId == cat.id }
                    item {
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = { selectedCategory = cat.id },
                            label = { Text("${cat.emoji} ${cat.name} ($count)") }
                        )
                    }
                }
            }
        }

        items(filteredProducts, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = product.iconEmoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${product.nepaliName} • ${product.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NPR ${String.format(Locale.US, "%.0f", product.price)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                                )
                                if (product.isOrganic) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = ForestGreenPrimary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Organic",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = ForestGreenPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { onEditProduct(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Product", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// -----------------------------------------------------------------------------------------
// Tab 3: Coupons Management
// -----------------------------------------------------------------------------------------
@Composable
fun AdminCouponsTab(
    coupons: Map<String, Double>,
    onAddCoupon: () -> Unit,
    onDeleteCoupon: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddCoupon,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_add_coupon_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Promo Code / Voucher", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "Active Discount Codes (${coupons.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        coupons.entries.forEach { (code, discountVal) ->
            item(key = code) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HarvestOrangeSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = HarvestOrangeSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                )
                                val discountText = if (discountVal <= 1.0) {
                                    "${(discountVal * 100).toInt()}% Percentage OFF"
                                } else {
                                    "Flat NPR ${discountVal.toInt()} OFF"
                                }
                                Text(
                                    text = discountText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteCoupon(code) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Coupon", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// -----------------------------------------------------------------------------------------
// Dialogs: Add Product, Edit Product, Add Coupon
// -----------------------------------------------------------------------------------------

@Composable
fun AddProductDialog(
    categories: List<com.example.data.model.Category>,
    onDismiss: () -> Unit,
    onAdd: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nepaliName by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var originalPriceStr by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("1 kg") }
    var emoji by remember { mutableStateOf("🥬") }
    var originValley by remember { mutableStateOf("Kathmandu Valley") }
    var description by remember { mutableStateOf("") }
    var isOrganic by remember { mutableStateOf(true) }
    var isLocalHarvest by remember { mutableStateOf(true) }
    var selectedCatId by remember { mutableStateOf(categories.firstOrNull { it.id != "all" }?.id ?: "fruits_vegetables") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Product", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product English Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nepaliName,
                    onValueChange = { nepaliName = it },
                    label = { Text("Nepali Name (नेपाली नाम) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Selling Price (NPR) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = originalPriceStr,
                        onValueChange = { originalPriceStr = it },
                        label = { Text("Original Price (NPR)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g. 1 kg, 500g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji Icon") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }

                OutlinedTextField(
                    value = originValley,
                    onValueChange = { originValley = it },
                    label = { Text("Origin / Farm Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOrganic, onCheckedChange = { isOrganic = it })
                    Text("Organic Certified", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLocalHarvest, onCheckedChange = { isLocalHarvest = it })
                    Text("Local Nepal Harvest", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 100.0
                    val originalPrice = originalPriceStr.toDoubleOrNull() ?: (price * 1.2)
                    val newProduct = Product(
                        id = "prod_" + System.currentTimeMillis(),
                        name = name.ifBlank { "Fresh Organic Item" },
                        nepaliName = nepaliName.ifBlank { name },
                        price = price,
                        originalPrice = originalPrice,
                        unit = unit.ifBlank { "1 unit" },
                        categoryId = selectedCatId,
                        isOrganic = isOrganic,
                        isLocalHarvest = isLocalHarvest,
                        originValley = originValley.ifBlank { "Kathmandu Valley" },
                        rating = 5.0,
                        reviewCount = 1,
                        description = description.ifBlank { "Freshly sourced from Pratyush Grocery Store partner farms." },
                        nutritionalFacts = "Fresh, natural and pure.",
                        isFlashDeal = false,
                        iconEmoji = emoji.ifBlank { "🥬" }
                    )
                    onAdd(newProduct)
                }
            ) {
                Text("Add Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var nepaliName by remember { mutableStateOf(product.nepaliName) }
    var priceStr by remember { mutableStateOf(product.price.toString()) }
    var unit by remember { mutableStateOf(product.unit) }
    var originValley by remember { mutableStateOf(product.originValley) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${product.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nepaliName,
                    onValueChange = { nepaliName = it },
                    label = { Text("Nepali Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price (NPR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = originValley,
                    onValueChange = { originValley = it },
                    label = { Text("Origin / Farm Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPrice = priceStr.toDoubleOrNull() ?: product.price
                    val updated = product.copy(
                        name = name,
                        nepaliName = nepaliName,
                        price = updatedPrice,
                        unit = unit,
                        originValley = originValley
                    )
                    onSave(updated)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddCouponDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var discountValueStr by remember { mutableStateOf("20") }
    var isPercentage by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Promo Code", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Promo Code (e.g. FESTIVAL30)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isPercentage,
                        onClick = { isPercentage = true },
                        label = { Text("Percentage (%) OFF") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isPercentage,
                        onClick = { isPercentage = false },
                        label = { Text("Flat NPR OFF") }
                    )
                }

                OutlinedTextField(
                    value = discountValueStr,
                    onValueChange = { discountValueStr = it },
                    label = { Text(if (isPercentage) "Discount Percentage (e.g. 20 for 20%)" else "Flat Discount (NPR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawVal = discountValueStr.toDoubleOrNull() ?: 10.0
                    val finalVal = if (isPercentage) (rawVal / 100.0) else rawVal
                    if (code.isNotBlank()) {
                        onAdd(code.trim().uppercase(), finalVal)
                    }
                }
            ) {
                Text("Create Promo Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

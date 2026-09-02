package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.auth.AuthState
import com.example.ui.navigation.Screen
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.cart.CartScreen
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.checkout.CheckoutScreen
import com.example.ui.screens.details.ProductDetailSheet
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.tracking.LiveTrackingScreen
import com.example.ui.theme.LekhaliFreshTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.GroceryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val groceryViewModel: GroceryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LekhaliFreshTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val cartItems by groceryViewModel.cartItems.collectAsState()
                val totalCartCount = cartItems.values.sum()
                val userOrders by groceryViewModel.userOrders.collectAsState()
                val activeOrderCount = userOrders.count {
                    it.status != com.example.data.model.OrderStatus.DELIVERED &&
                    it.status != com.example.data.model.OrderStatus.CANCELLED
                }

                val selectedProductForDetails by groceryViewModel.selectedProductForDetails.collectAsState()
                val favorites by groceryViewModel.favoriteProductIds.collectAsState()
                val userProfile by authViewModel.currentUserProfile.collectAsState()

                // Update active user in GroceryViewModel
                LaunchedEffect(userProfile) {
                    if (userProfile != null) {
                        groceryViewModel.setUserId(userProfile!!.uid)
                    }
                }

                // Listen to Toast / Snack messages
                LaunchedEffect(Unit) {
                    launch {
                        authViewModel.userMessage.collectLatest { msg ->
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                    launch {
                        groceryViewModel.userMessage.collectLatest { msg ->
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Categories.route,
                    Screen.Cart.route,
                    Screen.Orders.route,
                    Screen.Profile.route
                )

                val bottomNavItems = listOf(
                    BottomNavItem(
                        route = Screen.Home.route,
                        title = "Home",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home
                    ),
                    BottomNavItem(
                        route = Screen.Categories.route,
                        title = "Categories",
                        selectedIcon = Icons.Filled.Category,
                        unselectedIcon = Icons.Outlined.Category
                    ),
                    BottomNavItem(
                        route = Screen.Cart.route,
                        title = "Basket",
                        selectedIcon = Icons.Filled.ShoppingBag,
                        unselectedIcon = Icons.Outlined.ShoppingBag,
                        badgeCount = totalCartCount
                    ),
                    BottomNavItem(
                        route = Screen.Orders.route,
                        title = "Orders",
                        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        badgeCount = activeOrderCount
                    ),
                    BottomNavItem(
                        route = Screen.Profile.route,
                        title = "Account",
                        selectedIcon = Icons.Filled.Person,
                        unselectedIcon = Icons.Outlined.Person
                    )
                )

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            if (item.badgeCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge {
                                                            Text(
                                                                text = "${item.badgeCount}",
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                        contentDescription = item.title,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = item.title,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_item_${item.route}")
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                groceryViewModel = groceryViewModel,
                                authViewModel = authViewModel,
                                onNavigateToCategories = {
                                    navController.navigate(Screen.Categories.route)
                                },
                                onNavigateToProfile = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onProductClick = { product ->
                                    groceryViewModel.selectProductForDetails(product)
                                }
                            )
                        }

                        composable(Screen.Categories.route) {
                            CategoriesScreen(
                                groceryViewModel = groceryViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onProductClick = { product ->
                                    groceryViewModel.selectProductForDetails(product)
                                }
                            )
                        }

                        composable(Screen.Cart.route) {
                            CartScreen(
                                groceryViewModel = groceryViewModel,
                                onNavigateToCheckout = {
                                    navController.navigate(Screen.Checkout.route)
                                },
                                onExploreProducts = {
                                    navController.navigate(Screen.Home.route)
                                }
                            )
                        }

                        composable(Screen.Checkout.route) {
                            CheckoutScreen(
                                groceryViewModel = groceryViewModel,
                                authViewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onOrderPlaced = { order ->
                                    navController.navigate(Screen.Tracking.route) {
                                        popUpTo(Screen.Home.route)
                                    }
                                },
                                onRequireAuth = {
                                    navController.navigate(Screen.Auth.route)
                                }
                            )
                        }

                        composable(Screen.Orders.route) {
                            OrdersScreen(
                                groceryViewModel = groceryViewModel,
                                onTrackOrder = { order ->
                                    navController.navigate(Screen.Tracking.route)
                                },
                                onExploreHarvests = {
                                    navController.navigate(Screen.Home.route)
                                }
                            )
                        }

                        composable(Screen.Tracking.route) {
                            LiveTrackingScreen(
                                groceryViewModel = groceryViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                authViewModel = authViewModel,
                                groceryViewModel = groceryViewModel,
                                onNavigateToAuth = {
                                    navController.navigate(Screen.Auth.route)
                                },
                                onNavigateToOrders = {
                                    navController.navigate(Screen.Orders.route)
                                },
                                onNavigateToAdmin = {
                                    navController.navigate(Screen.Admin.route)
                                }
                            )
                        }

                        composable(Screen.Admin.route) {
                            AdminScreen(
                                groceryViewModel = groceryViewModel,
                                authViewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Auth.route) {
                            AuthScreen(
                                authViewModel = authViewModel,
                                onAuthSuccess = {
                                    navController.popBackStack()
                                },
                                onBackOrSkip = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    // Product Details Bottom Sheet
                    if (selectedProductForDetails != null) {
                        val product = selectedProductForDetails!!
                        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                        ProductDetailSheet(
                            product = product,
                            isFavorite = favorites.contains(product.id),
                            onDismiss = {
                                groceryViewModel.selectProductForDetails(null)
                            },
                            sheetState = sheetState,
                            onAddToCart = { qty ->
                                groceryViewModel.addToCart(product, qty)
                            },
                            onToggleFavorite = {
                                groceryViewModel.toggleFavorite(product.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.lankasmartmart.app

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lankasmartmart.app.presentation.auth.AuthViewModel
import com.lankasmartmart.app.presentation.auth.LoginScreen
import com.lankasmartmart.app.presentation.auth.SignUpScreen
import com.lankasmartmart.app.presentation.cart.CartScreen
import com.lankasmartmart.app.presentation.city.SelectCityScreen
import com.lankasmartmart.app.presentation.location.LocationSetupScreen
import com.lankasmartmart.app.presentation.home.HomeScreen
import com.lankasmartmart.app.presentation.products.ProductDetailsScreen
import com.lankasmartmart.app.presentation.profile.AccountScreen
import com.lankasmartmart.app.presentation.favorites.FavoriteScreen
import com.lankasmartmart.app.presentation.splash.SplashScreen
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import com.lankasmartmart.app.ui.theme.LankaSmartMartTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.lankasmartmart.app.presentation.theme.ThemeMode
import com.lankasmartmart.app.presentation.theme.ThemeViewModel

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            LankaSmartMartTheme(darkTheme = isDarkTheme) {
                MainApp()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Welcome : Screen("welcome", "Welcome")
    object Login : Screen("login", "Login")
    object Signup : Screen("signup", "Signup")
    object SelectCity : Screen("select_city", "Select City")
    object LocationSetup : Screen("location_setup", "Location")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Favourite : Screen("favourite", "Favourite", Icons.Default.FavoriteBorder)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Profile : Screen("profile", "Profile", Icons.Default.PersonOutline)
    object ImageSearch : Screen("image_search", "Image Search")
    object Menu : Screen("menu", "Menu", Icons.Default.Menu)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object Checkout : Screen("checkout", "Checkout")
    object Orders : Screen("orders", "Orders")
    object ProductDetails : Screen("product_details/{productId}", "Product Details") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    object OrderSuccess : Screen("order_success/{orderId}", "Order Success") {
         fun createRoute(orderId: String) = "order_success/$orderId"
    }
    object CategoryProducts : Screen("category_products/{categoryName}", "Category Products") {
        fun createRoute(categoryName: String) = "category_products/$categoryName"
    }
    object OrderDetails : Screen("order_details/{orderId}", "Order Details") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    object OrderTracking : Screen("order_tracking/{orderId}", "Track Order") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }
    object AddressList : Screen("address_list", "My Addresses")
    object AddAddress : Screen("add_address", "Add Address")

    object EditProfile : Screen("edit_profile", "Edit Profile")
    // Admin
    object AdminDashboard : Screen("admin_dashboard", "Admin Panel")
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Notification Permission for Android 13+
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Handle if granted or not, optional
        }
    )
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Cart,
        Screen.Favourite,
        Screen.Profile
    )
    
    // Only show bottom bar on these screens
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = if (isSelected && screen == Screen.Home) Icons.Filled.Home else screen.icon!!, 
                                    contentDescription = screen.title,
                                    tint = if (isSelected) com.lankasmartmart.app.ui.theme.WelcomeScreenGreen else androidx.compose.ui.graphics.Color.Black
                                ) 
                            },
                            label = { 
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) com.lankasmartmart.app.ui.theme.WelcomeScreenGreen else androidx.compose.ui.graphics.Color.Black,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
            }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }
            composable(Screen.Welcome.route) {
                com.lankasmartmart.app.presentation.welcome.WelcomeScreen(navController = navController)
            }
            composable(Screen.Login.route) {
                LoginScreen(navController = navController, viewModel = authViewModel)
            }
            composable(Screen.Signup.route) {
                SignUpScreen(navController = navController, viewModel = authViewModel)
            }
            composable(Screen.SelectCity.route) {
                SelectCityScreen(navController = navController)
            }
            composable(Screen.LocationSetup.route) {
                LocationSetupScreen(navController = navController)
            }
            composable(Screen.Home.route) { backStackEntry ->
                val viewModel: com.lankasmartmart.app.presentation.home.ProductViewModel = hiltViewModel()
                
                // Handle Scanned Barcode Result
                val scannedCode = backStackEntry.savedStateHandle.get<String>("scanned_code")
                if (scannedCode != null) {
                    viewModel.searchProducts(scannedCode)
                    backStackEntry.savedStateHandle.remove<String>("scanned_code")
                }

                HomeScreen(
                    authViewModel = authViewModel,
                    viewModel = viewModel,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onAddressClick = {
                        navController.navigate(Screen.LocationSetup.route)
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onCameraClick = {
                        navController.navigate(Screen.ImageSearch.route)
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
            
            composable(Screen.ImageSearch.route) {
                com.lankasmartmart.app.presentation.scanner.BarcodeScannerScreen(
                    onBackClick = { navController.popBackStack() },
                    onBarcodeScanned = { code ->
                         navController.previousBackStackEntry?.savedStateHandle?.set("scanned_code", code)
                         navController.popBackStack()
                    }
                )
            }
            composable(Screen.Favourite.route) {
                FavoriteScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onBackClick = {
                        // Decide where back goes. Since it's a bottom nav tab, maybe do nothing or go Home.
                        // Standard pattern for bottom nav tabs is usually no back button in top bar, 
                        // but if we keep the consistent TopAppBar with back, it should probably popBackStack or go Home.
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
             composable(Screen.Search.route) {
                com.lankasmartmart.app.presentation.search.SearchScreen(navController = navController)
            }
            composable(Screen.Orders.route) {
                com.lankasmartmart.app.presentation.orders.OrderScreen(
                    navController,
                    onBackClick = { navController.popBackStack() },
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetails.createRoute(orderId))
                    }
                )
            }
            composable(Screen.Profile.route) { backStackEntry ->
                AccountScreen(
                    profileBackStackEntry = backStackEntry,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onOrderClick = { navController.navigate(Screen.Orders.route) },
                    onAddressClick = { navController.navigate(Screen.AddressList.route) },
                    onEditProfileClick = { navController.navigate(Screen.EditProfile.route) },
                    onAdminClick = { navController.navigate(Screen.AdminDashboard.route) }
                )
            }
            composable(Screen.Menu.route) {
                com.lankasmartmart.app.presentation.menu.MenuScreen(navController = navController)
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { navController.navigate(Screen.Checkout.route) }
                )
            }
            
            composable(Screen.Checkout.route) {
                com.lankasmartmart.app.presentation.checkout.CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderSuccess = { orderId ->
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                             popUpTo(Screen.Home.route) { inclusive = false } // Keep Home in backstack? Or clear? 
                             // Let's clear checkout and cart from backstack
                             popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.lankasmartmart.app.presentation.checkout.OrderSuccessScreen(
                    orderId = orderId,
                    onHomeClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onTrackOrderClick = { oid ->
                        // Navigate to Orders Screen (ideally filtering for this order, but for now just Orders list)
                        navController.navigate(Screen.Orders.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.OrderDetails.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.lankasmartmart.app.presentation.orders.OrderDetailsScreen(
                    onBackClick = { navController.popBackStack() },
                    onTrackOrderClick = { oid ->
                        navController.navigate(Screen.OrderTracking.createRoute(oid))
                    }
                )
            }
            
            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.lankasmartmart.app.presentation.orders.OrderTrackingScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddressList.route) {
                com.lankasmartmart.app.presentation.address.AddressListScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddAddressClick = { navController.navigate(Screen.AddAddress.route) }
                )
            }
            

            composable(Screen.AddAddress.route) {
                com.lankasmartmart.app.presentation.address.AddAddressScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddressAdded = { navController.popBackStack() }
                )
            }
            
            composable(Screen.EditProfile.route) {
                com.lankasmartmart.app.presentation.profile.EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onProfileUpdated = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("profile_updated", true)
                    }
                )
            }
            
            composable(Screen.AdminDashboard.route) {
                com.lankasmartmart.app.presentation.admin.AdminDashboardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CategoryProducts.route,
                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                com.lankasmartmart.app.presentation.category.CategoryProductsScreen(
                    categoryName = categoryName,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    }
                )
            }
            composable(
                route = Screen.ProductDetails.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailsScreen(
                    productId = productId,
                    onBackClick = { navController.popBackStack() },
                    onFavoriteClick = { 
                         navController.navigate(Screen.Favourite.route) {
                            // Optional: Pop back stack to avoid circular loops if needed, but standard push is fine here
                            launchSingleTop = true 
                         }
                    }
                )
            }
        }
    }
}

package com.example.autostradaauctions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.autostradaauctions.ui.screens.HomeScreen
import com.example.autostradaauctions.ui.screens.EnhancedAuctionDetailScreen
import com.example.autostradaauctions.ui.screens.EnhancedLoginScreen
import com.example.autostradaauctions.ui.screens.UserProfileScreen
import com.example.autostradaauctions.ui.screens.FavoritesScreen
import com.example.autostradaauctions.ui.screens.AdminDashboardScreen
import com.example.autostradaauctions.ui.screens.UserDashboardScreen
import com.example.autostradaauctions.ui.screens.AuctionManagementScreen
import com.example.autostradaauctions.ui.screens.UserManagementScreen
import com.example.autostradaauctions.ui.screens.AnalyticsScreen
import com.example.autostradaauctions.ui.screens.MyBidsScreen
import com.example.autostradaauctions.data.model.UserSession
import com.example.autostradaauctions.data.model.UserRole

@Composable
fun SimpleNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Current user session state
    val currentUser = remember { mutableStateOf<UserSession?>(null) }
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onAuctionClick = { auctionId ->
                    println("DEBUG: Navigation triggered for auction ID: $auctionId")
                    try {
                        navController.navigate("auction_detail/$auctionId")
                        println("DEBUG: Navigation successful")
                    } catch (e: Exception) {
                        println("DEBUG: Navigation failed: ${e.message}")
                    }
                },
                onLoginClick = {
                    println("DEBUG: Login clicked - navigating to login")
                    navController.navigate("enhanced_login")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onFavoritesClick = {
                    navController.navigate("favorites")
                }
            )
        }
        
        composable("auction_detail/{auctionId}") { backStackEntry ->
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
            println("DEBUG: Entered auction detail screen with ID: $auctionId")
            EnhancedAuctionDetailScreen(
                auctionId = auctionId,
                onBackClick = {
                    println("DEBUG: Back button clicked")
                    navController.popBackStack()
                }
            )
        }
        
        composable("enhanced_login") {
            EnhancedLoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginSuccess = { userSession ->
                    currentUser.value = userSession
                    // Navigate based on user role
                    when (userSession.role) {
                        UserRole.ADMIN -> {
                            navController.navigate("admin_dashboard") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                        UserRole.BUYER, UserRole.SELLER -> {
                            navController.navigate("user_dashboard") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        
        composable("admin_dashboard") {
            AdminDashboardScreen(
                onBackClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onLogout = {
                    currentUser.value = null
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onManageAuctions = {
                    navController.navigate("auction_management")
                },
                onManageUsers = {
                    navController.navigate("user_management")
                },
                onViewAnalytics = {
                    navController.navigate("analytics")
                }
            )
        }
        
        composable("user_dashboard") {
            val user = currentUser.value
            if (user != null) {
                UserDashboardScreen(
                    username = user.username,
                    onBackClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onLogout = {
                        currentUser.value = null
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onViewAuctions = {
                        navController.navigate("home")
                    },
                    onMyBids = {
                        navController.navigate("my_bids")
                    },
                    onWatchlist = {
                        navController.navigate("favorites")
                    },
                    onProfile = {
                        navController.navigate("profile")
                    }
                )
            }
        }
        
        composable("profile") {
            UserProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        
        composable("favorites") {
            FavoritesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                }
            )
        }
        
        // Admin Management Screens
        composable("auction_management") {
            AuctionManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAuction = {
                    // TODO: Navigate to create auction screen
                },
                onEditAuction = { auctionId ->
                    // TODO: Navigate to edit auction screen
                },
                onDeleteAuction = { auctionId ->
                    // TODO: Handle auction deletion
                }
            )
        }
        
        composable("user_management") {
            UserManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateUser = {
                    // TODO: Navigate to create user screen
                },
                onEditUser = { userId ->
                    // TODO: Navigate to edit user screen
                },
                onToggleUserStatus = { userId ->
                    // TODO: Handle user status toggle
                }
            )
        }
        
        composable("analytics") {
            AnalyticsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // User Screens
        composable("my_bids") {
            MyBidsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onViewAuction = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                }
            )
        }
        
        composable("test") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("TEST SCREEN - NAVIGATION IS WORKING!")
            }
        }
    }
}

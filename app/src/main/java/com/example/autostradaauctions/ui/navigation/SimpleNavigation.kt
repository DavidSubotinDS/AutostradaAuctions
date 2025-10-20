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
import com.example.autostradaauctions.ui.screens.UserDashboardScreen
import com.example.autostradaauctions.ui.screens.MyBidsScreen
import com.example.autostradaauctions.ui.screens.AuctionSubmissionScreen
import com.example.autostradaauctions.ui.screens.AdvancedSearchScreen
import com.example.autostradaauctions.ui.admin.AdminDashboardScreen
import com.example.autostradaauctions.ui.admin.AdminUsersScreen
import com.example.autostradaauctions.ui.admin.AuctionApprovalScreen
import com.example.autostradaauctions.data.model.UserSession
import com.example.autostradaauctions.data.model.UserRole

@Composable
fun SimpleNavigation(
    navController: NavHostController = rememberNavController()
) {
    // 🚨 CRITICAL DEBUG: SimpleNavigation composable started
    println("🚨🚨🚨 SIMPLENAVIGATION COMPOSABLE STARTED")
    android.util.Log.d("AutostradaDebug", "🚨🚨🚨 SIMPLENAVIGATION COMPOSABLE STARTED")
    
    // Current user session state
    val currentUser = remember { mutableStateOf<UserSession?>(null) }
    
    println("🚨 ABOUT TO CREATE NAVHOST")
    android.util.Log.d("AutostradaDebug", "🚨 ABOUT TO CREATE NAVHOST")
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onAuctionClick = { auctionId ->
                    println("🚨 NAVIGATION TRIGGERED: auction ID: $auctionId")
                    try {
                        navController.navigate("auction_detail/$auctionId")
                        println("🚨 NAVIGATION SUCCESSFUL: navigated to auction_detail/$auctionId")
                    } catch (e: Exception) {
                        println("🚨 NAVIGATION FAILED: ${e.message}")
                        e.printStackTrace()
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
                },
                onAnalyticsClick = {
                    navController.navigate("admin_dashboard")
                },
                onSellClick = {
                    navController.navigate("auction_submission")
                },
                onSearchClick = {
                    navController.navigate("search")
                },
                currentUserRole = currentUser.value?.role
            )
        }
        
        composable("auction_detail/{auctionId}") { backStackEntry ->
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
            println("🚨 AUCTION DETAIL SCREEN: Starting with ID: $auctionId")
            println("🚨 CALLING EnhancedAuctionDetailScreen")
            EnhancedAuctionDetailScreen(
                auctionId = auctionId,
                onBackClick = {
                    println("🚨 BACK BUTTON: clicked")
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
                onNavigateToUsers = {
                    navController.navigate("admin_users")
                },
                onNavigateToAuctions = {
                    navController.navigate("admin_auctions")
                },
                onNavigateBack = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
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
        composable("admin_users") {
            AdminUsersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("admin_auctions") {
            AuctionApprovalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("auction_submission") {
            AuctionSubmissionScreen(
                onNavigateBack = {
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
        
        composable("search") { navBackStackEntry ->
            AdvancedSearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAuctionClick = { auctionId ->
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

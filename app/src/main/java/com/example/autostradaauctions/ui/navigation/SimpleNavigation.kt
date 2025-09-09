package com.example.autostradaauctions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autostradaauctions.ui.screens.SimpleHomeScreen
import com.example.autostradaauctions.ui.screens.EnhancedAuctionDetailScreen
import com.example.autostradaauctions.ui.screens.SimpleLoginScreen
import com.example.autostradaauctions.ui.screens.EnhancedLoginScreen
import com.example.autostradaauctions.ui.screens.UserProfileScreen
import com.example.autostradaauctions.ui.screens.FavoritesScreen

@Composable
fun SimpleNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            SimpleHomeScreen(
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                },
                onLoginClick = {
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
            EnhancedAuctionDetailScreen(
                auctionId = auctionId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Legacy login screen for backward compatibility
        composable("login") {
            SimpleLoginScreen(
                onLoginSuccess = {
                    navController.popBackStack("home", inclusive = false)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Enhanced authentication screens
        composable("enhanced_login") {
            EnhancedLoginScreen(
                onLoginSuccess = {
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateToRegister = {
                    // For now, stay on login screen - register can be added later
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            UserProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        composable("favorites") {
            FavoritesScreen(
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

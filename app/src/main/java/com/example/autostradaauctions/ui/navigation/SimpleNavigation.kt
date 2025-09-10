package com.example.autostradaauctions.ui.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun SimpleNavigation(
    navController: NavHostController = rememberNavController()
) {
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
                    println("DEBUG: Login clicked - navigating to test")
                    navController.navigate("test")
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
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
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

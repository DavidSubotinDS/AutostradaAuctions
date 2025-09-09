package com.example.autostradaauctions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autostradaauctions.ui.screens.SimpleHomeScreen
import com.example.autostradaauctions.ui.screens.EnhancedAuctionDetailScreen
import com.example.autostradaauctions.ui.screens.SimpleLoginScreen

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
                    navController.navigate("login")
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
    }
}

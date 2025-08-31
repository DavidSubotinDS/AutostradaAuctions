package com.example.autostradaauctions.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autostradaauctions.ui.screens.auth.AuthViewModel
import com.example.autostradaauctions.ui.screens.auth.LoginScreen
import com.example.autostradaauctions.ui.screens.auth.RegisterScreen
import com.example.autostradaauctions.ui.screens.auction.AuctionDetailScreen
import com.example.autostradaauctions.ui.screens.home.HomeScreen
import com.example.autostradaauctions.ui.screens.profile.ProfileScreen

@Composable
fun AutostradaNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                },
                onLoginClick = {
                    navController.navigate("login")
                },
                isUserLoggedIn = authState.isAuthenticated
            )
        }

        composable("auction_detail/{auctionId}") { backStackEntry ->
            val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
            AuctionDetailScreen(
                auctionId = auctionId,
                userId = authState.currentUser?.id,
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegistrationSuccess = {
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable("profile") {
            ProfileScreen(
                onAuctionClick = { auctionId ->
                    navController.navigate("auction_detail/$auctionId")
                },
                onEditProfile = {
                    // TODO: Navigate to edit profile screen
                },
                onLogout = {
                    authViewModel.logout()
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        composable("favorites") {
            // FavoritesScreen - to be implemented
            // This would show user's favorite auctions
        }

        composable("my_bids") {
            // MyBidsScreen - to be implemented
            // This would show user's bidding history
        }
    }
}

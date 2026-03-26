package com.example.mototap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.ChatRepository
import com.example.mototap.core.repository.JobRepository
import com.example.mototap.features.auth.*
import com.example.mototap.features.driver.*
import com.example.mototap.features.mechanic.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MotoTapNavHost(
    authRepository: AuthRepository,
    jobRepository: JobRepository,
    chatRepository: ChatRepository,
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val mechanicViewModel: MechanicDashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MechanicDashboardViewModel(jobRepository) as T
            }
        }
    )

    val driverViewModel: DriverHomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DriverHomeViewModel(authRepository, jobRepository) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onGetStarted = {
                    navController.navigate(AppRoute.Login.route)
                },
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    when (role) {
                        "customer" -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                        "mechanic" -> {
                            navController.navigate(AppRoute.ProviderDashboard.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(AppRoute.UserSelection.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoute.SignUp.route)
                }
            )
        }

        composable(AppRoute.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { role ->
                    when (role) {
                        "customer" -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                        "mechanic" -> {
                            navController.navigate(AppRoute.ProviderDashboard.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(AppRoute.UserSelection.route) {
                                popUpTo(AppRoute.Splash.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Login.route)
                }
            )
        }
        
        composable(AppRoute.UserSelection.route) {
            UserSelectionScreen(
                onRequestService = {
                    navController.navigate(AppRoute.CustomerDashboard.route)
                },
                onProvideService = {
                    navController.navigate(AppRoute.ProviderDashboard.route)
                }
            )
        }

        // Customer Flow
        composable(AppRoute.CustomerDashboard.route) {
            CustomerDashboardScreen(
                onCategorySelected = { category ->
                    navController.navigate(AppRoute.SubServiceSelection.createRoute(category))
                }
            )
        }

        composable(
            route = AppRoute.SubServiceSelection.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            SubServiceSelectionScreen(
                categoryName = category,
                onSubServiceSelected = { subService ->
                    driverViewModel.onIssueChanged(subService)
                    navController.navigate(AppRoute.RequestService.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(AppRoute.RequestService.route) {
            RequestServiceScreen(
                viewModel = driverViewModel,
                onBack = { navController.popBackStack() },
                onSubmit = {
                    navController.navigate(AppRoute.QuotePayment.route)
                }
            )
        }
        
        composable(AppRoute.QuotePayment.route) {
            QuotePaymentScreen(
                onBack = { navController.popBackStack() },
                onAcceptAndPay = {
                    navController.navigate(AppRoute.JobTracking.route)
                }
            )
        }
        
        composable(AppRoute.JobTracking.route) {
            JobTrackingScreen(
                onBack = { navController.popBackStack() },
                onChat = {
                    navController.navigate(AppRoute.Chat.createRoute("current_job"))
                }
            )
        }

        // Provider Flow
        composable(AppRoute.ProviderDashboard.route) {
            ProviderDashboardScreen(
                viewModel = mechanicViewModel,
                onBack = { navController.popBackStack() },
                onSubmitQuote = {
                    navController.navigate(AppRoute.ProviderJobTracking.route)
                }
            )
        }
        
        composable(AppRoute.ProviderJobTracking.route) {
            val uiState by mechanicViewModel.uiState.collectAsState()
            val currentJob = uiState.openJobs.firstOrNull() 
            
            ProviderJobTrackingScreen(
                job = currentJob,
                onBack = { navController.popBackStack() },
                onChat = {
                    currentJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                }
            )
        }
        
        composable(
            route = AppRoute.Chat.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            val chatViewModel: ChatViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ChatViewModel(chatRepository, jobId, currentUserId) as T
                    }
                }
            )
            
            ChatScreen(
                viewModel = chatViewModel,
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.RatingReview.route) {
            RatingReviewScreen(
                onBack = { navController.popBackStack() },
                onAddReview = {
                    navController.navigate(AppRoute.CustomerDashboard.route) {
                        popUpTo(AppRoute.CustomerDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

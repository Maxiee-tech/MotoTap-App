package com.example.mototap.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
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
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val mechanicViewModel: MechanicDashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MechanicDashboardViewModel(authRepository, jobRepository) as T
            }
        }
    )

    val driverViewModel: DriverHomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DriverHomeViewModel(authRepository, jobRepository) as T
            }
        }
    )

    androidx.compose.runtime.LaunchedEffect(driverViewModel.navigationEvent) {
        driverViewModel.navigationEvent.collect { event ->
            when (event) {
                "job_tracking" -> navController.navigate(AppRoute.JobTracking.route)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onGetStarted = {
                    navController.navigate(AppRoute.UserSelection.route)
                }
            )
        }

        composable(AppRoute.UserSelection.route) {
            UserSelectionScreen(
                onRequestService = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        authViewModel.checkExistingSession { role ->
                            if (role == "mechanic") {
                                navController.navigate(AppRoute.ProviderDashboard.route) {
                                    popUpTo(AppRoute.UserSelection.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(AppRoute.CustomerDashboard.route) {
                                    popUpTo(AppRoute.UserSelection.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(AppRoute.Login.route)
                    }
                },
                onProvideService = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        authViewModel.checkExistingSession { role ->
                            if (role == "mechanic") {
                                navController.navigate(AppRoute.ProviderDashboard.route) {
                                    popUpTo(AppRoute.UserSelection.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(AppRoute.CustomerDashboard.route) {
                                    popUpTo(AppRoute.UserSelection.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(AppRoute.Login.route)
                    }
                }
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    when (role) {
                        "customer", "driver" -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                        "mechanic" -> {
                            navController.navigate(AppRoute.ProviderDashboard.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
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
                        "customer", "driver" -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.SignUp.route) { inclusive = true }
                            }
                        }
                        "mechanic" -> {
                            navController.navigate(AppRoute.ProviderDashboard.route) {
                                popUpTo(AppRoute.SignUp.route) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(AppRoute.CustomerDashboard.route) {
                                popUpTo(AppRoute.SignUp.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Login.route)
                }
            )
        }

        // Customer Flow
        composable(AppRoute.CustomerDashboard.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.fetchUserProfile()
            }

            CustomerDashboardScreen(
                viewModel = driverViewModel,
                userProfile = userProfile,
                onCategorySelected = { category ->
                    navController.navigate(AppRoute.SubServiceSelection.createRoute(category))
                },
                onNavigateToRequests = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToMessages = {
                    navController.navigate(AppRoute.ChatList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
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
                viewModel = driverViewModel,
                onSubServiceSelected = { subService ->
                    navController.navigate(AppRoute.MechanicMap.createRoute(subService))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.MechanicMap.route,
            arguments = listOf(navArgument("service") { type = NavType.StringType })
        ) { backStackEntry ->
            val service = backStackEntry.arguments?.getString("service") ?: ""
            val userProfile by authViewModel.userProfile.collectAsState()
            
            MechanicMapScreen(
                service = service,
                viewModel = driverViewModel,
                isAdmin = userProfile?.role == com.example.mototap.core.model.UserRole.ADMIN,
                onMechanicDetailsClick = { mechanic ->
                    navController.navigate(AppRoute.MechanicDetails.createRoute(mechanic.id))
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = AppRoute.MechanicDetails.route,
            arguments = listOf(navArgument("mechanicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mechanicId = backStackEntry.arguments?.getString("mechanicId") ?: ""
            val userProfile by authViewModel.userProfile.collectAsState()
            val uiState by driverViewModel.uiState.collectAsState()
            val mechanic = uiState.availableMechanics.firstOrNull { it.id == mechanicId }
            
            val reviews by authRepository.observeMechanicReviews(mechanicId).collectAsState(initial = emptyList())

            if (mechanic != null) {
                val context = LocalContext.current
                val isAdmin = userProfile?.role == com.example.mototap.core.model.UserRole.ADMIN
                
                MechanicDetailsPage(
                    mechanic = mechanic,
                    reviews = reviews,
                    isAdmin = isAdmin,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        driverViewModel.openChatWithMechanic(mechanic.id) { jobId ->
                            navController.navigate(AppRoute.Chat.createRoute(jobId))
                        }
                    },
                    onBookService = { serviceName ->
                        driverViewModel.bookMechanic(context, mechanic, serviceName)
                    }
                )
            }
        }

        composable(AppRoute.RequestService.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            
            RequestServiceScreen(
                viewModel = driverViewModel,
                isAdmin = userProfile?.role == com.example.mototap.core.model.UserRole.ADMIN,
                onBack = { navController.popBackStack() }
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
            val userProfile by authViewModel.userProfile.collectAsState()
            val driverUiState by driverViewModel.uiState.collectAsState()
            val activeJob = driverUiState.jobs.firstOrNull { it.mechanicId != null && it.status != com.example.mototap.core.model.JobStatus.COMPLETED }
            
            JobTrackingScreen(
                onBack = { navController.popBackStack() },
                onChat = {
                    activeJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                },
                mechanicPhoneNumber = driverUiState.mechanicPhoneNumber,
                status = activeJob?.status ?: com.example.mototap.core.model.JobStatus.ASSIGNED,
                isAdmin = userProfile?.role == com.example.mototap.core.model.UserRole.ADMIN,
                onJobCompleted = {
                    activeJob?.mechanicId?.let { mechanicId ->
                        navController.navigate(AppRoute.RatingReview.createRoute(mechanicId))
                    } ?: navController.popBackStack()
                }
            )
        }

        // History/Requests screen based on role
        composable(AppRoute.RequestHistory.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.fetchUserProfile()
            }
            
            if (userProfile?.role?.name?.lowercase() == "mechanic") {
                MechanicHistoryScreen(
                    viewModel = mechanicViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                RequestHistoryScreen(
                    viewModel = driverViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoute.ChatList.route) {
            ChatListScreen(
                chatRepository = chatRepository,
                onChatSelected = { jobId ->
                    navController.navigate(AppRoute.Chat.createRoute(jobId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                driverViewModel = driverViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onDeleteSuccess = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToRequests = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onBookNow = { category ->
                    navController.navigate(AppRoute.SubServiceSelection.createRoute(category))
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
                },
                onNavigateToRequests = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToMessages = {
                    navController.navigate(AppRoute.ChatList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                }
            )
        }
        
        composable(AppRoute.ProviderJobTracking.route) {
            val uiState by mechanicViewModel.uiState.collectAsState()
            val currentJob = uiState.ongoingJobs.firstOrNull()
            
            ProviderJobTrackingScreen(
                job = currentJob,
                onBack = { navController.popBackStack() },
                onChat = {
                    currentJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                },
                onComplete = { jobId ->
                    mechanicViewModel.updateStatus(jobId, com.example.mototap.core.model.JobStatus.COMPLETED)
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
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ChatViewModel(authRepository, chatRepository, jobId, currentUserId) as T
                    }
                }
            )
            
            ChatScreen(
                viewModel = chatViewModel,
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.RatingReview.route,
            arguments = listOf(navArgument("mechanicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val mechanicId = backStackEntry.arguments?.getString("mechanicId") ?: ""
            val ratingViewModel: RatingReviewViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return RatingReviewViewModel(authRepository, mechanicId) as T
                    }
                }
            )
            
            RatingReviewScreen(
                viewModel = ratingViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(AppRoute.CustomerDashboard.route) {
                        popUpTo(AppRoute.CustomerDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

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
import com.example.mototap.core.repository.GarageRepository
import com.example.mototap.core.repository.JobRepository
import com.example.mototap.features.auth.*
import com.example.mototap.features.driver.*
import com.example.mototap.features.mechanic.*
import com.example.mototap.features.partsdealer.PartsDealerHomeScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private fun androidx.navigation.NavHostController.navigateAfterAuth(role: String?, popUpRoute: String) {
    when (role?.lowercase()?.trim()) {
        "mechanic" -> navigate(AppRoute.ProviderDashboard.route) {
            popUpTo(popUpRoute) { inclusive = true }
        }
        "parts_dealer", "parts dealer" -> navigate(AppRoute.PartsDealerHome.route) {
            popUpTo(popUpRoute) { inclusive = true }
        }
        else -> navigate(AppRoute.CustomerDashboard.route) {
            popUpTo(popUpRoute) { inclusive = true }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateAfterSession(
    role: String?,
    needsSignupResume: Boolean,
    popUpRoute: String,
) {
    if (needsSignupResume) {
        navigate(AppRoute.SignUp.route) {
            popUpTo(popUpRoute) { inclusive = true }
        }
    } else {
        navigateAfterAuth(role, popUpRoute)
    }
}

@Composable
fun MotoTapNavHost(
    authRepository: AuthRepository,
    jobRepository: JobRepository,
    chatRepository: ChatRepository,
    garageRepository: GarageRepository,
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository, garageRepository) as T
            }
        }
    )

    val mechanicViewModel: MechanicDashboardViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MechanicDashboardViewModel(authRepository, jobRepository, garageRepository) as T
            }
        }
    )

    val driverViewModel: DriverHomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DriverHomeViewModel(authRepository, jobRepository, chatRepository) as T
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
                        authViewModel.checkExistingSession { role, needsSignupResume ->
                            navController.navigateAfterSession(
                                role = role,
                                needsSignupResume = needsSignupResume,
                                popUpRoute = AppRoute.UserSelection.route,
                            )
                        }
                    } else {
                        navController.navigate(AppRoute.Login.route)
                    }
                },
                onProvideService = {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        authViewModel.checkExistingSession { role, needsSignupResume ->
                            navController.navigateAfterSession(
                                role = role,
                                needsSignupResume = needsSignupResume,
                                popUpRoute = AppRoute.UserSelection.route,
                            )
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
                    navController.navigateAfterAuth(role, AppRoute.Login.route)
                },
                onNavigateToSignUp = {
                    navController.navigate(AppRoute.SignUp.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AppRoute.ForgotPassword.route)
                }
            )
        }

        composable(AppRoute.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(AppRoute.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { role ->
                    navController.navigateAfterAuth(role, AppRoute.SignUp.route)
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
                    driverViewModel.setMarketplaceMode(DriverMarketplaceMode.MECHANICS)
                    navController.navigate(AppRoute.SubServiceSelection.createRoute(category))
                },
                onPartsCategorySelected = { category ->
                    driverViewModel.setMarketplaceMode(DriverMarketplaceMode.PARTS_DEALERS)
                    navController.navigate(AppRoute.PartsCategorySelection.createRoute(category))
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
                    navController.navigate(AppRoute.MechanicDetails.createRoute(mechanic.id, service))
                },
                onBack = {
                    driverViewModel.setMarketplaceMode(DriverMarketplaceMode.MECHANICS)
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = AppRoute.MechanicDetails.route,
            arguments = listOf(
                navArgument("mechanicId") { type = NavType.StringType },
                navArgument("service") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
        ) { backStackEntry ->
            val mechanicId = backStackEntry.arguments?.getString("mechanicId") ?: ""
            val selectedService = backStackEntry.arguments?.getString("service").orEmpty()
            val userProfile by authViewModel.userProfile.collectAsState()
            val uiState by driverViewModel.uiState.collectAsState()
            val mechanic = uiState.availableMechanics.firstOrNull { it.id == mechanicId }
            
            val reviews by authRepository.observeMechanicReviews(mechanicId).collectAsState(initial = emptyList())

            if (mechanic != null) {
                val context = LocalContext.current
                val isAdmin = userProfile?.role == com.example.mototap.core.model.UserRole.ADMIN
                
                MechanicDetailsPage(
                    mechanic = mechanic,
                    selectedService = selectedService,
                    reviews = reviews,
                    isAdmin = isAdmin,
                    vehicleMake = uiState.activeVehicleMake,
                    vehicleModel = uiState.activeVehicleModel,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        driverViewModel.openChatWithMechanic(mechanic.id) { jobId ->
                            navController.navigate(AppRoute.Chat.createRoute(jobId))
                        }
                    },
                    onBookService = { serviceName, estimatedKm ->
                        driverViewModel.bookMechanic(context, mechanic, serviceName, estimatedKm)
                    }
                )
            }
        }

        // Spare Parts Dealers Flow
        composable(
            route = AppRoute.PartsCategorySelection.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            PartsCategorySelectionScreen(
                categoryName = category,
                onPartSelected = { part ->
                    navController.navigate(AppRoute.PartsDealerMap.createRoute(part))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.PartsDealerMap.route,
            arguments = listOf(navArgument("part") { type = NavType.StringType })
        ) { backStackEntry ->
            val part = backStackEntry.arguments?.getString("part") ?: ""
            PartsDealerMapScreen(
                part = part,
                viewModel = driverViewModel,
                onDealerDetailsClick = { dealer ->
                    navController.navigate(AppRoute.PartsDealerDetails.createRoute(dealer.id))
                },
                onBack = {
                    driverViewModel.setMarketplaceMode(DriverMarketplaceMode.PARTS_DEALERS)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = AppRoute.PartsDealerDetails.route,
            arguments = listOf(navArgument("dealerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dealerId = backStackEntry.arguments?.getString("dealerId") ?: ""
            val driverState by driverViewModel.uiState.collectAsState()
            val dealer = driverState.availablePartsDealers.firstOrNull { it.id == dealerId }

            if (dealer != null) {
                PartsDealerDetailsPage(
                    dealer = dealer,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        driverViewModel.openChatWithProvider(dealer.id) { jobId ->
                            navController.navigate(AppRoute.Chat.createRoute(jobId))
                        }
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
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                MechanicHistoryScreen(
                    viewModel = mechanicViewModel,
                    onBack = { navController.popBackStack() },
                    onMessageDriver = { driverId, driverName ->
                        val me = FirebaseAuth.getInstance().currentUser?.uid
                        if (!me.isNullOrBlank()) {
                            scope.launch {
                                chatRepository.ensureConversation(
                                    me,
                                    driverId,
                                    mapOf(driverId to driverName),
                                )
                                val roomId = com.example.mototap.core.util.ChatIds.roomId(me, driverId)
                                navController.navigate(AppRoute.Chat.createRoute(roomId))
                            }
                        }
                    },
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
        
        composable(AppRoute.PartsDealerHome.route) {
            val userProfile by authViewModel.userProfile.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.fetchUserProfile()
            }

            PartsDealerHomeScreen(
                profile = userProfile,
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                },
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

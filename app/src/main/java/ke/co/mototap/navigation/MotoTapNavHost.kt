package ke.co.mototap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ke.co.mototap.core.repository.AuthRepository
import ke.co.mototap.core.repository.ChatRepository
import ke.co.mototap.core.repository.GarageRepository
import ke.co.mototap.core.repository.JobRepository
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.notify.AppNotification
import ke.co.mototap.core.notify.AppNotificationCenter
import ke.co.mototap.core.notify.AppNotificationKind
import ke.co.mototap.core.notify.NotificationsScreen
import ke.co.mototap.features.auth.*
import ke.co.mototap.features.driver.*
import ke.co.mototap.features.mechanic.*
import ke.co.mototap.features.partsdealer.PartsDealerHomeScreen
import com.google.firebase.auth.FirebaseAuth
import ke.co.mototap.core.model.UserRole
import ke.co.mototap.core.ui.LegalUrls
import ke.co.mototap.ui.FloatingAppMenu
import kotlinx.coroutines.launch

private fun androidx.navigation.NavHostController.navigateProviderActiveJob(job: JobRequest?) {
    val jobId = job?.id.orEmpty().ifBlank { "active" }
    val route = when (job?.status) {
        JobStatus.COMPLETED, JobStatus.PAID, JobStatus.CLOSED ->
            AppRoute.ProviderJobCompleted.createRoute(jobId)
        JobStatus.IN_PROGRESS ->
            AppRoute.ProviderJobInProgress.createRoute(jobId)
        else ->
            AppRoute.ProviderJobTracking.createRoute(jobId)
    }
    navigate(route)
}

private fun ke.co.mototap.features.mechanic.MechanicUiState.findJob(jobId: String): JobRequest? {
    return ongoingJobs.firstOrNull { it.id == jobId }
        ?: garageJobs.firstOrNull { it.id == jobId }
        ?: openJobs.firstOrNull { it.id == jobId }
        ?: completedJobs.firstOrNull { it.id == jobId }
        ?: ongoingJobs.firstOrNull()
        ?: completedJobs.firstOrNull()
}

private fun androidx.navigation.NavHostController.homeRouteFor(role: UserRole?, loggedIn: Boolean): String {
    if (!loggedIn) return AppRoute.CustomerDashboard.route
    return when (role) {
        UserRole.MECHANIC -> AppRoute.ProviderDashboard.route
        UserRole.PARTS_DEALER -> AppRoute.PartsDealerHome.route
        else -> AppRoute.CustomerDashboard.route
    }
}

private fun androidx.navigation.NavHostController.goHome(role: UserRole?, loggedIn: Boolean) {
    val dest = homeRouteFor(role, loggedIn)
    if (currentDestination?.route == dest) return
    val popped = popBackStack(dest, inclusive = false)
    if (!popped) {
        navigate(dest) { launchSingleTop = true }
    }
}

private fun androidx.navigation.NavHostController.goAuthed(loggedIn: Boolean, destination: String) {
    if (loggedIn) {
        navigate(destination) { launchSingleTop = true }
    } else {
        navigate(AppRoute.Login.route) { launchSingleTop = true }
    }
}

private fun androidx.navigation.NavHostController.openAppNotification(
    notification: AppNotification,
    role: UserRole?,
    driverViewModel: ke.co.mototap.features.driver.DriverHomeViewModel,
    mechanicViewModel: ke.co.mototap.features.mechanic.MechanicDashboardViewModel,
) {
    AppNotificationCenter.markRead(notification.id)
    when (notification.kind) {
        AppNotificationKind.MESSAGE -> navigate(AppRoute.Chat.createRoute(notification.targetId)) {
            popUpTo(AppRoute.Notifications.route) { inclusive = true }
            launchSingleTop = true
        }
        AppNotificationKind.INCOMING_REQUEST -> navigate(
            AppRoute.IncomingRequest.createRoute(notification.targetId),
        ) {
            popUpTo(AppRoute.Notifications.route) { inclusive = true }
            launchSingleTop = true
        }
        AppNotificationKind.JOB -> {
            val statusName = notification.extra.substringBefore("|")
            val mechanicId = notification.extra.substringAfter("|", "")
            if (role == UserRole.MECHANIC || role == UserRole.PARTS_DEALER) {
                val job = mechanicViewModel.uiState.value.findJob(notification.targetId)
                popBackStack(AppRoute.Notifications.route, inclusive = true)
                navigateProviderActiveJob(job)
            } else {
                val dest = if (
                    (statusName == JobStatus.COMPLETED.name || statusName == JobStatus.PAID.name) &&
                    mechanicId.isNotBlank()
                ) {
                    AppRoute.RatingReview.createRoute(mechanicId)
                } else {
                    driverViewModel.focusJob(notification.targetId)
                    AppRoute.JobTracking.route
                }
                navigate(dest) {
                    popUpTo(AppRoute.Notifications.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}

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
                "job_tracking" -> navController.navigate(AppRoute.JobTracking.route) {
                    popUpTo(AppRoute.CustomerDashboard.route) { inclusive = false }
                }
            }
        }
    }

    val userProfile by authViewModel.userProfile.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showFloatingMenu = currentRoute != null &&
        currentRoute != AppRoute.Splash.route &&
        currentRoute != AppRoute.UserSelection.route
    val uriHandler = LocalUriHandler.current
    val currentUserId by authRepository.currentUserId.collectAsState(initial = FirebaseAuth.getInstance().currentUser?.uid)
    val loggedIn = !currentUserId.isNullOrBlank()

    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(
                onFindMechanic = {
                    navController.navigate(AppRoute.CustomerDashboard.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onListBusiness = {
                    navController.navigate(AppRoute.UserSelection.route)
                },
            )
        }

        composable(AppRoute.UserSelection.route) {
            fun continueWithRole(selectedRole: String) {
                authViewModel.role.value = selectedRole
                if (selectedRole != "mechanic") {
                    authViewModel.garageMode.value = "own"
                    authViewModel.clearVerifiedInvite()
                }
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
                    navController.navigate(AppRoute.SignUp.route)
                }
            }
            UserSelectionScreen(
                onRequestService = {
                    navController.navigate(AppRoute.CustomerDashboard.route) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                },
                onProvideService = { continueWithRole("mechanic") },
                onProvideParts = { continueWithRole("parts_dealer") },
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
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(AppRoute.UserSelection.route)
                    }
                },
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
                },
                onNavigateToNotifications = {
                    navController.navigate(AppRoute.Notifications.route)
                },
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
                isAdmin = userProfile?.role == ke.co.mototap.core.model.UserRole.ADMIN,
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
                val isAdmin = userProfile?.role == ke.co.mototap.core.model.UserRole.ADMIN
                
                MechanicDetailsPage(
                    mechanic = mechanic,
                    selectedService = selectedService,
                    reviews = reviews,
                    isAdmin = isAdmin,
                    vehicleMake = uiState.activeVehicleMake,
                    vehicleModel = uiState.activeVehicleModel,
                    userLocation = uiState.userLocation,
                    onBack = { navController.popBackStack() },
                    onChat = {
                        driverViewModel.openChatWithMechanic(mechanic.id) { jobId ->
                            navController.navigate(AppRoute.Chat.createRoute(jobId))
                        }
                    },
                    onBookService = { serviceName, estimatedKm ->
                        driverViewModel.bookMechanic(context, mechanic, serviceName, estimatedKm)
                    },
                    onRequestService = { serviceName ->
                        driverViewModel.setSelectedMechanic(mechanic)
                        driverViewModel.onIssueChanged(serviceName)
                        navController.navigate(AppRoute.RequestService.route)
                    },
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
            val context = LocalContext.current
            RequestServiceScreen(
                viewModel = driverViewModel,
                isAdmin = userProfile?.role == ke.co.mototap.core.model.UserRole.ADMIN,
                onBack = { navController.popBackStack() },
                onSendRequest = { estimatedKm ->
                    val mechanic = driverViewModel.uiState.value.selectedMechanic ?: return@RequestServiceScreen
                    val service = driverViewModel.uiState.value.issueTypeInput.ifBlank { "Service request" }
                    driverViewModel.bookMechanic(context, mechanic, service, estimatedKm)
                    navController.navigate(AppRoute.JobTracking.route) {
                        popUpTo(AppRoute.RequestService.route) { inclusive = true }
                    }
                },
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
            val focusedId = driverUiState.focusedJobId
            val activeJob = driverUiState.jobs.firstOrNull { it.id == focusedId }
                ?: driverUiState.jobs.firstOrNull { it.mechanicId != null && it.status != ke.co.mototap.core.model.JobStatus.COMPLETED }
            val bookedMechanic = driverUiState.selectedMechanic
                ?: driverUiState.availableMechanics.firstOrNull { it.id == activeJob?.mechanicId }

            JobTrackingScreen(
                onBack = {
                    navController.popBackStack(AppRoute.CustomerDashboard.route, inclusive = false)
                },
                onChat = {
                    activeJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                },
                mechanicPhoneNumber = bookedMechanic?.phone?.ifBlank { null }
                    ?: driverUiState.mechanicPhoneNumber,
                status = activeJob?.status ?: ke.co.mototap.core.model.JobStatus.ASSIGNED,
                isAdmin = userProfile?.role == ke.co.mototap.core.model.UserRole.ADMIN,
                garageName = bookedMechanic?.name.orEmpty(),
                locationLabel = activeJob?.locationLabel ?: driverUiState.locationInput,
                serviceName = activeJob?.issueType?.takeIf { it.isNotBlank() }
                    ?: activeJob?.serviceName?.takeIf { it.isNotBlank() }
                    ?: driverUiState.issueTypeInput,
                price = activeJob?.price ?: 0L,
                onCancel = {
                    activeJob?.id?.let { driverViewModel.deleteRequest(it) }
                    navController.popBackStack(AppRoute.CustomerDashboard.route, inclusive = false)
                },
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
            
            if (userProfile?.role == UserRole.MECHANIC || userProfile?.role == UserRole.PARTS_DEALER) {
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
                                val roomId = ke.co.mototap.core.util.ChatIds.roomId(me, driverId)
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

        composable(AppRoute.Notifications.route) {
            val profile by authViewModel.userProfile.collectAsState()
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpen = { item ->
                    navController.openAppNotification(
                        notification = item,
                        role = profile?.role,
                        driverViewModel = driverViewModel,
                        mechanicViewModel = mechanicViewModel,
                    )
                },
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
                onNavigateToMessages = {
                    navController.navigate(AppRoute.ChatList.route)
                },
                onBookNow = { category ->
                    navController.navigate(AppRoute.SubServiceSelection.createRoute(category))
                }
            )
        }

        // Provider Flow
        composable(AppRoute.ProviderDashboard.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.fetchUserProfile()
            }
            ProviderDashboardScreen(
                viewModel = mechanicViewModel,
                userProfile = userProfile,
                onBack = { navController.popBackStack() },
                onSubmitQuote = {
                    navController.navigateProviderActiveJob(
                        mechanicViewModel.uiState.value.ongoingJobs.firstOrNull()
                    )
                },
                onNavigateToRequests = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToEarnings = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToMessages = {
                    navController.navigate(AppRoute.ChatList.route)
                },
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                },
                onIncomingRequest = { jobId ->
                    navController.navigate(AppRoute.IncomingRequest.createRoute(jobId))
                },
                onNavigateToNotifications = {
                    navController.navigate(AppRoute.Notifications.route)
                },
            )
        }
        
        composable(AppRoute.PartsDealerHome.route) {
            val userProfile by authViewModel.userProfile.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.fetchUserProfile()
            }

            PartsDealerHomeScreen(
                profile = userProfile,
                viewModel = mechanicViewModel,
                onNavigateToProfile = {
                    navController.navigate(AppRoute.Profile.route)
                },
                onNavigateToRequests = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToEarnings = {
                    navController.navigate(AppRoute.RequestHistory.route)
                },
                onNavigateToMessages = {
                    navController.navigate(AppRoute.ChatList.route)
                },
                onIncomingRequest = { jobId ->
                    navController.navigate(AppRoute.IncomingRequest.createRoute(jobId))
                },
                onOpenJob = {
                    navController.navigateProviderActiveJob(
                        mechanicViewModel.uiState.value.ongoingJobs.firstOrNull()
                    )
                },
                onNavigateToNotifications = {
                    navController.navigate(AppRoute.Notifications.route)
                },
            )
        }

        composable(
            route = AppRoute.IncomingRequest.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            val uiState by mechanicViewModel.uiState.collectAsState()
            val job = uiState.openJobs.firstOrNull { it.id == jobId }
                ?: uiState.garageJobs.firstOrNull { it.id == jobId }
                ?: uiState.ongoingJobs.firstOrNull { it.id == jobId }
            IncomingRequestScreen(
                job = job,
                authRepository = authRepository,
                onViewRequest = {
                    navController.navigate(AppRoute.RequestDetails.createRoute(jobId))
                },
                onDecline = { navController.popBackStack() },
                onAlerts = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = AppRoute.RequestDetails.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            val uiState by mechanicViewModel.uiState.collectAsState()
            val job = uiState.openJobs.firstOrNull { it.id == jobId }
                ?: uiState.garageJobs.firstOrNull { it.id == jobId }
                ?: uiState.ongoingJobs.firstOrNull { it.id == jobId }
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            RequestDetailsScreen(
                job = job,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onAccept = {
                    if (job != null && currentUserId.isNotBlank()) {
                        mechanicViewModel.acceptJob(job.id, currentUserId)
                    }
                    navController.navigate(AppRoute.ProviderJobTracking.createRoute(jobId)) {
                        popUpTo(AppRoute.IncomingRequest.createRoute(jobId)) { inclusive = true }
                    }
                },
                onDecline = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoute.ProviderJobTracking.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            val uiState by mechanicViewModel.uiState.collectAsState()
            val currentJob = uiState.findJob(jobId)
            ProviderJobTrackingScreen(
                job = currentJob,
                authRepository = authRepository,
                providerLat = uiState.latitude,
                providerLng = uiState.longitude,
                onBack = { navController.popBackStack() },
                onChat = {
                    currentJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                },
                onStart = { id ->
                    if (currentJob?.status != JobStatus.IN_PROGRESS &&
                        currentJob?.status != JobStatus.COMPLETED &&
                        currentJob?.status != JobStatus.PAID &&
                        currentJob?.status != JobStatus.CLOSED
                    ) {
                        mechanicViewModel.updateStatus(id, JobStatus.IN_PROGRESS)
                    }
                    navController.navigate(AppRoute.ProviderJobInProgress.createRoute(id))
                },
                onComplete = { id ->
                    mechanicViewModel.updateStatus(id, JobStatus.COMPLETED)
                },
            )
        }

        composable(
            route = AppRoute.ProviderJobInProgress.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            val uiState by mechanicViewModel.uiState.collectAsState()
            val currentJob = uiState.findJob(jobId)
            ProviderJobInProgressScreen(
                job = currentJob,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onChat = {
                    currentJob?.let {
                        navController.navigate(AppRoute.Chat.createRoute(it.id))
                    } ?: navController.navigate(AppRoute.Chat.createRoute("current_job"))
                },
                onComplete = { id ->
                    mechanicViewModel.updateStatus(id, JobStatus.COMPLETED)
                    navController.navigate(AppRoute.ProviderJobCompleted.createRoute(id)) {
                        popUpTo(AppRoute.ProviderJobInProgress.route) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = AppRoute.ProviderJobCompleted.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId").orEmpty()
            val uiState by mechanicViewModel.uiState.collectAsState()
            val currentJob = uiState.findJob(jobId)
            val userProfile by authViewModel.userProfile.collectAsState()
            ProviderJobCompletedScreen(
                job = currentJob,
                authRepository = authRepository,
                onBack = { navController.popBackStack() },
                onDone = {
                    val home = if (userProfile?.role == UserRole.PARTS_DEALER) {
                        AppRoute.PartsDealerHome.route
                    } else {
                        AppRoute.ProviderDashboard.route
                    }
                    val popped = navController.popBackStack(home, inclusive = false)
                    if (!popped) {
                        navController.navigate(home) { launchSingleTop = true }
                    }
                },
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

        if (showFloatingMenu) {
            FloatingAppMenu(
                modifier = Modifier.fillMaxSize(),
                onHome = { navController.goHome(userProfile?.role, loggedIn) },
                onHistory = { navController.goAuthed(loggedIn, AppRoute.RequestHistory.route) },
                onProfile = { navController.goAuthed(loggedIn, AppRoute.Profile.route) },
                onMessages = { navController.goAuthed(loggedIn, AppRoute.ChatList.route) },
                onPrivacy = { uriHandler.openUri(LegalUrls.PRIVACY) },
                onTerms = { uriHandler.openUri(LegalUrls.TERMS) },
                onLogout = {
                    if (!loggedIn) {
                        navController.navigate(AppRoute.Login.route) { launchSingleTop = true }
                    } else {
                        authViewModel.logout {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                },
                onDelete = { password ->
                    if (!loggedIn) {
                        navController.navigate(AppRoute.Login.route) { launchSingleTop = true }
                    } else {
                        authViewModel.deleteAccount(password) {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }
    }
}

package com.example.mototap.navigation

sealed class AppRoute(
    val route: String,
) {
    data object Splash : AppRoute("splash")
    data object UserSelection : AppRoute("user_selection")
    data object Login : AppRoute("login")
    data object SignUp : AppRoute("signup")
    
    // Customer Flow
    data object CustomerDashboard : AppRoute("customer_dashboard")
    data object SubServiceSelection : AppRoute("sub_service_selection/{category}") {
        fun createRoute(category: String) = "sub_service_selection/$category"
    }
    data object RequestService : AppRoute("request_service")
    data object QuotePayment : AppRoute("quote_payment")
    data object JobTracking : AppRoute("job_tracking")
    data object RequestHistory : AppRoute("request_history")
    data object Profile : AppRoute("profile")
    
    // Provider Flow
    data object ProviderDashboard : AppRoute("provider_dashboard")
    data object ProviderJobTracking : AppRoute("provider_job_tracking")
    data object RatingReview : AppRoute("rating_review")

    // Shared Flow
    data object Chat : AppRoute("chat/{jobId}") {
        fun createRoute(jobId: String) = "chat/$jobId"
    }
    data object ChatList : AppRoute("chat_list")

    // Legacy/Other
    data object Driver : AppRoute("driver")
    data object Mechanic : AppRoute("mechanic")
    data object Overview : AppRoute("overview")
}

package com.example.mototap.navigation

import android.net.Uri

sealed class AppRoute(
    val route: String,
) {
    data object Splash : AppRoute("splash")
    data object UserSelection : AppRoute("user_selection")
    data object Login : AppRoute("login")
    data object ForgotPassword : AppRoute("forgot_password")
    data object SignUp : AppRoute("signup")
    
    // Customer Flow
    data object CustomerDashboard : AppRoute("customer_dashboard")
    data object SubServiceSelection : AppRoute("sub_service_selection/{category}") {
        fun createRoute(category: String) = "sub_service_selection/${Uri.encode(category)}"
    }
    data object RequestService : AppRoute("request_service")
    data object QuotePayment : AppRoute("quote_payment")
    data object JobTracking : AppRoute("job_tracking")
    data object RequestHistory : AppRoute("request_history")
    data object Profile : AppRoute("profile")
    data object MechanicMap : AppRoute("mechanic_map/{service}") {
        fun createRoute(service: String) = "mechanic_map/${Uri.encode(service)}"
    }
    data object MechanicDetails : AppRoute("mechanic_details/{mechanicId}?service={service}") {
        fun createRoute(mechanicId: String, service: String = "") =
            if (service.isBlank()) {
                "mechanic_details/${Uri.encode(mechanicId)}"
            } else {
                "mechanic_details/${Uri.encode(mechanicId)}?service=${Uri.encode(service)}"
            }
    }

    // Spare Parts Dealers Flow
    data object PartsCategorySelection : AppRoute("parts_category/{category}") {
        fun createRoute(category: String) = "parts_category/${Uri.encode(category)}"
    }
    data object PartsDealerMap : AppRoute("parts_dealer_map/{part}") {
        fun createRoute(part: String) = "parts_dealer_map/${Uri.encode(part)}"
    }
    data object PartsDealerDetails : AppRoute("parts_dealer_details/{dealerId}") {
        fun createRoute(dealerId: String) = "parts_dealer_details/${Uri.encode(dealerId)}"
    }

    // Provider Flow
    data object ProviderDashboard : AppRoute("provider_dashboard")
    data object PartsDealerHome : AppRoute("parts_dealer_home")
    data object ProviderJobTracking : AppRoute("provider_job_tracking")
    data object RatingReview : AppRoute("rating_review/{mechanicId}") {
        fun createRoute(mechanicId: String) = "rating_review/${Uri.encode(mechanicId)}"
    }

    // Shared Flow
    data object Chat : AppRoute("chat/{jobId}") {
        fun createRoute(jobId: String) = "chat/${Uri.encode(jobId)}"
    }
    data object ChatList : AppRoute("chat_list")

    // Legacy/Other
    data object Driver : AppRoute("driver")
    data object Mechanic : AppRoute("mechanic")
    data object Overview : AppRoute("overview")
}

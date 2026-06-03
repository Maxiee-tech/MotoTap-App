package com.example.mototap.features.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.Review
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingReviewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RatingReviewViewModel(
    private val authRepository: AuthRepository,
    private val mechanicId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingReviewUiState())
    val uiState: StateFlow<RatingReviewUiState> = _uiState.asStateFlow()

    fun submitReview(rating: Double, comment: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Fetch driver name for the review
            val driverProfile = authRepository.getUserProfile(currentUserId)
            val driverName = driverProfile?.name ?: "MotoTap User"
            
            val review = Review(
                mechanicId = mechanicId,
                driverId = currentUserId,
                driverName = driverName,
                rating = rating,
                comment = comment,
                timestampMillis = System.currentTimeMillis()
            )
            
            val result = authRepository.addReview(review)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = result.exceptionOrNull()?.message ?: "Failed to submit review"
                )
            }
        }
    }
}

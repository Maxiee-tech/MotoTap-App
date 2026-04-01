package com.example.mototap.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: String?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val name = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val role = MutableStateFlow("customer") // default to customer

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun signIn() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signIn started for ${email.value}")
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signIn(email.value, password.value)
            if (result.isSuccess) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("AuthViewModel", "signIn success, fetching role for $userId with timeout")
                
                // Increased timeout to 15s to be more robust on slower networks
                val userRole = withTimeoutOrNull(15000) {
                    userId?.let { authRepository.getUserRole(it) }
                }
                
                Log.d("AuthViewModel", "role result: $userRole")
                _uiState.value = AuthUiState.Success(userRole)
            } else {
                val rawError = result.exceptionOrNull()?.message ?: ""
                val errorMsg = if (rawError.contains("credential", ignoreCase = true) || 
                                 rawError.contains("password", ignoreCase = true) ||
                                 rawError.contains("user", ignoreCase = true)) {
                    "Wrong Email or Password."
                } else {
                    "Login failed. Please check your connection."
                }
                Log.e("AuthViewModel", "signIn failed: $rawError")
                _uiState.value = AuthUiState.Error(errorMsg)
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signUp started for ${email.value}")
            _uiState.value = AuthUiState.Loading
            
            try {
                // Increased timeout to 30 seconds to accommodate double Firebase operations (Auth + Firestore)
                val result = withTimeoutOrNull(30000) {
                    authRepository.signUp(
                        email.value, 
                        password.value, 
                        name.value, 
                        role.value,
                        phoneNumber.value // Now always passing phoneNumber for both roles
                    )
                }

                if (result == null) {
                    val errorMsg = "Sign up timed out. Please check your connection and try again."
                    Log.e("AuthViewModel", errorMsg)
                    _uiState.value = AuthUiState.Error(errorMsg)
                } else if (result.isSuccess) {
                    Log.d("AuthViewModel", "signUp success, navigating with role ${role.value}")
                    _uiState.value = AuthUiState.Success(role.value)
                } else {
                    val rawError = result.exceptionOrNull()?.message ?: ""
                    val errorMsg = when {
                        rawError.contains("email", ignoreCase = true) && rawError.contains("already", ignoreCase = true) -> 
                            "This email is already registered."
                        rawError.contains("network", ignoreCase = true) -> 
                            "Network error. Please check your connection."
                        else -> "Sign up failed: $rawError"
                    }
                    Log.e("AuthViewModel", "signUp failed: $rawError")
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signUp exception: ${e.message}")
                _uiState.value = AuthUiState.Error("An unexpected error occurred during sign up.")
            }
        }
    }

    fun checkExistingSession(onRoleFetched: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                Log.d("AuthViewModel", "Existing session found for $userId, fetching role...")
                // Increased timeout to 15s
                val userRole = withTimeoutOrNull(15000) {
                    authRepository.getUserRole(userId)
                }
                Log.d("AuthViewModel", "Session role: $userRole")
                onRoleFetched(userRole)
            }
        }
    }
    
    fun fetchUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                // Increased timeout to 15s
                val profile = withTimeoutOrNull(15000) {
                    authRepository.getUserProfile(userId)
                }
                _userProfile.value = profile
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}")
            }
            _userProfile.value = null
            _uiState.value = AuthUiState.Idle
            onSuccess()
        }
    }

    fun deleteAccount(currentPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (currentPassword.isBlank()) {
                _uiState.value = AuthUiState.Error("Enter your current password to delete your account.")
                return@launch
            }

            Log.d("AuthViewModel", "deleteAccount started")
            _uiState.value = AuthUiState.Loading
            
            val result = withTimeoutOrNull(20000) {
                authRepository.deleteAccount(currentPassword)
            }

            if (result != null && result.isSuccess) {
                Log.d("AuthViewModel", "deleteAccount success")
                _userProfile.value = null
                _uiState.value = AuthUiState.Idle
                onSuccess()
            } else {
                val errorMsg = result?.exceptionOrNull()?.message ?: "Account deletion timed out. Please try again."
                Log.e("AuthViewModel", "deleteAccount failed: $errorMsg")
                _uiState.value = AuthUiState.Error(errorMsg)
            }
        }
    }

    fun resetState() {
        Log.d("AuthViewModel", "resetState called")
        _uiState.value = AuthUiState.Idle
    }
}

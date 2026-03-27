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
                
                val userRole = withTimeoutOrNull(5000) {
                    userId?.let { authRepository.getUserRole(it) }
                }
                
                Log.d("AuthViewModel", "role result: $userRole")
                _uiState.value = AuthUiState.Success(userRole)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Login failed"
                Log.e("AuthViewModel", "signIn failed: $errorMsg")
                _uiState.value = AuthUiState.Error(errorMsg)
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signUp started for ${email.value}")
            _uiState.value = AuthUiState.Loading
            
            // Add timeout to the entire signUp process
            val success = withTimeoutOrNull(10000) {
                val result = authRepository.signUp(
                    email.value, 
                    password.value, 
                    name.value, 
                    role.value,
                    if (role.value == "mechanic") phoneNumber.value else null
                )
                result.isSuccess
            } ?: false

            if (success) {
                Log.d("AuthViewModel", "signUp success, navigating with role ${role.value}")
                _uiState.value = AuthUiState.Success(role.value)
            } else {
                val errorMsg = "Sign up failed or timed out. Please check your connection."
                Log.e("AuthViewModel", errorMsg)
                _uiState.value = AuthUiState.Error(errorMsg)
            }
        }
    }

    fun checkExistingSession(onRoleFetched: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                Log.d("AuthViewModel", "Existing session found for $userId, fetching role...")
                val userRole = withTimeoutOrNull(5000) {
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
                val profile = authRepository.getUserProfile(userId)
                _userProfile.value = profile
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            _userProfile.value = null
            onSuccess()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                _userProfile.value = null
                _uiState.value = AuthUiState.Idle
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete account")
            }
        }
    }

    fun resetState() {
        Log.d("AuthViewModel", "resetState called")
        _uiState.value = AuthUiState.Idle
    }
}

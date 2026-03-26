package com.example.mototap.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val role = MutableStateFlow("customer") // default to customer

    fun signIn() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "signIn started for ${email.value}")
            _uiState.value = AuthUiState.Loading
            
            val result = authRepository.signIn(email.value, password.value)
            if (result.isSuccess) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                Log.d("AuthViewModel", "signIn success, fetching role for $userId with timeout")
                
                // Fetch role with a 5-second timeout to prevent hanging
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
            
            val result = authRepository.signUp(email.value, password.value, name.value, role.value)
            if (result.isSuccess) {
                Log.d("AuthViewModel", "signUp success, navigating with role ${role.value}")
                _uiState.value = AuthUiState.Success(role.value)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Sign up failed"
                Log.e("AuthViewModel", "signUp failed: $errorMsg")
                _uiState.value = AuthUiState.Error(errorMsg)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

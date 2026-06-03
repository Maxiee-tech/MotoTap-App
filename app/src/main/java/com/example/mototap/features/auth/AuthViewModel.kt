package com.example.mototap.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.model.UserRole
import com.example.mototap.core.model.VerificationStatus
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
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

enum class SignUpStep {
    BASIC_INFO,
    IDENTITY_VERIFICATION,
    ADDITIONAL_INFO
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _signUpStep = MutableStateFlow(SignUpStep.BASIC_INFO)
    val signUpStep: StateFlow<SignUpStep> = _signUpStep.asStateFlow()

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val name = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val role = MutableStateFlow("driver") // default to driver

    // Step 2 & 3 Fields
    val profilePhotoUrl = MutableStateFlow("")
    val idNumber = MutableStateFlow("")
    val idPhotoUrl = MutableStateFlow("")
    
    // Driver specific
    val vehicleType = MutableStateFlow("")
    val vehicleModel = MutableStateFlow("")
    val numberPlate = MutableStateFlow("")
    val vehiclePhotoUrl = MutableStateFlow("")

     // Mechanic specific
     val certificateNumber = MutableStateFlow("")
     val certificatePhotoUrl = MutableStateFlow("")
     val institutionName = MutableStateFlow("")
     val experienceYears = MutableStateFlow("")
     val latitude = MutableStateFlow<Double?>(null)
     val longitude = MutableStateFlow<Double?>(null)
     val address = MutableStateFlow("")
     val garagePhotos = MutableStateFlow<List<String>>(emptyList())
     val availableServices = MutableStateFlow<List<String>>(emptyList())

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    fun isNameValid(name: String): Boolean {
        return name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size >= 2
    }

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
        if (name.value.trim().split("\\s+".toRegex()).size < 2) {
            _uiState.value = AuthUiState.Error("Please provide at least two names.")
            return
        }

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
                // Reset state first to clear any role-specific data in ViewModels
                _userProfile.value = null
                _uiState.value = AuthUiState.Idle
                
                authRepository.signOut()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during logout: ${e.message}")
            }
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

    fun nextStep() {
        val next = when (_signUpStep.value) {
            SignUpStep.BASIC_INFO -> SignUpStep.IDENTITY_VERIFICATION
            SignUpStep.IDENTITY_VERIFICATION -> SignUpStep.ADDITIONAL_INFO
            SignUpStep.ADDITIONAL_INFO -> SignUpStep.ADDITIONAL_INFO
        }
        _signUpStep.value = next
    }

    fun previousStep() {
        val prev = when (_signUpStep.value) {
            SignUpStep.BASIC_INFO -> SignUpStep.BASIC_INFO
            SignUpStep.IDENTITY_VERIFICATION -> SignUpStep.BASIC_INFO
            SignUpStep.ADDITIONAL_INFO -> SignUpStep.IDENTITY_VERIFICATION
        }
        _signUpStep.value = prev
    }

    fun uploadImage(uri: android.net.Uri, type: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val path = "users/$userId/$type/${System.currentTimeMillis()}.jpg"
            val result = authRepository.uploadImage(uri, path)
            if (result.isSuccess) {
                val url = result.getOrNull() ?: ""
                when (type) {
                    "profile" -> profilePhotoUrl.value = url
                    "id_front" -> idPhotoUrl.value = url
                    "vehicle" -> vehiclePhotoUrl.value = url
                    "certificate" -> certificatePhotoUrl.value = url
                    "garage" -> {
                        val current = garagePhotos.value.toMutableList()
                        current.add(url)
                        garagePhotos.value = current
                    }
                }
                _uiState.value = AuthUiState.Idle
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    fun completeProfile(onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            val userRole = if (role.value == "mechanic") UserRole.MECHANIC else UserRole.DRIVER
            
            val profile = UserProfile(
                id = userId,
                name = name.value,
                email = email.value,
                phone = phoneNumber.value,
                role = userRole,
                profilePhotoUrl = profilePhotoUrl.value,
                idNumber = idNumber.value,
                idPhotoUrl = idPhotoUrl.value,
                status = VerificationStatus.PENDING,
                vehicleType = vehicleType.value,
                vehicleModel = vehicleModel.value,
                numberPlate = numberPlate.value,
                vehiclePhotoUrl = vehiclePhotoUrl.value,
                certificateNumber = certificateNumber.value,
                certificatePhotoUrl = certificatePhotoUrl.value,
                institutionName = institutionName.value,
                experienceYears = experienceYears.value,
                latitude = latitude.value,
                longitude = longitude.value,
                address = address.value,
                garagePhotos = garagePhotos.value,
                skills = availableServices.value,
                availableServices = availableServices.value,
                vehicles = _userProfile.value?.vehicles ?: emptyList() // Keep existing vehicles
            )
            
            val result = authRepository.updateUserProfile(profile)
            
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success(role.value)
                onSuccess()
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun updateVehicle(updatedVehicle: com.example.mototap.core.model.VehicleProfile) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentProfile = _userProfile.value ?: return

        viewModelScope.launch {
            val updatedVehicles = currentProfile.vehicles.toMutableList()
            val index = updatedVehicles.indexOfFirst { it.id == updatedVehicle.id }
            
            if (index != -1) {
                updatedVehicles[index] = updatedVehicle
            } else {
                updatedVehicles.add(updatedVehicle.copy(id = UUID.randomUUID().toString()))
            }

            val newProfile = currentProfile.copy(vehicles = updatedVehicles)
            val result = authRepository.updateUserProfile(newProfile)
            
            if (result.isSuccess) {
                _userProfile.value = newProfile
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        val currentProfile = _userProfile.value ?: return
        viewModelScope.launch {
            val updatedVehicles = currentProfile.vehicles.filter { it.id != vehicleId }
            val newProfile = currentProfile.copy(vehicles = updatedVehicles)
            val result = authRepository.updateUserProfile(newProfile)
            if (result.isSuccess) {
                _userProfile.value = newProfile
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        Log.d("AuthViewModel", "resetState called")
        _uiState.value = AuthUiState.Idle
        _signUpStep.value = SignUpStep.BASIC_INFO
    }
}

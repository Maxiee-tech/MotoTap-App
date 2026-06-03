package com.example.mototap.features.driver

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.JobRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

data class DriverUiState(
    val currentUserId: String? = null,
    val jobs: List<JobRequest> = emptyList(),
    val issueTypeInput: String = "",
    val descriptionInput: String = "",
    val locationInput: String = "",
    val priceInput: String = "1000",
    val infoMessage: String? = null,
    val mechanicPhoneNumber: String? = null,
    val availableMechanics: List<UserProfile> = emptyList(),
    val isLocating: Boolean = false,
    val selectedMechanic: UserProfile? = null,
    val bookingSuccess: Boolean = false,
    val userLocation: LatLng? = null,
)

class DriverHomeViewModel(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUserId.collectLatest { userId ->
                _uiState.update { it.copy(currentUserId = userId) }
                
                if (userId == null) {
                    _uiState.update { it.copy(
                        availableMechanics = emptyList(),
                        jobs = emptyList(),
                        mechanicPhoneNumber = null
                    ) }
                    return@collectLatest
                }

                launch {
                    authRepository.observeAllMechanics().collect { mechanics ->
                        _uiState.update { it.copy(availableMechanics = mechanics) }
                    }
                }

                launch {
                    jobRepository.observeDriverJobs(userId).collect { jobs ->
                        _uiState.update { it.copy(jobs = jobs) }
                        val activeJob = jobs.firstOrNull { it.mechanicId != null }
                        if (activeJob != null) {
                            val profile = authRepository.getUserProfile(activeJob.mechanicId!!)
                            _uiState.update { it.copy(mechanicPhoneNumber = profile?.phone) }
                        }
                    }
                }
            }
        }
    }

    fun onIssueChanged(value: String) = _uiState.update { it.copy(issueTypeInput = value) }
    fun onDescriptionChanged(value: String) = _uiState.update { it.copy(descriptionInput = value) }
    fun onLocationChanged(value: String) = _uiState.update { it.copy(locationInput = value) }
    fun onPriceChanged(value: String) = _uiState.update { it.copy(priceInput = value) }

    fun deleteRequest(jobId: String) = viewModelScope.launch { jobRepository.deleteJob(jobId) }

    @SuppressLint("MissingPermission")
    fun quickRequest(context: Context, serviceName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, infoMessage = "Getting location...") }
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location: Location? = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                
                val locationLabel = if (location != null) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addresses?.firstOrNull()?.getAddressLine(0) ?: "${location.latitude}, ${location.longitude}"
                } else "Nairobi, Kenya"

                val userId = _uiState.value.currentUserId ?: return@launch
                jobRepository.createJob(userId, serviceName, "Quick request", locationLabel, 1000L, null, null)
                _uiState.update { it.copy(isLocating = false, infoMessage = "Request sent!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocating = false, infoMessage = "Location error") }
            }
        }
    }

    fun submitJob() {
        val state = _uiState.value
        val userId = state.currentUserId ?: return
        if (state.issueTypeInput.isBlank() || state.locationInput.isBlank()) {
            _uiState.update { it.copy(infoMessage = "Fill all fields") }
            return
        }

        viewModelScope.launch {
            jobRepository.createJob(
                userId, state.issueTypeInput, state.descriptionInput, state.locationInput,
                state.priceInput.toLongOrNull() ?: 0L, null, null
            )
            _uiState.update { it.copy(issueTypeInput = "", descriptionInput = "", locationInput = "", infoMessage = "Request sent") }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(infoMessage = null) }
    fun setSelectedMechanic(mechanic: UserProfile) = _uiState.update { it.copy(selectedMechanic = mechanic) }
    fun setUserLocation(location: LatLng) = _uiState.update { it.copy(userLocation = location) }

    @SuppressLint("MissingPermission")
    fun bookMechanic(context: Context, mechanic: UserProfile, serviceName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLocating = true, infoMessage = "Booking $serviceName...") }
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location: Location? = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                val locationLabel = location?.let {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    geocoder.getFromLocation(it.latitude, it.longitude, 1)?.firstOrNull()?.getAddressLine(0)
                } ?: "Location shared"

                val userId = _uiState.value.currentUserId ?: return@launch
                
                // Reuse the same room ID if an inquiry already exists!
                // This ensures the chat history moves from Inquiry to Booking.
                val inquiryId = "chat_${userId}_${mechanic.id}"
                val result = jobRepository.createJob(
                    driverId = userId, 
                    issueType = serviceName, 
                    description = "Direct booking", 
                    locationLabel = locationLabel, 
                    suggestedPrice = 1500L, 
                    mechanicId = mechanic.id, 
                    jobId = inquiryId
                )

                if (result.isSuccess) {
                    _uiState.update { it.copy(isLocating = false, infoMessage = "Booked!", bookingSuccess = true) }
                    _navigationEvent.emit("job_tracking")
                } else {
                    _uiState.update { it.copy(isLocating = false, infoMessage = "Failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLocating = false, infoMessage = "Error") }
            }
        }
    }

    fun openChatWithMechanic(mechanicId: String, onJobIdFound: (String) -> Unit) {
        val userId = _uiState.value.currentUserId ?: return
        viewModelScope.launch {
            // Check if we already have an active job or inquiry with this mechanic
            // to ensure we always use the SAME room and see history.
            val existingJob = _uiState.value.jobs.find { 
                it.mechanicId == mechanicId && it.status != com.example.mototap.core.model.JobStatus.CLOSED 
            }
            
            if (existingJob != null) {
                Log.d("DriverVM", "Found existing room: ${existingJob.id}")
                onJobIdFound(existingJob.id)
            } else {
                val inquiryId = "chat_${userId}_${mechanicId}"
                Log.d("DriverVM", "Creating/Reopening inquiry room: $inquiryId")
                
                // Create the parent document if it doesn't exist
                jobRepository.createJob(
                    driverId = userId, 
                    issueType = "Inquiry", 
                    description = "Conversation history",
                    locationLabel = "Chat", 
                    suggestedPrice = 0, 
                    mechanicId = mechanicId,
                    jobId = inquiryId
                )
                onJobIdFound(inquiryId)
            }
        }
    }
}

class DriverHomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DriverHomeViewModel(authRepository, jobRepository) as T
}

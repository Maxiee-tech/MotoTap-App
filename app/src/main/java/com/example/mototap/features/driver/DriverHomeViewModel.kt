package com.example.mototap.features.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
)

class DriverHomeViewModel(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    init {
        loadMechanics()
        viewModelScope.launch {
            authRepository.currentUserId.collectLatest { userId ->
                _uiState.value = _uiState.value.copy(currentUserId = userId)
                if (userId == null) return@collectLatest

                jobRepository.observeDriverJobs(userId).collectLatest { jobs ->
                    _uiState.value = _uiState.value.copy(jobs = jobs)
                    
                    // Fetch mechanic phone number if a job is active and has a mechanic
                    val activeJob = jobs.firstOrNull { it.mechanicId != null }
                    if (activeJob != null) {
                        val profile = authRepository.getUserProfile(activeJob.mechanicId!!)
                        _uiState.value = _uiState.value.copy(mechanicPhoneNumber = profile?.phone)
                    }
                }
            }
        }
    }

    private fun loadMechanics() {
        viewModelScope.launch {
            val mechanics = authRepository.getAllMechanics()
            _uiState.value = _uiState.value.copy(availableMechanics = mechanics)
        }
    }

    fun onIssueChanged(value: String) {
        _uiState.value = _uiState.value.copy(issueTypeInput = value)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(descriptionInput = value)
    }

    fun onLocationChanged(value: String) {
        _uiState.value = _uiState.value.copy(locationInput = value)
    }

    fun onPriceChanged(value: String) {
        _uiState.value = _uiState.value.copy(priceInput = value)
    }

    fun deleteRequest(jobId: String) {
        viewModelScope.launch {
            jobRepository.deleteJob(jobId)
        }
    }

    fun submitJob() {
        val state = _uiState.value
        val userId = state.currentUserId ?: return
        if (state.issueTypeInput.isBlank() || state.locationInput.isBlank()) {
            _uiState.value = state.copy(infoMessage = "Please enter issue and location")
            return
        }

        val price = state.priceInput.toLongOrNull() ?: 0L
        viewModelScope.launch {
            val result = jobRepository.createJob(
                driverId = userId,
                issueType = state.issueTypeInput.trim(),
                description = state.descriptionInput.trim(),
                locationLabel = state.locationInput.trim(),
                suggestedPrice = price,
            )

            _uiState.value = _uiState.value.copy(
                issueTypeInput = "",
                descriptionInput = "",
                locationInput = "",
                infoMessage = if (result.isSuccess) "Request sent" else "Failed to send request",
            )
        }
    }
}

class DriverHomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DriverHomeViewModel(authRepository, jobRepository) as T
    }
}

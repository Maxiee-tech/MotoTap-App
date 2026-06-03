package com.example.mototap.features.mechanic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.JobRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MechanicUiState(
    val openJobs: List<JobRequest> = emptyList(),
    val ongoingJobs: List<JobRequest> = emptyList(),
    val completedJobs: List<JobRequest> = emptyList(),
    val selectedSkills: List<String> = emptyList(),
    val infoMessage: String? = null,
    val isLoading: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

class MechanicDashboardViewModel(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MechanicUiState())
    val uiState: StateFlow<MechanicUiState> = _uiState.asStateFlow()

    private val _selectedSkills = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            authRepository.currentUserId.collectLatest { userId ->
                if (userId != null) {
                    val profile = authRepository.getUserProfile(userId)
                    // Combine both fields to ensure all selected services are captured
                    val combinedSkills = ((profile?.skills ?: emptyList()) + (profile?.availableServices ?: emptyList())).distinct()
                    _selectedSkills.value = combinedSkills
                    _uiState.value = _uiState.value.copy(
                        selectedSkills = combinedSkills,
                        latitude = profile?.latitude,
                        longitude = profile?.longitude,
                    )

                    // Combine observations that depend on auth state
                    combine(
                        jobRepository.observeOpenJobs(),
                        jobRepository.observeMechanicJobs(userId),
                        _selectedSkills
                    ) { openJobs, myJobs, skills ->
                        // 1. New public requests that match skills
                        val newRequests = openJobs.filter { job -> 
                            job.status == JobStatus.REQUESTED && 
                            (skills.isEmpty() || skills.contains(job.issueType))
                        }
                        
                        // 2. Jobs assigned specifically to this mechanic
                        val ongoing = myJobs.filter { 
                            it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS
                        }

                        val completed = myJobs.filter {
                            it.status == JobStatus.COMPLETED || it.status == JobStatus.PAID || it.status == JobStatus.CLOSED
                        }
                        
                        Triple(newRequests, ongoing, completed)
                    }.collect { (newRequests, ongoing, completed) ->
                        _uiState.value = _uiState.value.copy(
                            openJobs = newRequests,
                            ongoingJobs = ongoing,
                            completedJobs = completed,
                            selectedSkills = _selectedSkills.value
                        )
                    }
                } else {
                    _uiState.value = MechanicUiState()
                    _selectedSkills.value = emptyList()
                }
            }
        }
    }

    fun toggleSkill(skill: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentSkills = _selectedSkills.value.toMutableList()
        if (currentSkills.contains(skill)) {
            currentSkills.remove(skill)
        } else {
            currentSkills.add(skill)
        }
        
        _selectedSkills.value = currentSkills
        _uiState.value = _uiState.value.copy(selectedSkills = currentSkills)
        
        viewModelScope.launch {
            val result = authRepository.updateMechanicSkills(currentUserId, currentSkills)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(infoMessage = "Failed to update services")
            }
        }
    }

    fun acceptJob(jobId: String, mechanicId: String) {
        viewModelScope.launch {
            val result = jobRepository.acceptJob(jobId, mechanicId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(infoMessage = "Job accepted successfully")
            } else {
                _uiState.value = _uiState.value.copy(infoMessage = "Failed to accept job")
            }
        }
    }

    fun updateStatus(jobId: String, status: JobStatus) {
        viewModelScope.launch {
            val result = jobRepository.updateJobStatus(jobId, status)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(infoMessage = "Status updated to ${status.name}")
                
                // Award points if job completed
                if (status == JobStatus.COMPLETED) {
                    val job = _uiState.value.ongoingJobs.find { it.id == jobId }
                    job?.driverId?.let { driverId ->
                        authRepository.awardLoyaltyPoints(driverId, 10) // Award 10 points
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(infoMessage = "Failed to update status")
            }
        }
    }

    fun updateLocation(lat: Double, lon: Double) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(latitude = lat, longitude = lon)
        
        viewModelScope.launch {
            val profile = authRepository.getUserProfile(currentUserId)
            if (profile != null) {
                val updatedProfile = profile.copy(latitude = lat, longitude = lon)
                val result = authRepository.updateUserProfile(updatedProfile)
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(infoMessage = "Failed to update location")
                } else {
                    _uiState.value = _uiState.value.copy(infoMessage = "Location updated successfully")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }
}

class MechanicDashboardViewModelFactory(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MechanicDashboardViewModel(authRepository, jobRepository) as T
    }
}

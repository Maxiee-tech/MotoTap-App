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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MechanicUiState(
    val openJobs: List<JobRequest> = emptyList(),
    val ongoingJobs: List<JobRequest> = emptyList(),
    val selectedSkills: List<String> = emptyList(),
    val infoMessage: String? = null,
    val isLoading: Boolean = false,
)

class MechanicDashboardViewModel(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MechanicUiState())
    val uiState: StateFlow<MechanicUiState> = _uiState.asStateFlow()

    private val _selectedSkills = MutableStateFlow<List<String>>(emptyList())

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId != null) {
            viewModelScope.launch {
                val profile = authRepository.getUserProfile(currentUserId)
                val skills = profile?.skills ?: emptyList()
                _selectedSkills.value = skills
                _uiState.value = _uiState.value.copy(selectedSkills = skills)
            }
        }

        viewModelScope.launch {
            combine(
                jobRepository.observeOpenJobs(),
                _selectedSkills
            ) { jobs, skills ->
                val newRequests = jobs.filter { job -> 
                    job.status == JobStatus.REQUESTED && 
                    (skills.isEmpty() || skills.contains(job.issueType))
                }
                
                val ongoing = jobs.filter { 
                    it.mechanicId == currentUserId && 
                    (it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS) 
                }
                
                Pair(newRequests, ongoing)
            }.collect { (newRequests, ongoing) ->
                _uiState.value = _uiState.value.copy(
                    openJobs = newRequests,
                    ongoingJobs = ongoing,
                    selectedSkills = _selectedSkills.value
                )
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
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(infoMessage = "Failed to update status")
            } else {
                _uiState.value = _uiState.value.copy(infoMessage = "Status updated to ${status.name}")
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

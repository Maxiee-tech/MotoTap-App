package com.example.mototap.features.mechanic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.repository.JobRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MechanicUiState(
    val openJobs: List<JobRequest> = emptyList(),
    val ongoingJobs: List<JobRequest> = emptyList(),
    val infoMessage: String? = null,
)

class MechanicDashboardViewModel(
    private val jobRepository: JobRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MechanicUiState())
    val uiState: StateFlow<MechanicUiState> = _uiState.asStateFlow()

    init {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        viewModelScope.launch {
            jobRepository.observeOpenJobs().collectLatest { jobs ->
                // Filter jobs that are REQUESTED for "New Requests"
                val newRequests = jobs.filter { it.status == JobStatus.REQUESTED }
                // Filter jobs ASSIGNED to this mechanic or currently IN_PROGRESS
                val ongoing = jobs.filter { 
                    it.mechanicId == currentUserId && 
                    (it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS) 
                }
                
                _uiState.value = _uiState.value.copy(
                    openJobs = newRequests,
                    ongoingJobs = ongoing
                )
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
    private val jobRepository: JobRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MechanicDashboardViewModel(jobRepository) as T
    }
}

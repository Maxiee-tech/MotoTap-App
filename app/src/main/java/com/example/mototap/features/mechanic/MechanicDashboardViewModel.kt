package com.example.mototap.features.mechanic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.repository.JobRepository
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
        viewModelScope.launch {
            jobRepository.observeOpenJobs().collectLatest { jobs ->
                // Filter jobs that are REQUESTED for "New Requests"
                val newRequests = jobs.filter { it.status == JobStatus.REQUESTED }
                _uiState.value = _uiState.value.copy(openJobs = newRequests)
            }
        }
        // In a real app, we'd also observe jobs assigned specifically to THIS mechanic.
        // For simplicity, we'll focus on the "Accept" flow.
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

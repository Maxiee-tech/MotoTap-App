package com.example.mototap.core.repository

import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    suspend fun createJob(
        driverId: String,
        issueType: String,
        description: String,
        locationLabel: String,
        suggestedPrice: Long,
    ): Result<String>

    fun observeDriverJobs(driverId: String): Flow<List<JobRequest>>

    fun observeOpenJobs(): Flow<List<JobRequest>>

    suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<Unit>
    
    suspend fun acceptJob(jobId: String, mechanicId: String): Result<Unit>

    suspend fun deleteJob(jobId: String): Result<Unit>
}

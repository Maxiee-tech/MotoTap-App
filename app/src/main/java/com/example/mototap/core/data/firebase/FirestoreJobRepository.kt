package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.model.JobRequest
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.repository.JobRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreJobRepository(
    private val firestore: FirebaseFirestore,
) : JobRepository {

    private val jobs = firestore.collection("jobs")

    override suspend fun createJob(
        driverId: String,
        issueType: String,
        description: String,
        locationLabel: String,
        suggestedPrice: Long,
        mechanicId: String?,
        jobId: String?,
    ): Result<String> = runCatching {
        val doc = if (jobId != null) jobs.document(jobId) else jobs.document()
        doc.set(
            mapOf(
                "driverId" to driverId,
                "mechanicId" to mechanicId,
                "issueType" to issueType,
                "description" to description,
                "locationLabel" to locationLabel,
                "status" to (if (mechanicId != null) JobStatus.ASSIGNED.name else JobStatus.REQUESTED.name),
                "price" to suggestedPrice,
                "createdAtMillis" to System.currentTimeMillis(),
            )
        ).await()
        doc.id
    }

    override fun observeDriverJobs(driverId: String): Flow<List<JobRequest>> = callbackFlow {
        if (driverId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = jobs
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreJobRepo", "Error observing driver jobs: ${error.message}")
                    trySend(emptyList()) 
                    return@addSnapshotListener
                }

                val list = snapshot
                    ?.documents
                    ?.mapNotNull { it.toJobRequestOrNull() }
                    ?.sortedByDescending { it.createdAtMillis }
                    .orEmpty()

                trySend(list)
            }

        awaitClose { 
            Log.d("FirestoreJobRepo", "Removing driver jobs observer")
            subscription.remove() 
        }
    }

    override fun observeMechanicJobs(mechanicId: String): Flow<List<JobRequest>> = callbackFlow {
        if (mechanicId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = jobs
            .whereEqualTo("mechanicId", mechanicId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreJobRepo", "Error observing mechanic jobs: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot
                    ?.documents
                    ?.mapNotNull { it.toJobRequestOrNull() }
                    ?.sortedByDescending { it.createdAtMillis }
                    .orEmpty()

                trySend(list)
            }

        awaitClose {
            Log.d("FirestoreJobRepo", "Removing mechanic jobs observer")
            subscription.remove()
        }
    }

    override fun observeOpenJobs(): Flow<List<JobRequest>> = callbackFlow {
        val subscription = jobs
            .whereIn(
                "status",
                listOf(
                    JobStatus.REQUESTED.name,
                    JobStatus.MATCHING.name,
                    JobStatus.ASSIGNED.name,
                )
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Critical: Just log the error and send empty list. 
                    // Do NOT use close(error) or throw any exception.
                    Log.e("FirestoreJobRepo", "Error observing open jobs: ${error.message}")
                    trySend(emptyList()) 
                    return@addSnapshotListener
                }

                val list = snapshot
                    ?.documents
                    ?.mapNotNull { it.toJobRequestOrNull() }
                    ?.sortedByDescending { it.createdAtMillis }
                    .orEmpty()

                trySend(list)
            }

        awaitClose { 
            Log.d("FirestoreJobRepo", "Removing open jobs observer")
            subscription.remove() 
        }
    }

    override suspend fun updateJobStatus(jobId: String, status: JobStatus): Result<Unit> = runCatching {
        jobs.document(jobId)
            .update("status", status.name)
            .await()
    }

    override suspend fun acceptJob(jobId: String, mechanicId: String): Result<Unit> = runCatching {
        jobs.document(jobId)
            .update(
                mapOf(
                    "mechanicId" to mechanicId,
                    "status" to JobStatus.ASSIGNED.name
                )
            ).await()
    }

    override suspend fun deleteJob(jobId: String): Result<Unit> = runCatching {
        jobs.document(jobId).delete().await()
    }

    private fun DocumentSnapshot.toJobRequestOrNull(): JobRequest? {
        val driverId = getString("driverId") ?: return null
        val issueType = getString("issueType") ?: return null
        val locationLabel = getString("locationLabel") ?: return null
        val status = getString("status")
            ?.let { runCatching { JobStatus.valueOf(it) }.getOrNull() }
            ?: JobStatus.REQUESTED

        return JobRequest(
            id = id,
            driverId = driverId,
            mechanicId = getString("mechanicId"),
            issueType = issueType,
            description = getString("description") ?: "",
            locationLabel = locationLabel,
            status = status,
            price = getLong("price") ?: 0L,
            createdAtMillis = getLong("createdAtMillis") ?: 0L,
        )
    }
}

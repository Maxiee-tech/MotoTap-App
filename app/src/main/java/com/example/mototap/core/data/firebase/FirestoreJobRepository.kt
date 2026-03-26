package com.example.mototap.core.data.firebase

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
    ): Result<String> = runCatching {
        val doc = jobs.document()
        doc.set(
            mapOf(
                "driverId" to driverId,
                "mechanicId" to null,
                "issueType" to issueType,
                "description" to description,
                "locationLabel" to locationLabel,
                "status" to JobStatus.REQUESTED.name,
                "price" to suggestedPrice,
                "createdAtMillis" to System.currentTimeMillis(),
            )
        ).await()
        doc.id
    }

    override fun observeDriverJobs(driverId: String): Flow<List<JobRequest>> = callbackFlow {
        val subscription = jobs
            .whereEqualTo("driverId", driverId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot
                    ?.documents
                    ?.mapNotNull { it.toJobRequestOrNull() }
                    .orEmpty()

                trySend(list)
            }

        awaitClose { subscription.remove() }
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
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val list = snapshot
                    ?.documents
                    ?.mapNotNull { it.toJobRequestOrNull() }
                    .orEmpty()

                trySend(list)
            }

        awaitClose { subscription.remove() }
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

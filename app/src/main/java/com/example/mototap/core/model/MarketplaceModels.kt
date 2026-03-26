package com.example.mototap.core.model

enum class UserRole {
    DRIVER,
    MECHANIC,
    ADMIN,
}

enum class JobStatus {
    REQUESTED,
    MATCHING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    PAID,
    CLOSED,
}

data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val role: UserRole,
)

data class MechanicProfile(
    val id: String,
    val displayName: String,
    val skills: List<String>,
    val isAvailable: Boolean,
    val rating: Double,
    val approved: Boolean,
)

data class JobRequest(
    val id: String,
    val driverId: String,
    val mechanicId: String?,
    val issueType: String,
    val description: String = "",
    val locationLabel: String,
    val status: JobStatus,
    val price: Long,
    val createdAtMillis: Long,
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestampMillis: Long,
    val read: Boolean,
)

data class PaymentRecord(
    val id: String,
    val jobId: String,
    val amount: Long,
    val status: String,
    val mpesaReceipt: String?,
)


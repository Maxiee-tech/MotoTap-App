package com.example.mototap.core.model

enum class UserRole {
    DRIVER,
    MECHANIC,
    ADMIN,
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    SUSPENDED,
}

enum class JobStatus {
    INQUIRY,
    REQUESTED,
    MATCHING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    PAID,
    CLOSED,
}

data class VehicleProfile(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val licensePlate: String = "",
    val mileage: String = "",
    val lastServiceDate: Long? = null,
    val photoUrl: String = "",
)

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.DRIVER,
    val profilePhotoUrl: String = "",
    val idNumber: String = "",
    val idPhotoUrl: String = "",
    val status: VerificationStatus = VerificationStatus.PENDING,
    
    // Driver specific fields
    val vehicleType: String = "",
    val vehicleModel: String = "",
    val numberPlate: String = "",
    val vehiclePhotoUrl: String = "",
    val vehicles: List<VehicleProfile> = emptyList(),
    val loyaltyPoints: Int = 0,
    
    // Mechanic specific fields
    val certificateNumber: String = "",
    val certificatePhotoUrl: String = "",
    val institutionName: String = "",
    val experienceYears: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val garagePhotos: List<String> = emptyList(),
    
    val skills: List<String> = emptyList(),
    val availableServices: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val brandSpecializations: List<String> = emptyList(),
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

data class Review(
    val id: String = "",
    val mechanicId: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
)

data class PaymentRecord(
    val id: String,
    val jobId: String,
    val amount: Long,
    val status: String,
    val mpesaReceipt: String?,
)

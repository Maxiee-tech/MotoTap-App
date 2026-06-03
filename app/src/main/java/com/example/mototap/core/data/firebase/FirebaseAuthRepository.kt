package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.model.UserRole
import com.example.mototap.core.model.VerificationStatus
import com.example.mototap.core.model.Review
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : AuthRepository {

    override val currentUserId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            trySend(it.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.uid)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun ensureSignedIn() {
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            Log.d("FirebaseAuthRepo", "Attempting signIn for $email")
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuthRepo", "signIn successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "signIn error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, name: String, role: String, phoneNumber: String?): Result<Unit> {
        return try {
            Log.d("FirebaseAuthRepo", "Attempting signUp for $email with role: $role")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User creation failed")
            Log.d("FirebaseAuthRepo", "Auth user created: $userId. Saving to Firestore...")
            
            val userMap = mutableMapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "role" to role.lowercase().trim() // Normalize role
            )
            phoneNumber?.let { userMap["phoneNumber"] = it }

            firestore.collection("users").document(userId).set(userMap).await()
            Log.d("FirebaseAuthRepo", "Firestore document created. signUp complete.")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "signUp error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
            // Important: Force a clear of the currentUserId flow immediately
            // But currentUserId is a callbackFlow which is driven by AuthStateListener
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "signOut error: ${e.message}")
        }
    }

    override suspend fun getUserRole(userId: String): String? {
        return try {
            Log.d("FirebaseAuthRepo", "Fetching role for $userId")
            val document = firestore.collection("users").document(userId).get().await()
            if (!document.exists()) {
                Log.w("FirebaseAuthRepo", "User document does not exist for $userId")
                return null
            }
            val role = document.getString("role")?.lowercase()?.trim()
            Log.d("FirebaseAuthRepo", "Role fetched and normalized: $role")
            role
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getUserRole error: ${e.message}")
            null
        }
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (!document.exists()) return null
            
            val roleStr = document.getString("role")?.lowercase()?.trim() ?: "driver"
            val role = if (roleStr == "mechanic") UserRole.MECHANIC else UserRole.DRIVER
            val statusStr = document.getString("status")?.uppercase() ?: "PENDING"
            val status = try { VerificationStatus.valueOf(statusStr) } catch (e: Exception) { VerificationStatus.PENDING }

            val garagePhotos = (document.get("garagePhotos") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
            val skills = (document.get("skills") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
            val availableServices = (document.get("availableServices") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
            val brandSpecializations = (document.get("brandSpecializations") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
            
            val vehicles = (document.get("vehicles") as? List<*>)?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                com.example.mototap.core.model.VehicleProfile(
                    id = map["id"]?.toString() ?: "",
                    make = map["make"]?.toString() ?: "",
                    model = map["model"]?.toString() ?: "",
                    year = map["year"]?.toString() ?: "",
                    licensePlate = map["licensePlate"]?.toString() ?: "",
                    mileage = map["mileage"]?.toString() ?: "",
                    lastServiceDate = map["lastServiceDate"] as? Long,
                    photoUrl = map["photoUrl"]?.toString() ?: ""
                )
            } ?: emptyList()

            UserProfile(
                id = userId,
                name = document.getString("name") ?: "",
                email = document.getString("email") ?: "",
                phone = document.getString("phoneNumber") ?: "",
                role = role,
                profilePhotoUrl = document.getString("profilePhotoUrl") ?: "",
                idNumber = document.getString("idNumber") ?: "",
                idPhotoUrl = document.getString("idPhotoUrl") ?: "",
                status = status,
                vehicleType = document.getString("vehicleType") ?: "",
                vehicleModel = document.getString("vehicleModel") ?: "",
                numberPlate = document.getString("numberPlate") ?: "",
                vehiclePhotoUrl = document.getString("vehiclePhotoUrl") ?: "",
                vehicles = vehicles,
                loyaltyPoints = document.getLong("loyaltyPoints")?.toInt() ?: 0,
                certificateNumber = document.getString("certificateNumber") ?: "",
                certificatePhotoUrl = document.getString("certificatePhotoUrl") ?: "",
                institutionName = document.getString("institutionName") ?: "",
                experienceYears = document.getString("experienceYears") ?: "",
                latitude = document.getDouble("latitude"),
                longitude = document.getDouble("longitude"),
                address = document.getString("address") ?: "",
                garagePhotos = garagePhotos,
                skills = skills,
                availableServices = availableServices,
                rating = document.getDouble("rating") ?: 0.0,
                reviewCount = document.getLong("reviewCount")?.toInt() ?: 0,
                brandSpecializations = brandSpecializations
            )
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getUserProfile error: ${e.message}")
            null
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val vehiclesMap = profile.vehicles.map { vehicle ->
                mapOf(
                    "id" to vehicle.id,
                    "make" to vehicle.make,
                    "model" to vehicle.model,
                    "year" to vehicle.year,
                    "licensePlate" to vehicle.licensePlate,
                    "mileage" to vehicle.mileage,
                    "lastServiceDate" to vehicle.lastServiceDate,
                    "photoUrl" to vehicle.photoUrl
                )
            }

            val userMap = mutableMapOf(
                "name" to profile.name,
                "phoneNumber" to profile.phone,
                "profilePhotoUrl" to profile.profilePhotoUrl,
                "idNumber" to profile.idNumber,
                "idPhotoUrl" to profile.idPhotoUrl,
                "status" to profile.status.name,
                "vehicleType" to profile.vehicleType,
                "vehicleModel" to profile.vehicleModel,
                "numberPlate" to profile.numberPlate,
                "vehiclePhotoUrl" to profile.vehiclePhotoUrl,
                "vehicles" to vehiclesMap,
                "loyaltyPoints" to profile.loyaltyPoints,
                "certificateNumber" to profile.certificateNumber,
                "certificatePhotoUrl" to profile.certificatePhotoUrl,
                "institutionName" to profile.institutionName,
                "experienceYears" to profile.experienceYears,
                "latitude" to profile.latitude,
                "longitude" to profile.longitude,
                "address" to profile.address,
                "garagePhotos" to profile.garagePhotos,
                "skills" to profile.skills,
                "availableServices" to profile.availableServices,
                "rating" to profile.rating,
                "reviewCount" to profile.reviewCount,
                "brandSpecializations" to profile.brandSpecializations
            )
            firestore.collection("users").document(profile.id).update(userMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "updateUserProfile error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(uri: android.net.Uri, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "uploadImage error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getAllMechanics(): List<UserProfile> {
        return try {
            val snapshot = firestore.collection("users")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val role = doc.getString("role")?.lowercase()?.trim()
                if (role == "mechanic") {
                    mapDocumentToUserProfile(doc.id, doc)
                } else null
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getAllMechanics error: ${e.message}")
            emptyList()
        }
    }

    override fun observeAllMechanics(): Flow<List<UserProfile>> = callbackFlow {
        val registration = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseAuthRepo", "Error observing mechanics: ${error.message}")
                    trySend(emptyList()) 
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val mechanics = snapshot.documents.mapNotNull { doc ->
                        val role = doc.getString("role")?.lowercase()?.trim()
                        if (role == "mechanic") {
                            mapDocumentToUserProfile(doc.id, doc)
                        } else null
                    }
                    trySend(mechanics)
                }
            }
        awaitClose { 
            Log.d("FirebaseAuthRepo", "Removing mechanics observer")
            registration.remove() 
        }
    }

    private fun mapDocumentToUserProfile(id: String, doc: com.google.firebase.firestore.DocumentSnapshot): UserProfile {
        val name = doc.getString("name") ?: ""
        val email = doc.getString("email") ?: ""
        val phone = doc.getString("phoneNumber") ?: ""
        val roleStr = doc.getString("role")?.lowercase()?.trim() ?: "mechanic"
        val role = if (roleStr == "mechanic") UserRole.MECHANIC else UserRole.DRIVER
        val statusStr = doc.getString("status")?.uppercase() ?: "PENDING"
        val status = try { VerificationStatus.valueOf(statusStr) } catch (e: Exception) { VerificationStatus.PENDING }

        val garagePhotos = (doc.get("garagePhotos") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
        val skills = (doc.get("skills") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
        val availableServices = (doc.get("availableServices") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
        val brandSpecializations = (doc.get("brandSpecializations") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()

        val vehicles = (doc.get("vehicles") as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            com.example.mototap.core.model.VehicleProfile(
                id = map["id"]?.toString() ?: "",
                make = map["make"]?.toString() ?: "",
                model = map["model"]?.toString() ?: "",
                year = map["year"]?.toString() ?: "",
                licensePlate = map["licensePlate"]?.toString() ?: "",
                mileage = map["mileage"]?.toString() ?: "",
                lastServiceDate = map["lastServiceDate"] as? Long,
                photoUrl = map["photoUrl"]?.toString() ?: ""
            )
        } ?: emptyList()

        return UserProfile(
            id = id,
            name = name,
            email = email,
            phone = phone,
            role = role,
            profilePhotoUrl = doc.getString("profilePhotoUrl") ?: "",
            idNumber = doc.getString("idNumber") ?: "",
            idPhotoUrl = doc.getString("idPhotoUrl") ?: "",
            status = status,
            vehicleType = doc.getString("vehicleType") ?: "",
            vehicleModel = doc.getString("vehicleModel") ?: "",
            numberPlate = doc.getString("numberPlate") ?: "",
            vehiclePhotoUrl = doc.getString("vehiclePhotoUrl") ?: "",
            vehicles = vehicles,
            loyaltyPoints = doc.getLong("loyaltyPoints")?.toInt() ?: 0,
            certificateNumber = doc.getString("certificateNumber") ?: "",
            certificatePhotoUrl = doc.getString("certificatePhotoUrl") ?: "",
            institutionName = doc.getString("institutionName") ?: "",
            experienceYears = doc.getString("experienceYears") ?: "",
            latitude = doc.getDouble("latitude"),
            longitude = doc.getDouble("longitude"),
            address = doc.getString("address") ?: "",
            garagePhotos = garagePhotos,
            skills = skills,
            availableServices = availableServices,
            rating = doc.getDouble("rating") ?: 0.0,
            reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
            brandSpecializations = brandSpecializations
        )
    }

    override suspend fun deleteAccount(currentPassword: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        val userId = user.uid
        val email = user.email ?: return Result.failure(Exception("Missing email for re-authentication"))
        
        return try {
            Log.d("FirebaseAuthRepo", "Starting account deletion for $userId")

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            Log.d("FirebaseAuthRepo", "Re-authenticating user before deletion...")
            user.reauthenticate(credential).await()
            Log.d("FirebaseAuthRepo", "Re-authentication successful.")

            // 1. Delete from Firestore (with timeout to prevent hanging if API is disabled)
            try {
                Log.d("FirebaseAuthRepo", "Deleting Firestore user document...")
                withTimeout(5000) {
                    firestore.collection("users").document(userId).delete().await()
                }
                Log.d("FirebaseAuthRepo", "Firestore document deleted.")
            } catch (e: Exception) {
                Log.e("FirebaseAuthRepo", "Firestore deletion failed or timed out: ${e.message}")
                // We continue to delete the Auth account even if Firestore fails
            }

            // 2. Delete from Firebase Auth
            Log.d("FirebaseAuthRepo", "Deleting Auth account...")
            user.delete().await()
            Log.d("FirebaseAuthRepo", "Auth account deleted.")
            
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("FirebaseAuthRepo", "Deletion failed: Invalid password: ${e.message}")
            Result.failure(Exception("The password you entered is incorrect."))
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.e("FirebaseAuthRepo", "Deletion failed: Re-authentication required: ${e.message}")
            Result.failure(Exception("Please re-enter your current password and try again."))
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "deleteAccount error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun updateMechanicSkills(userId: String, skills: List<String>): Result<Unit> {
        return try {
            val updates = mapOf(
                "skills" to skills,
                "availableServices" to skills
            )
            firestore.collection("users").document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "updateMechanicSkills error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun awardLoyaltyPoints(userId: String, points: Int): Result<Unit> = runCatching {
        firestore.runTransaction { transaction ->
            val userRef = firestore.collection("users").document(userId)
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("loyaltyPoints") ?: 0L
            transaction.update(userRef, "loyaltyPoints", currentPoints + points)
        }.await()
    }

    override suspend fun addReview(review: Review): Result<Unit> = runCatching {
        val reviewRef = firestore.collection("reviews").document()
        val reviewData = mapOf(
            "mechanicId" to review.mechanicId,
            "driverId" to review.driverId,
            "driverName" to review.driverName,
            "rating" to review.rating,
            "comment" to review.comment,
            "timestampMillis" to review.timestampMillis
        )
        
        firestore.runTransaction { transaction ->
            val mechanicRef = firestore.collection("users").document(review.mechanicId)
            val mechanicDoc = transaction.get(mechanicRef)
            
            val currentRating = mechanicDoc.getDouble("rating") ?: 0.0
            val currentCount = mechanicDoc.getLong("reviewCount") ?: 0L
            
            val newCount = currentCount + 1
            val newRating = ((currentRating * currentCount) + review.rating) / newCount
            
            transaction.set(reviewRef, reviewData)
            transaction.update(mechanicRef, mapOf(
                "rating" to newRating,
                "reviewCount" to newCount
            ))
        }.await()
    }

    override fun observeMechanicReviews(mechanicId: String): Flow<List<Review>> = callbackFlow {
        val subscription = firestore.collection("reviews")
            .whereEqualTo("mechanicId", mechanicId)
            .orderBy("timestampMillis", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    Review(
                        id = doc.id,
                        mechanicId = doc.getString("mechanicId") ?: "",
                        driverId = doc.getString("driverId") ?: "",
                        driverName = doc.getString("driverName") ?: "",
                        rating = doc.getDouble("rating") ?: 0.0,
                        comment = doc.getString("comment") ?: "",
                        timestampMillis = doc.getLong("timestampMillis") ?: 0L
                    )
                } ?: emptyList()
                trySend(reviews)
            }
        awaitClose { subscription.remove() }
    }
}

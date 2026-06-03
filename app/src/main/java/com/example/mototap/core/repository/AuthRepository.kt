package com.example.mototap.core.repository

import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.model.Review
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>

    suspend fun ensureSignedIn()
    
    suspend fun signIn(email: String, password: String): Result<Unit>
    
    suspend fun signUp(email: String, password: String, name: String, role: String, phoneNumber: String? = null): Result<Unit>

    suspend fun signOut()
    
    suspend fun getUserRole(userId: String): String?

    suspend fun getUserProfile(userId: String): UserProfile?

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    suspend fun uploadImage(uri: android.net.Uri, path: String): Result<String>

    suspend fun getAllMechanics(): List<UserProfile>
    fun observeAllMechanics(): Flow<List<UserProfile>>

    suspend fun deleteAccount(currentPassword: String): Result<Unit>

    suspend fun updateMechanicSkills(userId: String, skills: List<String>): Result<Unit>

    suspend fun awardLoyaltyPoints(userId: String, points: Int): Result<Unit>

    suspend fun addReview(review: Review): Result<Unit>
    fun observeMechanicReviews(mechanicId: String): Flow<List<Review>>
}

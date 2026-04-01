package com.example.mototap.core.repository

import com.example.mototap.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>

    suspend fun ensureSignedIn()
    
    suspend fun signIn(email: String, password: String): Result<Unit>
    
    suspend fun signUp(email: String, password: String, name: String, role: String, phoneNumber: String? = null): Result<Unit>

    suspend fun signOut()
    
    suspend fun getUserRole(userId: String): String?

    suspend fun getUserProfile(userId: String): UserProfile?

    suspend fun getAllMechanics(): List<UserProfile>

    suspend fun deleteAccount(currentPassword: String): Result<Unit>

    suspend fun updateMechanicSkills(userId: String, skills: List<String>): Result<Unit>
}

package com.example.mototap.core.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>

    suspend fun ensureSignedIn()
    
    suspend fun signIn(email: String, password: String): Result<Unit>
    
    suspend fun signUp(email: String, password: String, name: String, role: String): Result<Unit>

    suspend fun signOut()
    
    suspend fun getUserRole(userId: String): String?
}

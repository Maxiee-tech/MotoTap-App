package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    override suspend fun signUp(email: String, password: String, name: String, role: String): Result<Unit> {
        return try {
            Log.d("FirebaseAuthRepo", "Attempting signUp for $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("User creation failed")
            Log.d("FirebaseAuthRepo", "Auth user created: $userId. Saving to Firestore...")
            
            val userMap = mapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "role" to role
            )
            firestore.collection("users").document(userId).set(userMap).await()
            Log.d("FirebaseAuthRepo", "Firestore document created. signUp complete.")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "signUp error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun getUserRole(userId: String): String? {
        return try {
            Log.d("FirebaseAuthRepo", "Fetching role for $userId")
            val document = firestore.collection("users").document(userId).get().await()
            val role = document.getString("role")
            Log.d("FirebaseAuthRepo", "Role fetched: $role")
            role
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getUserRole error: ${e.message}")
            null
        }
    }
}

package com.example.mototap.core.data.firebase

import android.util.Log
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.model.UserRole
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

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
            
            val name = document.getString("name") ?: ""
            val phone = document.getString("phoneNumber") ?: ""
            val roleStr = document.getString("role")?.lowercase()?.trim() ?: "customer"
            val role = if (roleStr == "mechanic") UserRole.MECHANIC else UserRole.DRIVER
            @Suppress("UNCHECKED_CAST")
            val skills = document.get("skills") as? List<String> ?: emptyList()
            
            UserProfile(id = userId, name = name, phone = phone, role = role, skills = skills)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getUserProfile error: ${e.message}")
            null
        }
    }

    override suspend fun getAllMechanics(): List<UserProfile> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "mechanic")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: ""
                val phone = doc.getString("phoneNumber") ?: ""
                @Suppress("UNCHECKED_CAST")
                val skills = doc.get("skills") as? List<String> ?: emptyList()
                UserProfile(id = id, name = name, phone = phone, role = UserRole.MECHANIC, skills = skills)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "getAllMechanics error: ${e.message}")
            emptyList()
        }
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
            firestore.collection("users").document(userId).update("skills", skills).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepo", "updateMechanicSkills error: ${e.message}")
            Result.failure(e)
        }
    }
}

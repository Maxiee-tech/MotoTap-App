package com.example.mototap.core.data

import com.example.mototap.core.model.UserRole
import com.example.mototap.core.model.VerificationStatus
import com.example.mototap.core.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object MechanicSeeder {
    suspend fun seedMechanics(firestore: FirebaseFirestore, authRepository: AuthRepository) {
        val mechanics = listOf(
            mapOf(
                "name" to "JACK MAMBIA",
                "email" to "gphinidy@gmail.com",
                "address" to "Reskos",
                "phoneNumber" to "0768547362",
                "availableServices" to listOf("Engine overhaul"),
                "skills" to listOf("Engine overhaul"),
                "experienceYears" to "3-5 years",
                "role" to "mechanic",
                "status" to "VERIFIED",
                "rating" to 4.5,
                "reviewCount" to 12,
                "latitude" to -1.286389,
                "longitude" to 36.817223
            ),
            mapOf(
                "name" to "Samuel Mbugua",
                "email" to "samuelmbugua1998@gmail.com",
                "address" to "0100",
                "phoneNumber" to "0757332325",
                "availableServices" to listOf("On-site vehicle diagnostics"),
                "skills" to listOf("On-site vehicle diagnostics"),
                "experienceYears" to "Over 5 years",
                "role" to "mechanic",
                "status" to "VERIFIED",
                "rating" to 4.8,
                "reviewCount" to 25,
                "latitude" to -1.2921,
                "longitude" to 36.8219
            ),
            mapOf(
                "name" to "Calvince Omondi",
                "email" to "aolomondi64@gmail.com",
                "address" to "Dagoreti South",
                "phoneNumber" to "0111914297",
                "availableServices" to listOf("Brake pad replacement"),
                "skills" to listOf("Brake pad replacement"),
                "experienceYears" to "Over 5 years",
                "role" to "mechanic",
                "status" to "VERIFIED",
                "rating" to 4.7,
                "reviewCount" to 18,
                "latitude" to -1.3050,
                "longitude" to 36.7642
            ),
            mapOf(
                "name" to "Ahmed Nickle",
                "email" to "ahmednickie9@gmail.com",
                "address" to "Nairobi",
                "phoneNumber" to "0728988790",
                "availableServices" to listOf("Engine oil top-up/change"),
                "skills" to listOf("Engine oil top-up/change"),
                "experienceYears" to "Below 3 years",
                "role" to "mechanic",
                "status" to "VERIFIED",
                "rating" to 4.2,
                "reviewCount" to 8,
                "latitude" to -1.2750,
                "longitude" to 36.8110
            ),
            mapOf(
                "name" to "Dickson Usuti",
                "email" to "osutidickson09@gmail.com",
                "address" to "025",
                "phoneNumber" to "0746285526",
                "availableServices" to listOf("Shock absorber replacement"),
                "skills" to listOf("Shock absorber replacement"),
                "experienceYears" to "Over 5 years",
                "role" to "mechanic",
                "status" to "VERIFIED",
                "rating" to 4.9,
                "reviewCount" to 31,
                "latitude" to -1.3100,
                "longitude" to 36.8500
            )
        )

        for (mechanic in mechanics) {
            try {
                val email = mechanic["email"] as String
                val phone = mechanic["phoneNumber"] as String
                val name = mechanic["name"] as String
                
                val authResult = authRepository.signUp(
                    email = email,
                    password = phone,
                    name = name,
                    role = "mechanic",
                    phoneNumber = phone
                )

                if (authResult.isSuccess || authResult.exceptionOrNull()?.message?.contains("already in use") == true) {
                    val query = firestore.collection("users").whereEqualTo("email", email).get().await()
                    if (!query.isEmpty) {
                        val docId = query.documents.first().id
                        firestore.collection("users").document(docId).update(mechanic).await()
                        android.util.Log.d("MechanicSeeder", "Verified and updated: $name")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MechanicSeeder", "Error seeding account: ${e.message}")
            }
        }
    }
}

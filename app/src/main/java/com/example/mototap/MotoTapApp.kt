package com.example.mototap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.mototap.core.data.firebase.FirebaseAuthRepository
import com.example.mototap.core.data.firebase.FirestoreChatRepository
import com.example.mototap.core.data.firebase.FirestoreJobRepository
import com.example.mototap.navigation.MotoTapNavHost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MotoTapApp() {
    val firestore = FirebaseFirestore.getInstance()
    val authRepository = remember { FirebaseAuthRepository(FirebaseAuth.getInstance(), firestore) }
    val jobRepository = remember { FirestoreJobRepository(firestore) }
    val chatRepository = remember { FirestoreChatRepository(firestore) }


    MotoTapNavHost(
        authRepository = authRepository,
        jobRepository = jobRepository,
        chatRepository = chatRepository
    )
}

package com.example.mototap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.mototap.core.data.MechanicSeeder
import com.example.mototap.core.data.firebase.FirebaseAuthRepository
import com.example.mototap.core.data.firebase.FirebaseGarageRepository
import com.example.mototap.core.data.firebase.FirestoreChatRepository
import com.example.mototap.core.data.firebase.FirestoreJobRepository
import com.example.mototap.core.update.AppUpdateChecker
import com.example.mototap.core.update.AppUpdateDialog
import com.example.mototap.core.update.AppUpdateInfo
import com.example.mototap.navigation.MotoTapNavHost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MotoTapApp() {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val garageRepository = remember { FirebaseGarageRepository(firestore) }
    val authRepository = remember { FirebaseAuthRepository(FirebaseAuth.getInstance(), firestore, garageRepository) }
    val jobRepository = remember { FirestoreJobRepository(firestore) }
    val chatRepository = remember { FirestoreChatRepository(firestore) }
    var pendingUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        MechanicSeeder.seedMechanics(firestore, authRepository)
        pendingUpdate = AppUpdateChecker.checkForUpdate(context.applicationContext)
    }

    MotoTapNavHost(
        authRepository = authRepository,
        jobRepository = jobRepository,
        chatRepository = chatRepository,
        garageRepository = garageRepository,
    )

    pendingUpdate?.let { info ->
        AppUpdateDialog(
            info = info,
            onDismiss = { pendingUpdate = null },
        )
    }
}

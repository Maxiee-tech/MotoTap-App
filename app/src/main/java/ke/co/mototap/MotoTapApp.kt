package ke.co.mototap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ke.co.mototap.core.data.MechanicSeeder
import ke.co.mototap.core.data.firebase.FirebaseAuthRepository
import ke.co.mototap.core.data.firebase.FirebaseGarageRepository
import ke.co.mototap.core.data.firebase.FirestoreChatRepository
import ke.co.mototap.core.data.firebase.FirestoreJobRepository
import ke.co.mototap.core.notify.IncomingAlertsHost
import ke.co.mototap.core.update.AppUpdateChecker
import ke.co.mototap.core.update.AppUpdateDialog
import ke.co.mototap.core.update.AppUpdateInfo
import ke.co.mototap.navigation.MotoTapNavHost
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

    Box(modifier = Modifier.fillMaxSize()) {
        MotoTapNavHost(
            authRepository = authRepository,
            jobRepository = jobRepository,
            chatRepository = chatRepository,
            garageRepository = garageRepository,
        )
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            IncomingAlertsHost(
                authRepository = authRepository,
                chatRepository = chatRepository,
                jobRepository = jobRepository,
            )
        }
    }

    pendingUpdate?.let { info ->
        AppUpdateDialog(
            info = info,
            onDismiss = { pendingUpdate = null },
        )
    }
}

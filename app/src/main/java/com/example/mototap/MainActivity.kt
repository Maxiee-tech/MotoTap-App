package com.example.mototap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.LocalCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.example.mototap.ui.theme.MotoTapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firestore = FirebaseFirestore.getInstance()
        
        // Fix: Use setLocalCacheSettings instead of the deprecated setPersistenceEnabled
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        firestore.firestoreSettings = settings

        enableEdgeToEdge()
        setContent {
            MotoTapTheme {
                MotoTapApp()
            }
        }
    }
}

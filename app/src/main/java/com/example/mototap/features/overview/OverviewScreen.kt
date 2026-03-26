package com.example.mototap.features.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Moto Tap MVP",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = "- Driver request creation")
        Text(text = "- Real-time Firestore job streams")
        Text(text = "- Mechanic queue with state updates")
        Text(text = "- Firestore offline cache enabled")
        Text(text = "Next: OTP auth, chat, and M-PESA via Cloud Functions")
    }
}


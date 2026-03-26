package com.example.mototap.features.mechanic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mototap.core.model.JobRequest

@Composable
fun MechanicDashboardScreen(
    state: MechanicUiState,
    onAccept: (String) -> Unit,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            Text(
                text = "Mechanic Queue",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        state.infoMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        items(state.openJobs, key = { it.id }) { job ->
            MechanicJobCard(
                job = job,
                onAccept = onAccept,
                onStart = onStart,
            )
        }
    }
}

@Composable
private fun MechanicJobCard(
    job: JobRequest,
    onAccept: (String) -> Unit,
    onStart: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = job.issueType, style = MaterialTheme.typography.titleSmall)
            Text(text = "Location: ${job.locationLabel}")
            Text(text = "Status: ${job.status.name}")
            Text(text = "Offer: KES ${job.price}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAccept(job.id) }) {
                    Text("Accept")
                }
                Button(onClick = { onStart(job.id) }) {
                    Text("Start")
                }
            }
        }
    }
}


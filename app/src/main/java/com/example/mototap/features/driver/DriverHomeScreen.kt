package com.example.mototap.features.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mototap.core.model.JobRequest

@Composable
fun DriverHomeScreen(
    state: DriverUiState,
    onIssueChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
                text = "Driver Request",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            OutlinedTextField(
                value = state.issueTypeInput,
                onValueChange = onIssueChange,
                label = { Text("Issue type") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = state.locationInput,
                onValueChange = onLocationChange,
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = state.priceInput,
                onValueChange = onPriceChange,
                label = { Text("Suggested price (KES)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Request mechanic")
            }
        }
        state.infoMessage?.let { message ->
            item {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        item {
            Text(
                text = "My jobs",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(state.jobs, key = { it.id }) { job ->
            JobCard(job = job)
        }
    }
}

@Composable
private fun JobCard(job: JobRequest) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = job.issueType,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = job.locationLabel)
            Text(text = "Status: ${job.status.name}")
            Text(text = "Price: KES ${job.price}")
        }
    }
}


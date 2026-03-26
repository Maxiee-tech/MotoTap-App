package com.example.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.core.model.JobRequest
import com.example.mototap.features.driver.BottomNavigationBar
import com.example.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobTrackingScreen(
    job: JobRequest?,
    onBack: () -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MOTOTAP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onChat) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Chat",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MotoRed
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(currentRoute = "home", onNavigate = {})
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MotoRed)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JOB DETAILS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (job != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Service: ${job.issueType}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Problem Description:",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (job.description.isBlank()) "No description provided." else job.description,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location: ${job.locationLabel}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProviderTrackingItem(stringResource(R.string.on_the_way), Icons.Default.Refresh, true)
            Spacer(modifier = Modifier.height(16.dp))
            ProviderTrackingItem(stringResource(R.string.in_progress), Icons.Default.CheckCircle, true)
            Spacer(modifier = Modifier.height(16.dp))
            ProviderTrackingItem(stringResource(R.string.completed), Icons.Default.CheckCircle, true)
        }
    }
}

@Composable
fun ProviderTrackingItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MotoRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.Transparent)
                .padding(vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 16.sp
                )
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)
            }
        }
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Checked",
                tint = MotoRed,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

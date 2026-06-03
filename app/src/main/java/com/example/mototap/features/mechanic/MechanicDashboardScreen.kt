package com.example.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.core.model.JobRequest
import com.example.mototap.ui.BottomNavigationBar
import com.example.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDashboardScreen(
    state: MechanicUiState,
    onAccept: (String) -> Unit,
    onStart: (String) -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MOTO TAP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MotoRed
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onNavigate = { route: String ->
                    when(route) {
                        "requests" -> onNavigateToRequests()
                        "messages" -> onNavigateToMessages()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // My Jobs Section (Step 5)
            if (state.ongoingJobs.isNotEmpty()) {
                item {
                    Text(
                        text = "NEW DIRECT BOOKINGS / ACTIVE",
                        color = MotoRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(state.ongoingJobs, key = { "ongoing_${it.id}" }) { job ->
                    MechanicJobCard(
                        job = job,
                        onAccept = onAccept,
                        onStart = onStart,
                        isOngoing = true
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                Text(
                    text = "PUBLIC QUEUE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (state.openJobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No pending requests nearby", color = Color.Gray)
                    }
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
}

@Composable
private fun MechanicJobCard(
    job: JobRequest,
    onAccept: (String) -> Unit,
    onStart: (String) -> Unit,
    isOngoing: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isOngoing) Color(0xFF1F2C34) else Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOngoing) MotoRed.copy(alpha = 0.5f) else Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = job.issueType,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                if (isOngoing) {
                    Surface(
                        color = if (job.status.name == "ASSIGNED") Color.Blue.copy(alpha = 0.2f) else MotoRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = job.status.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "KES ${job.price}",
                        color = MotoRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = job.locationLabel, color = Color.LightGray, fontSize = 14.sp)
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isOngoing) {
                    OutlinedButton(
                        onClick = { onAccept(job.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ACCEPT")
                    }
                }
                
                Button(
                    onClick = { onStart(job.id) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOngoing && job.status.name == "IN_PROGRESS") Color.DarkGray else MotoRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = job.status.name != "IN_PROGRESS"
                ) {
                    Text(
                        text = if (job.status.name == "ASSIGNED") "START WORK" else if (job.status.name == "IN_PROGRESS") "IN PROGRESS" else "START", 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

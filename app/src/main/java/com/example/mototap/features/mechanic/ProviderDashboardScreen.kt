package com.example.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.features.driver.BottomNavigationBar
import com.example.mototap.ui.theme.MotoRed
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: MechanicDashboardViewModel,
    onBack: () -> Unit,
    onSubmitQuote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var newRequestsActive by remember { mutableStateOf(true) }
    var ongoingJobsActive by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.provider_dashboard),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
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
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
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
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    ToggleRow(stringResource(R.string.new_requests), newRequestsActive) { newRequestsActive = it }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (newRequestsActive) {
                    if (uiState.openJobs.isEmpty()) {
                        item {
                            Text(
                                "No new requests available",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(uiState.openJobs) { job ->
                            RequestItem(
                                label = "${job.issueType} - ${job.locationLabel}",
                                actionText = "ACCEPT",
                                onActionClick = { viewModel.acceptJob(job.id, currentUserId) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ToggleRow(stringResource(R.string.ongoing_jobs), ongoingJobsActive) { ongoingJobsActive = it }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardButton(stringResource(R.string.submit_quote), onSubmitQuote)
                DashboardButton(stringResource(R.string.on_the_way), {})
                DashboardButton(stringResource(R.string.in_progress), {})
                DashboardButton(stringResource(R.string.complete), {})
            }
        }
    }
}

@Composable
fun ToggleRow(label: String, isActive: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Black, fontWeight = FontWeight.Bold)
        Switch(
            checked = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MotoRed,
                checkedTrackColor = MotoRed.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun RequestItem(label: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, maxLines = 1)
        }
        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = androidx.compose.ui.graphics.RectangleShape,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(text = actionText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
        shape = androidx.compose.ui.graphics.RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

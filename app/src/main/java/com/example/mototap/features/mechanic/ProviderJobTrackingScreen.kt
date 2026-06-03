package com.example.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.mototap.ui.BottomNavigationBar
import com.example.mototap.features.driver.TrackingItem
import com.example.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobTrackingScreen(
    job: JobRequest?,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onComplete: (String) -> Unit,
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
            BottomNavigationBar(
                currentRoute = "requests",
                onNavigate = { _ -> }
            )
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
            TrackingItem(stringResource(R.string.on_the_way), Icons.Default.LocationOn, true)
            Spacer(modifier = Modifier.height(16.dp))
            TrackingItem(stringResource(R.string.in_progress), Icons.Default.CheckCircle, true)
            Spacer(modifier = Modifier.height(16.dp))
            TrackingItem(stringResource(R.string.completed), Icons.Default.CheckCircle, false)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    job?.let { onComplete(it.id) }
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = androidx.compose.ui.graphics.RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark as Finished",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

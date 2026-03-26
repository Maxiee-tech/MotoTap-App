package com.example.mototap.features.driver

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobTrackingScreen(
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
                onClick = { /* Handle Ensure There */ },
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
                        text = "Ensure There",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TrackingItem(label: String, icon: ImageVector, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MotoRed,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        if (isChecked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Checked",
                tint = MotoRed,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White)
            )
        }
    }
}

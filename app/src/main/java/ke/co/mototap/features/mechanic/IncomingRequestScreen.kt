package ke.co.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.repository.AuthRepository
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRequestScreen(
    job: JobRequest?,
    authRepository: AuthRepository,
    onViewRequest: () -> Unit,
    onDecline: () -> Unit,
    onAlerts: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(job?.driverId) {
        val id = job?.driverId.orEmpty()
        driver = if (id.isBlank()) null else authRepository.getUserProfile(id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { BrandLogo(modifier = Modifier.height(26.dp)) },
                actions = {
                    IconButton(onClick = onAlerts) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = MotoInk)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        if (job == null) {
            Box(
                modifier = modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("This request is no longer available.", color = MotoMuted)
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE8EA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MotoRed, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("New Service Request", color = MotoInk, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, textAlign = TextAlign.Center)
            Text(
                "You have a new job request",
                color = MotoMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
            )

            IncomingDriverRow(job = job, driver = driver)
            IncomingDetailRow(
                icon = Icons.Default.DirectionsCar,
                title = listOf(job.vehicleMake, job.vehicleModel).filter { it.isNotBlank() }.joinToString(" ")
                    .ifBlank { "Vehicle" },
                subtitle = job.vehicleId.takeIf { it.isNotBlank() && !it.contains(":") },
            )
            IncomingDetailRow(
                icon = Icons.Default.Build,
                title = job.issueType.ifBlank { job.serviceName.ifBlank { "Service request" } },
                subtitle = job.description.takeIf { it.isNotBlank() && it != "Direct booking" }
                    ?: "Needs roadside assistance",
            )
            IncomingDetailRow(
                icon = Icons.Default.LocationOn,
                title = formatShopAreaLabel(job.locationLabel).ifBlank { job.locationLabel.ifBlank { "Location shared" } },
                subtitle = null,
            )
            IncomingDetailRow(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "Estimated service value",
                subtitle = if (job.price > 0) "KSh ${formatKsh(job.price)}" else "On request",
            )

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onViewRequest,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Text("View Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 20.dp),
                shape = MotoButtonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoRed),
            ) {
                Text("Decline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun IncomingDriverRow(job: JobRequest, driver: UserProfile?) {
    val name = driver?.name?.ifBlank { job.driverName }?.ifBlank { "Customer" }
        ?: job.driverName.ifBlank { "Customer" }
    val photo = driver?.profilePhotoUrl?.ifBlank { null } ?: job.driverPhotoUrl.ifBlank { null }
    val rating = driver?.rating ?: 0.0
    val reviews = driver?.reviewCount ?: 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F2)),
            contentAlignment = Alignment.Center,
        ) {
            if (!photo.isNullOrBlank()) {
                AsyncImage(model = photo, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C", color = MotoInk, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5B400), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (reviews > 0) "${String.format("%.1f", rating)} ($reviews reviews)" else "New customer",
                    color = MotoMuted,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun IncomingDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = MotoMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

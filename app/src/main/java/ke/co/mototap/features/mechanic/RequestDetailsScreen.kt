package ke.co.mototap.features.mechanic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.repository.AuthRepository
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(
    job: JobRequest?,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(job?.driverId) {
        val id = job?.driverId.orEmpty()
        driver = if (id.isBlank()) null else authRepository.getUserProfile(id)
    }
    val vehicle = remember(driver, job) {
        val vehicles = driver?.vehicles.orEmpty()
        vehicles.firstOrNull { it.id == job?.vehicleId && job.vehicleId.isNotBlank() }
            ?: vehicles.firstOrNull {
                it.make.equals(job?.vehicleMake, ignoreCase = true) &&
                    it.model.equals(job?.vehicleModel, ignoreCase = true)
            }
            ?: vehicles.firstOrNull()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Request Details", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MotoInk)
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

        val name = driver?.name?.ifBlank { job.driverName }?.ifBlank { "Customer" }
            ?: job.driverName.ifBlank { "Customer" }
        val photo = driver?.profilePhotoUrl?.ifBlank { null } ?: job.driverPhotoUrl.ifBlank { null }
        val phone = driver?.phone.orEmpty()
        val vehiclePhoto = vehicle?.photoUrl?.ifBlank { null } ?: driver?.vehiclePhotoUrl?.ifBlank { null }
        val makeModel = listOf(
            vehicle?.make?.ifBlank { job.vehicleMake } ?: job.vehicleMake,
            vehicle?.model?.ifBlank { job.vehicleModel } ?: job.vehicleModel,
        ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "Vehicle" }
        val plate = vehicle?.licensePlate?.ifBlank { null } ?: driver?.numberPlate?.ifBlank { null }
        val yearColor = listOf(vehicle?.year?.ifBlank { null }).filterNotNull().joinToString(" • ")
        val notes = job.description.takeIf { it.isNotBlank() && !it.equals("Direct booking", ignoreCase = true) }
        val area = formatShopAreaLabel(job.locationLabel).ifBlank { job.locationLabel.ifBlank { "Location shared" } }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5B400), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        val rating = driver?.rating ?: 0.0
                        val reviews = driver?.reviewCount ?: 0
                        Text(
                            if (reviews > 0) "${String.format("%.1f", rating)} ($reviews reviews)" else "New customer",
                            color = MotoMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (phone.isNotBlank()) {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                        }
                    },
                    enabled = phone.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F5F7)),
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = if (phone.isNotBlank()) MotoInk else MotoMuted)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Vehicle", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!vehiclePhoto.isNullOrBlank()) {
                        AsyncImage(model = vehiclePhoto, contentDescription = makeModel, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MotoRed)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(makeModel, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (!plate.isNullOrBlank()) {
                        Text(plate, color = MotoInk, fontSize = 14.sp)
                    }
                    if (yearColor.isNotBlank()) {
                        Text(yearColor, color = MotoMuted, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            DetailBlock(
                icon = Icons.Default.Build,
                title = "Problem",
                headline = job.issueType.ifBlank { job.serviceName.ifBlank { "Service request" } },
                body = notes ?: "Needs roadside assistance",
            )
            DetailBlock(
                icon = Icons.Default.LocationOn,
                title = "Location",
                headline = area,
                trailing = {
                    TextButton(
                        onClick = {
                            val query = Uri.encode(job.locationLabel.ifBlank { area })
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")))
                        }
                    ) {
                        Text("View on Map", color = MotoRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                },
            )
            DetailBlock(
                icon = Icons.Default.Description,
                title = "Customer notes",
                headline = notes ?: "No extra notes from the customer.",
            )
            DetailBlock(
                icon = Icons.Outlined.AccountBalanceWallet,
                title = "Estimated service value",
                headline = if (job.price > 0) "KSh ${formatKsh(job.price)}" else "On request",
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Text("Accept Job", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
private fun DetailBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    headline: String,
    body: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(headline, color = MotoInk, fontSize = 15.sp, modifier = Modifier.padding(start = 26.dp))
        if (!body.isNullOrBlank() && body != headline) {
            Text(body, color = MotoMuted, fontSize = 14.sp, modifier = Modifier.padding(start = 26.dp, top = 4.dp))
        }
    }
}

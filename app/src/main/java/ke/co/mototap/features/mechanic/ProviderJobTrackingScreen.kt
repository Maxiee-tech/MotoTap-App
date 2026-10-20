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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.repository.AuthRepository
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobTrackingScreen(
    job: JobRequest?,
    authRepository: AuthRepository,
    providerLat: Double? = null,
    providerLng: Double? = null,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onStart: (String) -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    var headingOut by remember { mutableStateOf(false) }
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
    val mapCenter = remember(providerLat, providerLng) {
        if (providerLat != null && providerLng != null) LatLng(providerLat, providerLng)
        else LatLng(-1.286389, 36.817223)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapCenter, 13f)
    }
    LaunchedEffect(mapCenter) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(mapCenter, 13f)
    }

    val status = job?.status ?: JobStatus.ASSIGNED
    val accepted = true
    val onTheWay = headingOut || status == JobStatus.IN_PROGRESS || status == JobStatus.COMPLETED
    val started = status == JobStatus.IN_PROGRESS || status == JobStatus.COMPLETED || status == JobStatus.PAID
    val completed = status == JobStatus.COMPLETED || status == JobStatus.PAID || status == JobStatus.CLOSED

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Job Accepted", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE8EA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MotoRed, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Job Accepted", color = MotoInk, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text(
                "You are now assigned to this job",
                color = MotoMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            val name = driver?.name?.ifBlank { job?.driverName }?.ifBlank { "Customer" }
                ?: job?.driverName.orEmpty().ifBlank { "Customer" }
            val photo = driver?.profilePhotoUrl?.ifBlank { null } ?: job?.driverPhotoUrl?.ifBlank { null }
            val phone = driver?.phone.orEmpty()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF0F0F2)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(model = photo, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C", fontWeight = FontWeight.Bold, color = MotoInk)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        if (phone.isNotBlank()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                    },
                    enabled = phone.isNotBlank(),
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF4F5F7)),
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = MotoInk, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onChat,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF4F5F7)),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MotoInk, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val makeModel = listOf(
                vehicle?.make?.ifBlank { job?.vehicleMake } ?: job?.vehicleMake.orEmpty(),
                vehicle?.model?.ifBlank { job?.vehicleModel } ?: job?.vehicleModel.orEmpty(),
            ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { job?.issueType.orEmpty().ifBlank { "Vehicle" } }
            val plate = vehicle?.licensePlate?.ifBlank { null } ?: driver?.numberPlate?.ifBlank { null }
            val vehiclePhoto = vehicle?.photoUrl?.ifBlank { null } ?: driver?.vehiclePhotoUrl?.ifBlank { null }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)),
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
                    if (!plate.isNullOrBlank()) Text(plate, color = MotoMuted, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val area = formatShopAreaLabel(job?.locationLabel).ifBlank { job?.locationLabel.orEmpty().ifBlank { "Location shared" } }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(area, color = MotoInk, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        headingOut = true
                        val query = Uri.encode(job?.locationLabel?.ifBlank { area } ?: area)
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$query")))
                    }
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate", color = MotoRed, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(state = MarkerState(position = mapCenter), title = "You")
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            JobAcceptedStepper(
                accepted = accepted,
                onTheWay = onTheWay,
                started = started,
                completed = completed,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        if (phone.isNotBlank()) context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                    },
                    enabled = phone.isNotBlank(),
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MotoButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoRed),
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Customer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onChat,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = MotoButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoRed),
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val id = job?.id ?: return@Button
                    onStart(id)
                },
                enabled = job != null && !completed,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Text(
                    when {
                        completed -> "Completed"
                        started -> "Continue Job"
                        else -> "Start Job"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun JobAcceptedStepper(
    accepted: Boolean,
    onTheWay: Boolean,
    started: Boolean,
    completed: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            StepDot(accepted)
            StepLine(onTheWay)
            StepDot(onTheWay)
            StepLine(started)
            StepDot(started)
            StepLine(completed)
            StepDot(completed)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Accepted", "On the way", "Job started", "Completed").forEach { label ->
                Text(label, color = MotoMuted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StepDot(done: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (done) MotoRed else Color(0xFFE8E8EA)),
        contentAlignment = Alignment.Center,
    ) {
        if (done) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
    }
}

@Composable
private fun RowScope.StepLine(filled: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(3.dp)
            .background(if (filled) MotoRed else Color(0xFFE8E8EA)),
    )
}

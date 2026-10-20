package ke.co.mototap.features.mechanic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DragHandle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.repository.AuthRepository
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoCardLine
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

private data class ChecklistItem(
    val id: String,
    val label: String,
    val checked: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobInProgressScreen(
    job: JobRequest?,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    var notes by remember(job?.id) { mutableStateOf("") }
    var checklist by remember(job?.id, job?.issueType, job?.description) {
        mutableStateOf(defaultChecklist(job))
    }

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

    val completed = job?.status == JobStatus.COMPLETED ||
        job?.status == JobStatus.PAID ||
        job?.status == JobStatus.CLOSED

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Job in Progress", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            val name = driver?.name?.ifBlank { job?.driverName }?.ifBlank { "Customer" }
                ?: job?.driverName.orEmpty().ifBlank { "Customer" }
            val photo = driver?.profilePhotoUrl?.ifBlank { null } ?: job?.driverPhotoUrl?.ifBlank { null }
            val phone = driver?.phone.orEmpty()
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFF0F0F2)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C",
                            fontWeight = FontWeight.Bold,
                            color = MotoInk,
                        )
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

            Spacer(modifier = Modifier.height(18.dp))
            val makeModel = listOf(
                vehicle?.make?.ifBlank { job?.vehicleMake } ?: job?.vehicleMake.orEmpty(),
                vehicle?.model?.ifBlank { job?.vehicleModel } ?: job?.vehicleModel.orEmpty(),
            ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { job?.issueType.orEmpty().ifBlank { "Vehicle" } }
            val plate = vehicle?.licensePlate?.ifBlank { null } ?: driver?.numberPlate?.ifBlank { null }
            val vehiclePhoto = vehicle?.photoUrl?.ifBlank { null } ?: driver?.vehiclePhotoUrl?.ifBlank { null }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!vehiclePhoto.isNullOrBlank()) {
                        AsyncImage(
                            model = vehiclePhoto,
                            contentDescription = makeModel,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
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

            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Build, contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Problem", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        job?.description?.takeIf { it.isNotBlank() && !it.equals("Direct booking", ignoreCase = true) }
                            ?: job?.issueType?.ifBlank { job.serviceName }?.ifBlank { "Service requested" }
                            ?: "Service requested",
                        color = MotoMuted,
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Service Checklist", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text("(optional)", color = MotoMuted, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            checklist.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = { checked ->
                            checklist = checklist.map { if (it.id == item.id) it.copy(checked = checked) else it }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MotoRed,
                            uncheckedColor = Color(0xFFC9C9CD),
                            checkmarkColor = Color.White,
                        ),
                    )
                    Text(item.label, color = MotoInk, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.DragHandle, contentDescription = null, tint = Color(0xFFC9C9CD), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Service Notes", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add notes about the work done...", color = MotoMuted) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MotoCardLine,
                    unfocusedBorderColor = MotoCardLine,
                    focusedContainerColor = Color(0xFFF7F7F8),
                    unfocusedContainerColor = Color(0xFFF7F7F8),
                    cursorColor = MotoRed,
                ),
            )

            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Service Amount", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(
                    "KSh  ${formatKsh(job?.price ?: 0L)}",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onChat,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MotoButtonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoRed),
                border = BorderStroke(1.dp, MotoRed),
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Customer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { job?.id?.let(onComplete) },
                enabled = job != null && !completed,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Text(
                    if (completed) "Completed" else "Mark Job Complete",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

private fun defaultChecklist(job: JobRequest?): List<ChecklistItem> {
    val extra = job?.additionalServices.orEmpty().mapNotNull { note ->
        note.text.trim().takeIf { it.isNotBlank() }?.let { text ->
            ChecklistItem(id = note.id.ifBlank { text }, label = text, checked = true)
        }
    }
    val defaults = listOf(
        ChecklistItem("diagnose", "Diagnose issue", checked = true),
        ChecklistItem("battery", "Check battery", checked = true),
        ChecklistItem("starter", "Check starter motor", checked = false),
        ChecklistItem("other", "Other repairs (specify)", checked = false),
    )
    return if (extra.isEmpty()) defaults else extra.take(6)
}

package ke.co.mototap.features.mechanic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.model.Review
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.repository.AuthRepository
import kotlinx.coroutines.flow.flowOf
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoCardLine
import ke.co.mototap.ui.theme.MotoGreen
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

private const val MOTOTAP_FEE_PERCENT = 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderJobCompletedScreen(
    job: JobRequest?,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var driver by remember { mutableStateOf<UserProfile?>(null) }
    val providerId = job?.mechanicId?.ifBlank { null }
        ?: FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val reviews by remember(providerId) {
        if (providerId.isBlank()) flowOf(emptyList<Review>())
        else authRepository.observeMechanicReviews(providerId)
    }.collectAsState(initial = emptyList<Review>())
    LaunchedEffect(job?.driverId) {
        val id = job?.driverId.orEmpty()
        driver = if (id.isBlank()) null else authRepository.getUserProfile(id)
    }
    val vehicleId = job?.vehicleId.orEmpty()
    val vehicle = remember(driver, job) {
        val vehicles = driver?.vehicles.orEmpty()
        vehicles.firstOrNull { vehicleId.isNotBlank() && it.id == vehicleId }
            ?: vehicles.firstOrNull {
                it.make.equals(job?.vehicleMake, ignoreCase = true) &&
                    it.model.equals(job?.vehicleModel, ignoreCase = true)
            }
            ?: vehicles.firstOrNull()
    }

    val name = driver?.name?.ifBlank { job?.driverName }?.ifBlank { "Customer" }
        ?: job?.driverName.orEmpty().ifBlank { "Customer" }
    val photo = driver?.profilePhotoUrl?.ifBlank { null } ?: job?.driverPhotoUrl?.ifBlank { null }
    val makeModel = listOf(
        vehicle?.make?.ifBlank { job?.vehicleMake } ?: job?.vehicleMake.orEmpty(),
        vehicle?.model?.ifBlank { job?.vehicleModel } ?: job?.vehicleModel.orEmpty(),
    ).filter { it.isNotBlank() }.joinToString(" ").ifBlank { job?.issueType.orEmpty().ifBlank { "Vehicle" } }
    val plate = vehicle?.licensePlate?.ifBlank { null } ?: driver?.numberPlate?.ifBlank { null }
    val vehicleLabel = if (!plate.isNullOrBlank()) "$makeModel ($plate)" else makeModel

    val gross = job?.price ?: 0L
    val fee = (gross * MOTOTAP_FEE_PERCENT + 50) / 100
    val earnings = (gross - fee).coerceAtLeast(0L)
    val paid = job?.status == JobStatus.PAID || job?.status == JobStatus.CLOSED || job?.status == JobStatus.COMPLETED
    val jobReview = remember(reviews, job?.driverId) {
        val driverId = job?.driverId.orEmpty()
        reviews.firstOrNull { driverId.isNotBlank() && it.driverId == driverId }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Job Completed", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(42.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Job Completed", color = MotoInk, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text("Great work!", color = MotoMuted, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp, bottom = 22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, MotoCardLine, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                SummaryRow(
                    icon = Icons.Default.Person,
                    label = "Customer",
                    value = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFF0F0F2)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (!photo.isNullOrBlank()) {
                                    AsyncImage(model = photo, contentDescription = name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Text(name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "C", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    },
                )
                HorizontalDivider(color = MotoCardLine)
                SummaryRow(
                    icon = Icons.Default.DirectionsCar,
                    label = "Vehicle",
                    value = { Text(vehicleLabel, color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                )
                HorizontalDivider(color = MotoCardLine)
                SummaryRow(
                    icon = Icons.Default.Work,
                    label = "Final Amount",
                    value = {
                        Text("KSh ${formatKsh(gross)}", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    },
                )
                HorizontalDivider(color = MotoCardLine)
                SummaryRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "MotoTap Fee ($MOTOTAP_FEE_PERCENT%)",
                    value = {
                        Text("- KSh ${formatKsh(fee)}", color = MotoRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    },
                    iconTint = MotoRed,
                    iconBackground = Color(0xFFFFE8EA),
                )
                HorizontalDivider(color = MotoCardLine)
                SummaryRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Your Earnings",
                    value = {
                        Text("KSh ${formatKsh(earnings)}", color = MotoGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    iconTint = MotoGreen,
                    iconBackground = Color(0xFFDCFCE7),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Payment Status", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (paid) Color(0xFFE8F8EE) else Color(0xFFF4F5F7))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (paid) MotoGreen else MotoMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (paid) "Paid" else "Pending",
                        color = if (paid) MotoGreen else MotoMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Customer Rating", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val stars = jobReview?.rating?.toInt()?.coerceIn(0, 5) ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < stars) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFF5B400),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        jobReview?.comment?.ifBlank { null }
                            ?: if (jobReview != null) "Rated ${"%.0f".format(jobReview.rating)} stars" else "Waiting for customer rating",
                        color = MotoMuted,
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: ImageVector,
    label: String,
    value: @Composable () -> Unit,
    iconTint: Color = MotoRed,
    iconBackground: Color = Color(0xFFFFE8EA),
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = MotoMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            value()
        }
    }
}

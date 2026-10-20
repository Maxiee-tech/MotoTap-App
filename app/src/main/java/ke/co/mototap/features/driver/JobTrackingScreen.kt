package ke.co.mototap.features.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobTrackingScreen(
    onBack: () -> Unit,
    onChat: () -> Unit,
    onJobCompleted: () -> Unit = {},
    onCancel: () -> Unit = {},
    status: JobStatus = JobStatus.ASSIGNED,
    isAdmin: Boolean = false,
    mechanicPhoneNumber: String? = null,
    garageName: String = "",
    locationLabel: String = "",
    serviceName: String = "",
    price: Long = 0L,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var confirmCancel by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val provider = garageName.ifBlank { "the provider" }

    LaunchedEffect(status) {
        if (status == JobStatus.COMPLETED) {
            onJobCompleted()
        }
    }

    val step1Done = true
    val step2Done = status in setOf(
        JobStatus.ASSIGNED, JobStatus.IN_PROGRESS, JobStatus.COMPLETED, JobStatus.PAID, JobStatus.CLOSED,
    )
    val step3Done = status in setOf(JobStatus.IN_PROGRESS, JobStatus.COMPLETED, JobStatus.PAID, JobStatus.CLOSED)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Service Request", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MotoInk)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MotoInk)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Cancel request") },
                                onClick = {
                                    showMenu = false
                                    confirmCancel = true
                                },
                            )
                        }
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            RequestSentHero()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Request Sent", color = MotoInk, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$provider has received\nyour request.",
                color = MotoMuted,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(modifier = Modifier.height(28.dp))
            RequestDetailRow(
                icon = Icons.Default.LocationOn,
                label = "Your location",
                value = formatShopAreaLabel(locationLabel).ifBlank { locationLabel.ifBlank { "Location shared" } },
            )
            HorizontalDivider(color = Color(0xFFF0F0F2))
            RequestDetailRow(
                icon = Icons.Default.DirectionsCar,
                label = "Car problem",
                value = serviceName.ifBlank { "Service request" },
            )
            HorizontalDivider(color = Color(0xFFF0F0F2))
            RequestDetailRow(
                icon = Icons.Outlined.AccountBalanceWallet,
                label = "Estimated price",
                value = if (price > 0) "KSh ${formatKsh(price)}" else "On request",
            )

            Spacer(modifier = Modifier.height(28.dp))
            RequestProgressStepper(step1Done = step1Done, step2Done = step2Done, step3Done = step3Done)

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = {
                    val phone = mechanicPhoneNumber.orEmpty()
                    if (phone.isNotBlank()) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                    }
                },
                enabled = !mechanicPhoneNumber.isNullOrBlank() || isAdmin,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Call Provider", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onChat,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MotoButtonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoInk),
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = MotoRed)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            TextButton(
                onClick = { confirmCancel = true },
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            ) {
                Text("Cancel Request", color = MotoRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("Cancel request?", color = MotoInk) },
            text = { Text("This request will be cancelled and the provider will be notified.", color = MotoMuted) },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    onCancel()
                }) {
                    Text("Cancel request", color = MotoRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) {
                    Text("Keep request", color = MotoMuted)
                }
            },
            containerColor = Color.White,
        )
    }
}

private data class ConfettiSpec(
    val angleDeg: Float,
    val color: Color,
    val delayMs: Int,
    val durationMs: Int,
    val lengthDp: Float,
    val thicknessDp: Float,
)

@Composable
private fun RequestSentHero() {
    val infinite = rememberInfiniteTransition(label = "request_sent_hero")
    val pulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val planeTilt by infinite.animateFloat(
        initialValue = -14f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tilt",
    )
    val planeLift by infinite.animateFloat(
        initialValue = 8f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lift",
    )
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val introScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.4f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "intro",
    )

    Box(
        modifier = Modifier
            .size(180.dp)
            .scale(introScale),
        contentAlignment = Alignment.Center,
    ) {
        ConfettiBurst()
        Box(
            modifier = Modifier
                .size(96.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(Color(0xFFFFE8EA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = MotoRed,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        translationY = planeLift
                        rotationZ = planeTilt
                    },
            )
        }
    }
}

@Composable
private fun ConfettiBurst() {
    val specs = remember {
        listOf(
            ConfettiSpec(20f, MotoRed, 0, 1400, 14f, 5f),
            ConfettiSpec(55f, Color(0xFFFFC107), 120, 1500, 10f, 4f),
            ConfettiSpec(95f, Color(0xFF22C55E), 80, 1300, 12f, 4f),
            ConfettiSpec(140f, MotoRed, 200, 1600, 11f, 5f),
            ConfettiSpec(185f, Color(0xFF60A5FA), 40, 1450, 13f, 4f),
            ConfettiSpec(230f, Color(0xFFFFC107), 160, 1350, 9f, 4f),
            ConfettiSpec(275f, MotoRed, 90, 1550, 12f, 5f),
            ConfettiSpec(318f, Color(0xFF22C55E), 220, 1480, 10f, 4f),
            ConfettiSpec(340f, Color(0xFFFB7185), 60, 1380, 8f, 3f),
            ConfettiSpec(75f, Color(0xFFFB7185), 180, 1520, 8f, 3f),
        )
    }
    val density = LocalDensity.current
    Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        specs.forEach { spec ->
            val infinite = rememberInfiniteTransition(label = "confetti_${spec.angleDeg}")
            val progress by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(spec.durationMs, delayMillis = spec.delayMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "p",
            )
            val spin by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(spec.durationMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "spin",
            )
            val radiusPx = with(density) { (36.dp + 40.dp * progress).toPx() }
            val rad = Math.toRadians(spec.angleDeg.toDouble())
            val x = (cos(rad) * radiusPx).toFloat()
            val y = (sin(rad) * radiusPx).toFloat()
            Box(
                modifier = Modifier
                    .offset { IntOffset(x.toInt(), y.toInt()) }
                    .size(spec.lengthDp.dp, spec.thicknessDp.dp)
                    .rotate(spec.angleDeg + spin)
                    .alpha((1f - progress).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(spec.color),
            )
        }
    }
}

@Composable
private fun RequestDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = MotoMuted, fontSize = 13.sp)
            Text(value, color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun RequestProgressStepper(step1Done: Boolean, step2Done: Boolean, step3Done: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
            StepDot(done = step1Done, current = !step2Done)
            StepLine(modifier = Modifier.weight(1f), filled = step2Done)
            StepDot(done = step2Done, current = step2Done && !step3Done)
            StepLine(modifier = Modifier.weight(1f), filled = step3Done)
            StepDot(done = step3Done, current = false)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Request\nsent", color = MotoInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("Provider\nresponding", color = MotoMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("Service\nconfirmed", color = MotoMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StepDot(done: Boolean, current: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> MotoRed
                    current -> Color(0xFFE8E8EA)
                    else -> Color(0xFFE8E8EA)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun StepLine(modifier: Modifier, filled: Boolean) {
    Box(
        modifier = modifier
            .height(3.dp)
            .background(if (filled) MotoRed else Color(0xFFE8E8EA)),
    )
}

@Composable
fun TrackingItem(label: String, icon: ImageVector, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = MotoInk, fontSize = 18.sp, modifier = Modifier.weight(1f))
    }
}

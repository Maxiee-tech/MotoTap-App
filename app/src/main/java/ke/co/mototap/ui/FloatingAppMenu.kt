package ke.co.mototap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.roundToInt
import ke.co.mototap.core.ui.ContactInfo
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingAppMenu(
    onHome: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onMessages: () -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onLogout: () -> Unit,
    onDelete: (currentPassword: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showContacts by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deletePasswordVisible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    var posX by rememberSaveable(key = "menu_x_v2") { mutableStateOf(Float.NaN) }
    var posY by rememberSaveable(key = "menu_y_v2") { mutableStateOf(Float.NaN) }

    Box(modifier = modifier.fillMaxSize()) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val btnPx = with(density) { 56.dp.toPx() }
        val edgePx = with(density) { 8.dp.toPx() }
        val minX = edgePx
        val minY = WindowInsets.statusBars.getTop(density).toFloat() + edgePx
        val maxX = (constraints.maxWidth - btnPx - edgePx).coerceAtLeast(minX)
        val maxY = (constraints.maxHeight - btnPx - WindowInsets.navigationBars.getBottom(density) - edgePx)
            .coerceAtLeast(minY)
        val defaultY = ((constraints.maxHeight / 4f) - (btnPx / 2f))
            .coerceIn(minY, maxY)

        LaunchedEffect(maxX, maxY, minY, defaultY) {
            if (posX.isNaN() || posY.isNaN()) {
                posX = maxX
                posY = defaultY
            } else {
                posX = posX.coerceIn(minX, maxX)
                posY = posY.coerceIn(minY, maxY)
            }
        }

        val drawX = if (posX.isNaN()) maxX else posX
        val drawY = if (posY.isNaN()) defaultY else posY

        Box(
            modifier = Modifier
                .offset { IntOffset(drawX.roundToInt(), drawY.roundToInt()) }
                .size(56.dp)
                .pointerInput(minX, minY, maxX, maxY) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        var dragged = false
                        var travel = 0f
                        val slop = viewConfiguration.touchSlop
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (!dragged) {
                                    open = true
                                } else {
                                    val mid = (minX + maxX) / 2f
                                    posX = if (posX + btnPx / 2f < mid) minX else maxX
                                    posY = posY.coerceIn(minY, maxY)
                                }
                                break
                            }
                            val delta = change.positionChange()
                            travel += hypot(delta.x, delta.y)
                            if (!dragged && travel > slop) dragged = true
                            if (dragged) {
                                change.consume()
                                posX = (posX + delta.x).coerceIn(minX, maxX)
                                posY = (posY + delta.y).coerceIn(minY, maxY)
                            }
                        }
                    }
                },
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PulsingMenuOutline()
            }
        }
    }

    if (open) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .clickable { open = false },
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 64.dp)
                .width(IntrinsicSize.Max),
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomStart = 0.dp, bottomEnd = 20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 16.dp)
                    .width(IntrinsicSize.Max),
            ) {
                Text(
                    "Menu",
                    color = MotoInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                )
                MenuRow("HOME", Icons.Default.Home) {
                    open = false
                    onHome()
                }
                MenuRow("Request History", Icons.AutoMirrored.Filled.List) {
                    open = false
                    onHistory()
                }
                MenuRow("My Profile", Icons.Default.Person) {
                    open = false
                    onProfile()
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Shortcuts",
                    color = MotoMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                MenuRow("Messages", Icons.Default.Email) {
                    open = false
                    onMessages()
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                MenuRow("Privacy Policy", Icons.Default.Policy) {
                    open = false
                    onPrivacy()
                }
                MenuRow("Terms & Conditions", Icons.Default.Description) {
                    open = false
                    onTerms()
                }
                MenuRow("Contacts", Icons.Default.Call) {
                    open = false
                    showContacts = true
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                MenuRow("Log Out", Icons.AutoMirrored.Filled.ExitToApp) {
                    open = false
                    confirmLogout = true
                }
                MenuRow("DELETE", Icons.Default.Delete, tint = Color(0xFFB91C1C)) {
                    open = false
                    deletePassword = ""
                    deletePasswordVisible = false
                    confirmDelete = true
                }
            }
        }
    }
    }

    if (showContacts) {
        AlertDialog(
            onDismissRequest = { showContacts = false },
            title = {
                Column {
                    Text(
                        ContactInfo.SLOGAN,
                        color = MotoRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                    Text(
                        ContactInfo.HEADING,
                        color = MotoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ContactLinkRow(
                        icon = Icons.Default.LocationOn,
                        label = ContactInfo.COMPANY,
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Email,
                        label = ContactInfo.EMAIL_SUPPORT,
                        onClick = { uriHandler.openUri("mailto:${ContactInfo.EMAIL_SUPPORT}") },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Email,
                        label = ContactInfo.EMAIL_LAB,
                        onClick = { uriHandler.openUri("mailto:${ContactInfo.EMAIL_LAB}") },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Call,
                        label = ContactInfo.PHONE_1,
                        onClick = { uriHandler.openUri("tel:${ContactInfo.PHONE_1_E164}") },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Call,
                        label = ContactInfo.PHONE_2,
                        onClick = { uriHandler.openUri("tel:${ContactInfo.PHONE_2_E164}") },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp ${ContactInfo.PHONE_1}",
                        onClick = { uriHandler.openUri(ContactInfo.WHATSAPP_1) },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp ${ContactInfo.PHONE_2}",
                        onClick = { uriHandler.openUri(ContactInfo.WHATSAPP_2) },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Person,
                        label = "Instagram @mototap_autosystems",
                        onClick = { uriHandler.openUri(ContactInfo.INSTAGRAM) },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Person,
                        label = "X @mototap01",
                        onClick = { uriHandler.openUri(ContactInfo.X) },
                    )
                    ContactLinkRow(
                        icon = Icons.Default.Person,
                        label = "Threads @mototap_autosystems",
                        onClick = { uriHandler.openUri(ContactInfo.THREADS) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showContacts = false }) {
                    Text("Close", color = MotoRed, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Log out?", color = MotoInk) },
            text = { Text("You will need to sign in again to use MotoTap.", color = MotoMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("Cancel", color = MotoMuted)
                }
            },
            containerColor = Color.White,
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = {
                confirmDelete = false
                deletePassword = ""
            },
            title = { Text("Delete Account?", color = MotoInk) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This action is permanent and cannot be undone.", color = MotoMuted)
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = if (deletePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { deletePasswordVisible = !deletePasswordVisible }) {
                                Icon(
                                    if (deletePasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val password = deletePassword
                        confirmDelete = false
                        deletePassword = ""
                        onDelete(password)
                    },
                    enabled = deletePassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                ) {
                    Text("DELETE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    deletePassword = ""
                }) {
                    Text("Cancel", color = MotoMuted)
                }
            },
            containerColor = Color.White,
        )
    }
}

private val MenuOutlineBright = Color(0xFFFFF59D)
private val MenuOutlineDim = Color(0xFFC9A227)

@Composable
private fun PulsingMenuOutline() {
    val infinite = rememberInfiniteTransition(label = "menu_outline")
    val glow by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    val outline = androidx.compose.ui.graphics.lerp(MenuOutlineDim, MenuOutlineBright, glow)
    Box(
        modifier = Modifier
            .size(44.dp)
            .border(width = 2.5.dp, color = outline, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MotoInk)
            }
        }
    }
}

@Composable
private fun MenuRow(
    label: String,
    icon: ImageVector,
    tint: Color = MotoRed,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = if (tint == MotoRed) MotoInk else tint, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun ContactLinkRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = MotoInk, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

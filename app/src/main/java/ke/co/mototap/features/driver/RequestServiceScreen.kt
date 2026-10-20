package ke.co.mototap.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.core.util.getMechanicServicePrice
import ke.co.mototap.core.util.isTowingService
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoCardLine
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceScreen(
    viewModel: DriverHomeViewModel,
    isAdmin: Boolean = false,
    onBack: () -> Unit,
    onSendRequest: (estimatedKm: Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val mechanic = uiState.selectedMechanic
    val service = uiState.issueTypeInput.ifBlank { "Service request" }
    var towingKm by remember { mutableStateOf("") }
    val towing = isTowingService(service)

    LaunchedEffect(Unit) {
        if (uiState.locationInput.isBlank()) {
            viewModel.refreshLocationLabel(context)
        }
    }

    val price = mechanic?.let {
        getMechanicServicePrice(it, service, uiState.activeVehicleMake, uiState.activeVehicleModel)
    }

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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        if (mechanic == null) {
            Box(
                modifier = modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("No provider selected.", color = MotoMuted)
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            RequestLine(
                icon = Icons.Default.Person,
                label = "Provider",
                value = mechanic.name.ifBlank { mechanic.institutionName.ifBlank { "Garage" } },
            )
            RequestLine(
                icon = Icons.Default.Build,
                label = "Service",
                value = service,
            )
            RequestLine(
                icon = Icons.Default.LocationOn,
                label = "Your location",
                value = formatShopAreaLabel(uiState.locationInput).ifBlank {
                    uiState.locationInput.ifBlank { if (uiState.isLocating) "Finding location..." else "Location shared" }
                },
            )
            if (price != null && price > 0) {
                Text(
                    if (towing) "Rate  KSh ${formatKsh(price)}/km" else "Estimated  KSh ${formatKsh(price)}",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            if (towing) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = towingKm,
                    onValueChange = { towingKm = it },
                    label = { Text("Estimated kilometres") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MotoRed,
                        cursorColor = MotoRed,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.descriptionInput,
                onValueChange = viewModel::onDescriptionChanged,
                placeholder = { Text("Add notes for the provider (optional)", color = MotoMuted) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MotoCardLine,
                    unfocusedBorderColor = MotoCardLine,
                    focusedContainerColor = Color(0xFFF7F7F8),
                    unfocusedContainerColor = Color(0xFFF7F7F8),
                    cursorColor = MotoRed,
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    val km = towingKm.toDoubleOrNull()
                    onSendRequest(if (towing) km else null)
                },
                enabled = !uiState.isLocating && (!towing || towingKm.toDoubleOrNull() != null),
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MotoButtonShape,
            ) {
                if (uiState.isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Send Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun RequestLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFFE8EA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = MotoMuted, fontSize = 12.sp)
            Text(value, color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

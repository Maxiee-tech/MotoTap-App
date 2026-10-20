package ke.co.mototap.features.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.core.data.serviceCategoryByName
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.MotoCardShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

data class ServiceGroup(val header: String, val services: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubServiceSelectionScreen(
    categoryName: String,
    viewModel: DriverHomeViewModel,
    onSubServiceSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedServices = getGroupedServicesForCategory(categoryName)
    val allServices = remember(groupedServices) { groupedServices.flatMap { it.services } }
    var selectedService by remember { mutableStateOf(allServices.firstOrNull().orEmpty()) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationLabel = uiState.locationInput.ifBlank { "Nairobi, Kenya" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName.replaceFirstChar { it.uppercase() },
                        color = MotoInk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MotoInk,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MotoInk,
                    navigationIconContentColor = MotoInk,
                ),
            )
        },
        containerColor = Color.White,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.refreshLocationLabel(context) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF7F7F8),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use my location", color = MotoInk, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (uiState.isLocating) "Getting location..." else locationLabel,
                                color = MotoMuted,
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MotoMuted,
                        )
                    }
                }
                Button(
                    onClick = {
                        if (selectedService.isBlank()) return@Button
                        viewModel.onIssueChanged(selectedService)
                        onSubServiceSelected(selectedService)
                    },
                    enabled = selectedService.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                    shape = MotoButtonShape,
                ) {
                    Text("Find Help", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "What do you need help with?",
                    color = MotoInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                )
            }
            groupedServices.forEach { group ->
                if (group.header.isNotEmpty()) {
                    item {
                        Text(
                            text = group.header.replaceFirstChar { it.uppercase() },
                            color = MotoMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                group.services.chunked(2).forEach { row ->
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { name ->
                                ChooseServiceTile(
                                    name = name,
                                    selected = name == selectedService,
                                    onClick = { selectedService = name },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ChooseServiceTile(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFFFE8EA) else Color.White,
        ),
        shape = MotoCardShape,
        border = BorderStroke(1.dp, if (selected) MotoRed.copy(alpha = 0.35f) else Color(0xFFE8E8EA)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = chooseServiceIcon(name),
                contentDescription = null,
                tint = MotoRed,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = name,
                color = MotoInk,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                textAlign = TextAlign.Start,
            )
        }
    }
}

private fun chooseServiceIcon(name: String): ImageVector {
    val key = name.lowercase()
    return when {
        "start" in key || "won't" in key || "wont" in key -> Icons.Default.DirectionsCar
        "engine" in key -> Icons.Default.Settings
        "brake" in key -> Icons.Default.Warning
        "battery" in key -> Icons.Default.BatteryChargingFull
        "tyre" in key || "tire" in key || "wheel" in key -> Icons.Default.TireRepair
        "electric" in key -> Icons.Default.FlashOn
        "other" in key || "general" in key -> Icons.Default.MoreHoriz
        else -> Icons.Default.Build
    }
}

/**
 * Groups for a top-level category, sourced from [ServiceCatalogData] so
 * driver discovery matches the web catalog / Firebase skills.
 */
fun getGroupedServicesForCategory(category: String): List<ServiceGroup> {
    val catalogCategory = serviceCategoryByName(category)
        ?: return listOf(ServiceGroup("", listOf("General Request")))

    val multiGroup = catalogCategory.groups.size > 1
    return catalogCategory.groups.map { group ->
        val hideHeader = !multiGroup &&
            (group.title.equals(catalogCategory.name, ignoreCase = true) || group.title.isBlank())
        val header = when {
            hideHeader -> ""
            else -> group.title
        }
        ServiceGroup(header, group.items)
    }
}

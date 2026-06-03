package com.example.mototap.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed
import com.example.mototap.ui.BottomNavigationBar

data class ServiceItem(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    viewModel: DriverHomeViewModel,
    userProfile: com.example.mototap.core.model.UserProfile?,
    onCategorySelected: (String) -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val activeVehicle = userProfile?.vehicles?.firstOrNull()

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MOTO TAP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MotoRed
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onNavigate = { route: String ->
                    when(route) {
                        "requests" -> onNavigateToRequests()
                        "messages" -> onNavigateToMessages()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Vehicle Profile Section (Step 1)
                item {
                    VehicleProfileCard(
                        vehicleName = activeVehicle?.let { "${it.make} ${it.model}" } ?: "No active vehicle",
                        licensePlate = activeVehicle?.licensePlate ?: "N/A",
                        mileage = activeVehicle?.mileage?.let { "$it km" } ?: "0 km",
                        onEditClick = onNavigateToProfile
                    )
                }

                item {
                    ServiceCategorySection(
                        title = stringResource(R.string.emergency_services),
                        services = listOf(
                            ServiceItem(stringResource(R.string.road_assistance), Icons.Default.Warning),
                            ServiceItem(stringResource(R.string.towing_services), Icons.Default.Build),
                            ServiceItem(stringResource(R.string.mobile_mechanic), Icons.Default.Settings),
                            ServiceItem(stringResource(R.string.emergency_tire), Icons.Default.Info),
                            ServiceItem(stringResource(R.string.emergency_battery), Icons.Default.Face)
                        ),
                        onCategoryClick = { onCategorySelected(it) }
                    )
                }

                item {
                    ServiceCategorySection(
                        title = stringResource(R.string.maintenance_services),
                        services = listOf(
                            ServiceItem(stringResource(R.string.garage_services), Icons.Default.Home),
                            ServiceItem(stringResource(R.string.preventive_maintenance), Icons.Default.Refresh),
                            ServiceItem(stringResource(R.string.vehicle_diagnostics), Icons.Default.Search),
                            ServiceItem(stringResource(R.string.auto_electrical), Icons.Default.Info),
                            ServiceItem(stringResource(R.string.ac_services), Icons.Default.Star),
                            ServiceItem(stringResource(R.string.tire_wheel_services), Icons.Default.Settings)
                        ),
                        onCategoryClick = { onCategorySelected(it) }
                    )
                }

                item {
                    ServiceCategorySection(
                        title = stringResource(R.string.upgrades_value_added),
                        services = listOf(
                            ServiceItem(stringResource(R.string.car_wash), Icons.Default.ShoppingCart),
                            ServiceItem(stringResource(R.string.car_body_cosmetic), Icons.Default.Edit),
                            ServiceItem(stringResource(R.string.car_customization), Icons.Default.Add)
                        ),
                        onCategoryClick = { onCategorySelected(it) }
                    )
                }
            }

            if (uiState.isLocating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MotoRed)
                }
            }
        }
    }
}

@Composable
fun VehicleProfileCard(
    vehicleName: String,
    licensePlate: String,
    mileage: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ACTIVE VEHICLE PROFILE",
                    color = MotoRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vehicleName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(text = "Plate: $licensePlate", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Mileage: $mileage", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Vehicle",
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ServiceCategorySection(
    title: String,
    services: List<ServiceItem>,
    onCategoryClick: (String) -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            services.forEach { service ->
                ServiceRow(
                    service = service,
                    onRowClick = { onCategoryClick(service.name) }
                )
            }
        }
    }
}

@Composable
fun ServiceRow(
    service: ServiceItem,
    onRowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onRowClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.name,
                    tint = MotoRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = service.name,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MotoRed,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.core.data.SERVICE_DISPLAY_GROUP_ORDER
import com.example.mototap.core.data.serviceCategoriesForDisplayGroup
import com.example.mototap.ui.theme.MotoBlue
import com.example.mototap.ui.theme.MotoRed
import com.example.mototap.ui.BottomNavigationBar

data class ServiceItem(val name: String, val icon: ImageVector)

fun serviceCategoryIcon(categoryId: String): ImageVector = when (categoryId) {
    "road-assistance" -> Icons.Default.Warning
    "towing-services" -> Icons.Default.Build
    "mobile-mechanic" -> Icons.Default.Settings
    "emergency-tire-services" -> Icons.Default.Info
    "emergency-battery-services" -> Icons.Default.Face
    "garage-services" -> Icons.Default.Home
    "preventive-routine-maintenance" -> Icons.Default.Refresh
    "vehicle-diagnostics" -> Icons.Default.Search
    "auto-electrical-services" -> Icons.Default.Info
    "ac-services" -> Icons.Default.Star
    "tire-wheel-services" -> Icons.Default.Settings
    "car-wash-services" -> Icons.Default.ShoppingCart
    "car-body-cosmetic-services" -> Icons.Default.Edit
    "car-customization-upgrades" -> Icons.Default.Add
    "vehicle-electronics-security" -> Icons.Default.Key
    else -> Icons.Default.Settings
}

fun partsCategoryIcon(categoryId: String): ImageVector = when (categoryId) {
    "engine-drivetrain" -> Icons.Default.Settings
    "brakes" -> Icons.Default.Warning
    "electrical" -> Icons.Default.Info
    "vehicle-electronics-security" -> Icons.Default.Lock
    "filters-fluids" -> Icons.Default.Refresh
    "body-exterior" -> Icons.Default.DirectionsCar
    "tyres-wheels" -> Icons.Default.Build
    else -> Icons.Default.ShoppingCart
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    viewModel: DriverHomeViewModel,
    userProfile: com.example.mototap.core.model.UserProfile?,
    onCategorySelected: (String) -> Unit,
    onPartsCategorySelected: (String) -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isMechanicsMode = uiState.marketplaceMode == DriverMarketplaceMode.MECHANICS

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
                    MarketplaceModeToggle(
                        isMechanicsMode = isMechanicsMode,
                        onMechanicsSelected = { viewModel.setMarketplaceMode(DriverMarketplaceMode.MECHANICS) },
                        onPartsDealersSelected = { viewModel.setMarketplaceMode(DriverMarketplaceMode.PARTS_DEALERS) },
                    )
                }

                if (isMechanicsMode) {
                    SERVICE_DISPLAY_GROUP_ORDER.forEach { displayGroup ->
                        item {
                            ServiceCategorySection(
                                title = displayGroup,
                                services = serviceCategoriesForDisplayGroup(displayGroup).map { category ->
                                    ServiceItem(category.name, serviceCategoryIcon(category.id))
                                },
                                onCategoryClick = { onCategorySelected(it) }
                            )
                        }
                    }
                } else {
                    item {
                        ServiceCategorySection(
                            title = "Spare Parts",
                            services = PARTS_CATEGORIES.map { category ->
                                ServiceItem(category.name, partsCategoryIcon(category.id))
                            },
                            onCategoryClick = { onPartsCategorySelected(it) }
                        )
                    }
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
fun MarketplaceModeToggle(
    isMechanicsMode: Boolean,
    onMechanicsSelected: () -> Unit,
    onPartsDealersSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MarketplaceToggleButton(
            label = "MECHANICS",
            isActive = isMechanicsMode,
            onClick = onMechanicsSelected,
            modifier = Modifier.weight(1f)
        )
        MarketplaceToggleButton(
            label = "PARTS DEALERS",
            isActive = !isMechanicsMode,
            onClick = onPartsDealersSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarketplaceToggleButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MotoBlue.copy(alpha = if (isActive) 1f else 0.4f),
            contentColor = Color.White,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MotoBlue),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            letterSpacing = 0.6.sp,
            maxLines = 1,
        )
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

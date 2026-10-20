package ke.co.mototap.features.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ke.co.mototap.core.data.SERVICE_DISPLAY_GROUP_ORDER
import ke.co.mototap.core.data.serviceCategoriesForDisplayGroup
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.model.VerificationStatus
import ke.co.mototap.core.ui.ContactInfo
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.ui.BottomNavigationBar
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoCardShape
import ke.co.mototap.ui.theme.MotoGreen
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import ke.co.mototap.ui.theme.MotoScreen

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
    userProfile: ke.co.mototap.core.model.UserProfile?,
    onCategorySelected: (String) -> Unit,
    onPartsCategorySelected: (String) -> Unit = {},
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = onNavigateToRequests,
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
            TopAppBar(
                title = { BrandLogo(modifier = Modifier.height(26.dp)) },
                actions = {
                    ke.co.mototap.ui.NotificationBellButton(onClick = onNavigateToNotifications)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MotoInk,
                    navigationIconContentColor = MotoInk,
                    actionIconContentColor = MotoInk
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                onNavigate = { route: String ->
                    when(route) {
                        "requests" -> onNavigateToRequests()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = MotoScreen
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    DriverHomeProfileHeader(
                        profile = userProfile,
                        onOpenProfile = onNavigateToProfile,
                    )
                }

                item {
                    VehicleProfileCard(
                        vehicleName = activeVehicle?.let { "${it.make} ${it.model}" } ?: "No active vehicle",
                        licensePlate = activeVehicle?.licensePlate ?: "N/A",
                        mileage = activeVehicle?.mileage?.let { "$it km" } ?: "0 km",
                        onEditClick = onNavigateToProfile
                    )
                }

                item {
                    Text(
                        text = "What does your car need?",
                        color = MotoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
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

                item {
                    OfficeLocationMap()
                }
                item {
                    Text(
                        text = "Your activity",
                        color = MotoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                    )
                }
                item {
                    DriverHomeActivity(
                        jobs = uiState.jobs,
                        onSeeAllJobs = onNavigateToRequests,
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
private fun OfficeLocationMap() {
    val context = LocalContext.current
    val office = LatLng(ContactInfo.OFFICE_LAT, ContactInfo.OFFICE_LNG)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(office, 17f)
    }
    val markerState = remember { MarkerState(position = office) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Location",
            color = MotoInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
        )
        Text(
            text = "Where our office is located.",
            color = MotoMuted,
            fontSize = 14.sp,
        )
        Text(
            text = ContactInfo.OFFICE_ADDRESS,
            color = MotoInk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                ),
            ) {
                Marker(
                    state = markerState,
                    title = "MotoTap Autosystems",
                    snippet = ContactInfo.OFFICE_ADDRESS,
                )
            }
        }
        TextButton(
            onClick = {
                val query = Uri.encode("${ContactInfo.OFFICE_ADDRESS}, Nairobi")
                val uri = Uri.parse(
                    "geo:${ContactInfo.OFFICE_LAT},${ContactInfo.OFFICE_LNG}?q=$query",
                )
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            },
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MotoRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Open in Google Maps", color = MotoRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DriverHomeProfileHeader(
    profile: UserProfile?,
    onOpenProfile: () -> Unit,
) {
    val photo = profile?.profilePhotoUrl?.ifBlank { null }
        ?: profile?.vehiclePhotoUrl?.ifBlank { null }
    val location = profile?.locationName?.ifBlank { profile.address }?.ifBlank { "Kenya" } ?: "Kenya"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenProfile),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MotoCardShape,
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6),
                ) {
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = profile?.name,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MotoRed,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            profile?.name?.ifBlank { "Driver" } ?: "Driver",
                            color = MotoInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                        if (profile?.status == VerificationStatus.VERIFIED) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MotoGreen,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Text("Driver", color = MotoMuted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, color = MotoMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = {},
                label = { Text("Active") },
                leadingIcon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFE8F8EE),
                    labelColor = MotoGreen,
                    leadingIconContentColor = MotoGreen,
                ),
            )
            Text("Ready to request help", color = MotoMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DriverHomeActivity(
    jobs: List<JobRequest>,
    onSeeAllJobs: () -> Unit,
) {
    val completed = jobs.filter {
        it.status == JobStatus.COMPLETED || it.status == JobStatus.PAID || it.status == JobStatus.CLOSED
    }
    val ongoing = jobs.filter {
        it.status == JobStatus.ASSIGNED || it.status == JobStatus.IN_PROGRESS || it.status == JobStatus.MATCHING
    }
    val open = jobs.filter {
        it.status == JobStatus.REQUESTED || it.status == JobStatus.INQUIRY
    }
    val startOfDay = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val spentToday = completed.filter { it.createdAtMillis >= startOfDay }.sumOf { it.price }
    val recent = jobs.sortedByDescending { it.createdAtMillis }.take(4)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DriverStatCard("Today's spend", "KSh ${formatKsh(spentToday)}", Modifier.weight(1f))
            DriverStatCard("Jobs completed", completed.size.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DriverStatCard("New requests", open.size.toString(), Modifier.weight(1f))
            DriverStatCard("Ongoing jobs", ongoing.size.toString(), Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recent Jobs", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            TextButton(onClick = onSeeAllJobs) {
                Text("See all", color = MotoRed, fontWeight = FontWeight.Bold)
            }
        }
        if (recent.isEmpty()) {
            Text("No recent jobs yet", color = MotoMuted, fontSize = 13.sp)
        } else {
            recent.forEach { job ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onSeeAllJobs),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                job.issueType.ifBlank { "Service" },
                                color = MotoInk,
                                fontWeight = FontWeight.Bold,
                            )
                            Text("KSh ${formatKsh(job.price)}", color = MotoMuted, fontSize = 12.sp)
                        }
                        val done = job.status == JobStatus.COMPLETED || job.status == JobStatus.PAID
                        Text(
                            if (done) "Completed" else job.status.name.replace('_', ' ').lowercase()
                                .replaceFirstChar { it.titlecase() },
                            color = if (done) MotoGreen else MotoRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = MotoMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
            containerColor = if (isActive) MotoRed else Color.White,
            contentColor = if (isActive) Color.White else MotoInk,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) MotoRed else Color(0xFFE5E7EB)),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    text = "My Car",
                    color = MotoRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vehicleName,
                    color = MotoInk,
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
            color = MotoInk,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            services.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { service ->
                        ServiceRow(
                            service = service,
                            onRowClick = { onCategoryClick(service.name) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ServiceRow(
    service: ServiceItem,
    onRowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onRowClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MotoCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = service.name,
                tint = MotoRed,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = service.name,
                color = MotoInk,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
            )
        }
    }
}

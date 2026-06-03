package com.example.mototap.features.mechanic

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mototap.R
import com.example.mototap.core.model.JobStatus
import com.example.mototap.ui.BottomNavigationBar
import com.example.mototap.ui.theme.MotoRed
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: MechanicDashboardViewModel,
    onBack: () -> Unit,
    onSubmitQuote: () -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var showServiceSelection by remember { mutableStateOf(false) }
    var showLocationSelection by remember { mutableStateOf(false) }

    // Force selection logic
    LaunchedEffect(uiState.selectedSkills, uiState.latitude, uiState.longitude) {
        if (uiState.selectedSkills.isEmpty()) {
            showServiceSelection = true
            showLocationSelection = false
        } else if (uiState.latitude == null || uiState.longitude == null) {
            showLocationSelection = true
            showServiceSelection = false
        }
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val snackbarHostState = remember { SnackbarHostState() }

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
                        text = when {
                            showServiceSelection -> "CHOOSE SERVICES"
                            showLocationSelection -> "SET GARAGE LOCATION"
                            else -> stringResource(R.string.provider_dashboard)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    val canGoBack = (!showServiceSelection || uiState.selectedSkills.isNotEmpty()) &&
                                    (!showLocationSelection || (uiState.latitude != null && uiState.longitude != null))
                    
                    if (canGoBack) {
                        IconButton(onClick = { 
                            when {
                                showLocationSelection -> showLocationSelection = false
                                showServiceSelection -> showServiceSelection = false
                                else -> onBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    if (!showServiceSelection && !showLocationSelection) {
                        IconButton(onClick = { showLocationSelection = true }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Update Location", tint = Color.White)
                        }
                        IconButton(onClick = { showServiceSelection = true }) {
                            Icon(Icons.Default.Build, contentDescription = "Manage Services", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        bottomBar = {
            if (!showServiceSelection && !showLocationSelection) {
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
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        when {
            showServiceSelection -> {
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    MechanicServiceSelection(
                        selectedSkills = uiState.selectedSkills,
                        onSkillToggled = { viewModel.toggleSkill(it) },
                        modifier = Modifier.padding(bottom = 80.dp)
                    )
                    
                    if (uiState.selectedSkills.isNotEmpty()) {
                        Button(
                            onClick = { 
                                showServiceSelection = false
                                if (uiState.latitude == null || uiState.longitude == null) {
                                    showLocationSelection = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SAVE & CONTINUE", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Please select at least one service to continue",
                                color = Color.White,
                                modifier = Modifier.padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            showLocationSelection -> {
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    val nairobi = LatLng(-1.286389, 36.817223)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(
                            if (uiState.latitude != null && uiState.longitude != null) 
                                LatLng(uiState.latitude!!, uiState.longitude!!) 
                            else nairobi,
                            15f
                        )
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    isMyLocationEnabled = locationPermissionGranted,
                                    mapType = MapType.NORMAL,
                                    isTrafficEnabled = true
                                ),
                                uiSettings = MapUiSettings(
                                    myLocationButtonEnabled = locationPermissionGranted,
                                    zoomControlsEnabled = true
                                )
                            ) {
                                uiState.latitude?.let { lat ->
                                    uiState.longitude?.let { lon ->
                                        Marker(state = MarkerState(position = LatLng(lat, lon)))
                                    }
                                }
                            }
                            
                            if (!locationPermissionGranted) {
                                Button(
                                    onClick = { 
                                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                    },
                                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Text("Enable My Location")
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            tonalElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Tap the map or move the camera to your garage location and click 'Confirm'",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateLocation(
                                            cameraPositionState.position.target.latitude,
                                            cameraPositionState.position.target.longitude
                                        )
                                        showLocationSelection = false
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("CONFIRM GARAGE LOCATION", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MotoRed.copy(alpha = 0.1f)),
                                onClick = { showServiceSelection = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = MotoRed)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "SERVICES YOU OFFER",
                                            color = MotoRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "${uiState.selectedSkills.size} services selected",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("EDIT", color = MotoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                                onClick = { showLocationSelection = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "GARAGE LOCATION",
                                            color = MotoRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = if (uiState.latitude != null) "Location Pinned" else "Not set",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("UPDATE", color = MotoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.new_requests),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (uiState.openJobs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.selectedSkills.isEmpty()) 
                                            "Select services to see matching requests" 
                                            else "No matching requests nearby",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(uiState.openJobs, key = { it.id }) { job ->
                                RequestItem(
                                    label = "${job.issueType} - ${job.locationLabel}",
                                    actionText = "ACCEPT",
                                    onActionClick = { viewModel.acceptJob(job.id, currentUserId) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.ongoing_jobs),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (uiState.ongoingJobs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No ongoing jobs",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(uiState.ongoingJobs, key = { it.id }) { job ->
                                RequestItem(
                                    label = "${job.issueType} (${job.status})",
                                    actionText = "VIEW",
                                    onActionClick = { onSubmitQuote() }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status Actions for the first ongoing job
                    val firstJob = uiState.ongoingJobs.firstOrNull()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardButton(
                            text = stringResource(R.string.on_the_way),
                            onClick = { firstJob?.let { viewModel.updateStatus(it.id, JobStatus.MATCHING) } },
                            enabled = firstJob != null && firstJob.status == JobStatus.ASSIGNED
                        )
                        DashboardButton(
                            text = stringResource(R.string.in_progress),
                            onClick = { firstJob?.let { viewModel.updateStatus(it.id, JobStatus.IN_PROGRESS) } },
                            enabled = firstJob != null && (firstJob.status == JobStatus.ASSIGNED || firstJob.status == JobStatus.MATCHING)
                        )
                        DashboardButton(
                            text = stringResource(R.string.complete),
                            onClick = { firstJob?.let { viewModel.updateStatus(it.id, JobStatus.COMPLETED) } },
                            enabled = firstJob != null && firstJob.status == JobStatus.IN_PROGRESS
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MechanicServiceSelection(
    selectedSkills: List<String>,
    onSkillToggled: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        ServiceCategory(
            title = stringResource(R.string.road_assistance),
            services = listOf(
                stringResource(R.string.jumpstart),
                stringResource(R.string.fuel_delivery),
                stringResource(R.string.lockout_assistance)
            )
        ),
        ServiceCategory(
            title = stringResource(R.string.towing_services),
            services = listOf(
                stringResource(R.string.flatbed_towing),
                stringResource(R.string.wheel_lift_towing),
                stringResource(R.string.dolly_towing),
                stringResource(R.string.accident_towing),
                stringResource(R.string.breakdown_towing),
                stringResource(R.string.long_distance_towing),
                stringResource(R.string.off_road_recovery),
                stringResource(R.string.motorcycle_towing),
                stringResource(R.string.heavy_vehicle_towing),
                stringResource(R.string.low_clearance_towing)
            )
        ),
        ServiceCategory(
            title = stringResource(R.string.mobile_mechanic),
            services = listOf(
                stringResource(R.string.onsite_diagnostics),
                stringResource(R.string.battery_electrical_check),
                stringResource(R.string.engine_fault_id),
                stringResource(R.string.battery_replacement),
                stringResource(R.string.spark_plug_replacement),
                stringResource(R.string.belt_replacement),
                stringResource(R.string.hose_leak_fixes),
                stringResource(R.string.overheating_assistance),
                stringResource(R.string.brake_fix_temporary),
                stringResource(R.string.engine_wont_start),
                stringResource(R.string.oil_topup_change),
                stringResource(R.string.coolant_refill),
                stringResource(R.string.brake_fluid_topup),
                stringResource(R.string.puncture_repair),
                stringResource(R.string.tire_change)
            )
        ),
        ServiceCategory(
            title = stringResource(R.string.garage_services),
            services = listOf(
                stringResource(R.string.engine_overhaul),
                stringResource(R.string.timing_belt_replacement),
                stringResource(R.string.fuel_system_repair),
                stringResource(R.string.exhaust_system_repair),
                stringResource(R.string.gearbox_repair),
                stringResource(R.string.clutch_replacement),
                stringResource(R.string.transmission_fluid_service),
                stringResource(R.string.brake_pad_replacement),
                stringResource(R.string.disc_skimming),
                stringResource(R.string.full_brake_system_repair),
                stringResource(R.string.shock_absorber_replacement),
                stringResource(R.string.steering_rack_repair),
                stringResource(R.string.wheel_alignment),
                stringResource(R.string.wiring_overhaul),
                stringResource(R.string.ecu_repair),
                stringResource(R.string.alternator_starter_repair),
                stringResource(R.string.ac_repair_servicing),
                stringResource(R.string.radiator_repair),
                stringResource(R.string.cooling_system_flush)
            )
        ),
        ServiceCategory(
            title = stringResource(R.string.car_wash),
            services = listOf(
                stringResource(R.string.exterior_wash),
                stringResource(R.string.interior_vacuum),
                stringResource(R.string.tire_cleaning),
                stringResource(R.string.exterior_interior_cleaning),
                stringResource(R.string.dashboard_polish),
                stringResource(R.string.window_cleaning),
                stringResource(R.string.full_car_detailing),
                stringResource(R.string.engine_cleaning),
                stringResource(R.string.underbody_wash),
                stringResource(R.string.seat_shampoo),
                stringResource(R.string.leather_conditioning),
                stringResource(R.string.odor_removal),
                stringResource(R.string.waxing_polishing),
                stringResource(R.string.ceramic_coating),
                stringResource(R.string.headlight_restoration)
            )
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Select the services you can offer. Requests matching these will appear in your queue.",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(categories) { category ->
            ExpandableCategory(
                category = category,
                selectedSkills = selectedSkills,
                onSkillToggled = onSkillToggled
            )
        }
    }
}

data class ServiceCategory(val title: String, val services: List<String>)

@Composable
fun ExpandableCategory(
    category: ServiceCategory,
    selectedSkills: List<String>,
    onSkillToggled: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedInCategory = category.services.count { selectedSkills.contains(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title.uppercase(),
                    color = if (selectedInCategory > 0) MotoRed else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (selectedInCategory > 0) {
                    Text(
                        text = "$selectedInCategory selected",
                        color = MotoRed.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        if (expanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                category.services.forEach { service ->
                    val isSelected = selectedSkills.contains(service)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSkillToggled(service) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = service,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MotoRed,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Transparent, CircleShape)
                                    .border(1.dp, Color.DarkGray, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(label: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = Color.White, fontSize = 14.sp, maxLines = 1)
        }
        Button(
            onClick = onActionClick,
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(text = actionText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MotoRed,
            disabledContainerColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = text, color = if (enabled) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
    }
}

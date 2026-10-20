package ke.co.mototap.features.mechanic

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.content.Context
import ke.co.mototap.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import ke.co.mototap.core.model.JobRequest
import ke.co.mototap.core.model.JobStatus
import ke.co.mototap.core.util.JobAdditionalServices
import ke.co.mototap.features.jobs.JobAdditionalServicesSection
import ke.co.mototap.core.util.allSelectedServicesPriced
import ke.co.mototap.core.util.isTowingService
import ke.co.mototap.core.util.lookupFlatPrice
import ke.co.mototap.core.util.lookupVehiclePrices
import ke.co.mototap.core.util.parsePriceInput
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.model.VerificationStatus
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.getOpenClosedStatus
import ke.co.mototap.features.auth.LocationNamePicker
import ke.co.mototap.ui.BottomNavVariant
import ke.co.mototap.ui.BottomNavigationBar
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoCardShape
import ke.co.mototap.ui.theme.MotoGreen
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import ke.co.mototap.ui.theme.MotoScreen
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import coil3.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: MechanicDashboardViewModel,
    onBack: () -> Unit,
    onSubmitQuote: () -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToEarnings: () -> Unit = onNavigateToRequests,
    onIncomingRequest: (String) -> Unit = {},
    userProfile: UserProfile? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var showServiceSelection by remember { mutableStateOf(false) }
    var showLocationSelection by remember { mutableStateOf(false) }
    var showGarageCatalog by remember { mutableStateOf(false) }
    var draftLocationName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.locationName) {
        draftLocationName = uiState.locationName
    }

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
    val garagePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.updateGarageProfilePhoto(context, it) }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val onSetupScreen = showGarageCatalog || showServiceSelection || showLocationSelection
            CenterAlignedTopAppBar(
                title = {
                    if (onSetupScreen) {
                        Text(
                            text = when {
                                showGarageCatalog -> "GARAGE CATALOG"
                                showServiceSelection -> "CHOOSE SERVICES"
                                else -> "SET GARAGE LOCATION"
                            },
                            color = MotoInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    } else {
                        BrandLogo(modifier = Modifier.height(26.dp))
                    }
                },
                navigationIcon = {
                    if (onSetupScreen) {
                        IconButton(onClick = {
                            when {
                                showGarageCatalog -> showGarageCatalog = false
                                showLocationSelection -> showLocationSelection = false
                                showServiceSelection -> showServiceSelection = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MotoInk
                            )
                        }
                    }
                },
                actions = {
                    if (!onSetupScreen) {
                        IconButton(
                            onClick = {
                                uiState.openJobs.firstOrNull()?.id?.let(onIncomingRequest)
                                    ?: onNavigateToRequests()
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uiState.openJobs.isNotEmpty()) {
                                        Badge(containerColor = MotoRed) {
                                            Text("${uiState.openJobs.size.coerceAtMost(9)}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = MotoInk)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!showServiceSelection && !showLocationSelection && !showGarageCatalog) {
                BottomNavigationBar(
                    currentRoute = "home",
                    variant = BottomNavVariant.Provider,
                    onNavigate = { route: String ->
                        when (route) {
                            "requests" -> onNavigateToRequests()
                            "earnings" -> onNavigateToEarnings()
                            "profile" -> onNavigateToProfile()
                        }
                    }
                )
            }
        },
        containerColor = MotoScreen
    ) { paddingValues ->
        when {
            showGarageCatalog -> {
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    MechanicServiceSelection(
                        selectedSkills = uiState.garageSelectedSkills,
                        servicePrices = uiState.garageServicePrices,
                        vehiclePrices = uiState.garageVehiclePrices,
                        onSkillToggled = { viewModel.toggleGarageSkill(it) },
                        onPriceChanged = { service, price -> viewModel.setGaragePrice(service, price) },
                        onVehicleRateChanged = { service, make, model, price ->
                            viewModel.setGarageVehicleRate(service, make, model, price)
                        },
                        onVehicleRateRemoved = { service, make, model ->
                            viewModel.removeGarageVehicleRate(service, make, model)
                        },
                        modifier = Modifier.padding(bottom = 80.dp),
                        showPrices = true,
                        introText = "Optionally set shop-wide make/model prices. Towing rates are per kilometre (KSh/km) by make and model. Drivers see a mechanic's personal price when set; otherwise these garage prices apply.",
                        header = {
                            GarageVehicleTypesSection(
                                selectedIds = uiState.garageVehicleTypes,
                                onTypeToggled = { viewModel.toggleGarageVehicleType(it) },
                                onSelectAll = { viewModel.setAllGarageVehicleTypes(it) },
                            )
                        },
                    )

                    val canSave = uiState.garageSelectedSkills.isNotEmpty() &&
                        !uiState.isSavingGarageCatalog

                    Button(
                        onClick = { viewModel.saveGarageCatalog { showGarageCatalog = false } },
                        enabled = canSave,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (uiState.isSavingGarageCatalog) "SAVING..." else "SAVE GARAGE PRICES",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            showServiceSelection -> {
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    MechanicServiceSelection(
                        selectedSkills = uiState.selectedSkills,
                        servicePrices = uiState.servicePrices,
                        vehiclePrices = uiState.serviceVehiclePrices,
                        onSkillToggled = { viewModel.toggleSkill(it) },
                        onPriceChanged = { service, price -> viewModel.setServicePrice(service, price) },
                        onVehicleRateChanged = { service, make, model, price ->
                            viewModel.setServiceVehicleRate(service, make, model, price)
                        },
                        onVehicleRateRemoved = { service, make, model ->
                            viewModel.removeServiceVehicleRate(service, make, model)
                        },
                        modifier = Modifier.padding(bottom = 80.dp),
                        showPrices = !uiState.isGarageMember,
                    )

                    val canSaveServices = uiState.selectedSkills.isNotEmpty() &&
                        !uiState.isSavingServices

                    if (canSaveServices) {
                        Button(
                            onClick = {
                                viewModel.saveMechanicServices {
                                    showServiceSelection = false
                                    if (uiState.latitude == null || uiState.longitude == null) {
                                        showLocationSelection = true
                                    }
                                }
                            },
                            enabled = !uiState.isSavingServices,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (uiState.isSavingServices) "SAVING..." else "SAVE & CONTINUE",
                                fontWeight = FontWeight.Bold
                            )
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
                                color = MotoInk,
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
                                    colors = ButtonDefaults.buttonColors(containerColor = MotoScreen.copy(alpha = 0.6f))
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
                                    "Tap the map or move the camera to your garage location, then pick a popular place drivers will see.",
                                    color = MotoInk,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                LocationNamePicker(
                                    locationName = draftLocationName,
                                    onLocationNameChange = { draftLocationName = it },
                                    latitude = uiState.latitude
                                        ?: cameraPositionState.position.target.latitude,
                                    longitude = uiState.longitude
                                        ?: cameraPositionState.position.target.longitude,
                                    onPlacePicked = { place ->
                                        draftLocationName = place.name
                                    },
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateLocation(
                                            cameraPositionState.position.target.latitude,
                                            cameraPositionState.position.target.longitude,
                                            draftLocationName,
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
                val listState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                var highlightGarageJobs by remember { mutableStateOf(false) }
                val prefs = remember {
                    context.getSharedPreferences("mototap_garage_jobs", Context.MODE_PRIVATE)
                }
                val seenKey = "seen_${currentUserId}_${uiState.garageId}"
                val initKey = "init_${currentUserId}_${uiState.garageId}"

                var showNewJobAlert by remember { mutableStateOf(false) }
                var unseenCount by remember { mutableIntStateOf(0) }

                LaunchedEffect(uiState.garageJobs, uiState.garageId, currentUserId) {
                    if (uiState.garageId.isBlank() || currentUserId.isBlank()) {
                        showNewJobAlert = false
                        unseenCount = 0
                        return@LaunchedEffect
                    }
                    val currentIds = uiState.garageJobs.map { it.id }.filter { it.isNotBlank() }
                    val initialized = prefs.getBoolean(initKey, false)
                    if (!initialized) {
                        prefs.edit()
                            .putStringSet(seenKey, currentIds.toSet())
                            .putBoolean(initKey, true)
                            .apply()
                        showNewJobAlert = false
                        unseenCount = 0
                        return@LaunchedEffect
                    }
                    val seen = prefs.getStringSet(seenKey, emptySet()) ?: emptySet()
                    val unseen = currentIds.filter { it !in seen }
                    unseenCount = unseen.size
                    showNewJobAlert = unseen.isNotEmpty()
                }

                fun dismissNewJobAlertAndFocus() {
                    val currentIds = uiState.garageJobs.map { it.id }.filter { it.isNotBlank() }
                    val seen = (prefs.getStringSet(seenKey, emptySet()) ?: emptySet()).toMutableSet()
                    seen.addAll(currentIds)
                    prefs.edit().putStringSet(seenKey, seen).apply()
                    showNewJobAlert = false
                    unseenCount = 0
                    highlightGarageJobs = true
                    scope.launch {
                        // After alert is dismissed: services(0), location(1),
                        // owner extras (prices+invite), then garage jobs header.
                        val targetIndex = if (uiState.isGarageOwner) 4 else 2
                        listState.animateScrollToItem(targetIndex)
                        kotlinx.coroutines.delay(2200)
                        highlightGarageJobs = false
                    }
                }

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(key = "provider_home_header") {
                            ProviderHomeHeader(
                                profile = userProfile,
                                uiState = uiState,
                                onSeeAllJobs = onNavigateToRequests,
                                onOpenJob = onSubmitQuote,
                                onNewRequests = {
                                    uiState.openJobs.firstOrNull()?.id?.let(onIncomingRequest)
                                        ?: onNavigateToRequests()
                                },
                            )
                        }

                        if (showNewJobAlert && uiState.isGarageMember) {
                            item(key = "new_job_alert") {
                                NewGarageJobAlertBanner(
                                    count = unseenCount,
                                    onClick = {
                                        dismissNewJobAlertAndFocus()
                                        val jobId = uiState.openJobs.firstOrNull()?.id
                                            ?: uiState.garageJobs.firstOrNull()?.id
                                        if (jobId != null) onIncomingRequest(jobId)
                                    },
                                )
                            }
                        }

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
                                            text = when {
                                                uiState.selectedSkills.isEmpty() -> "No services selected"
                                                else -> "${uiState.selectedSkills.size} service(s) selected"
                                            },
                                            color = MotoInk,
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                            text = uiState.locationName.ifBlank {
                                                if (uiState.latitude != null) "Location pinned" else "Not set"
                                            },
                                            color = MotoInk,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("UPDATE", color = MotoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        if (uiState.isGarageOwner || uiState.garageId.isBlank()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "LOCATION NAME",
                                            color = MotoRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LocationNamePicker(
                                            locationName = draftLocationName,
                                            onLocationNameChange = { draftLocationName = it },
                                            latitude = uiState.latitude,
                                            longitude = uiState.longitude,
                                            onPlacePicked = { place ->
                                                draftLocationName = place.name
                                                viewModel.updateGarageLocationName(place.name)
                                            },
                                            showSave = true,
                                            onSave = { viewModel.updateGarageLocationName(draftLocationName) },
                                            isSaving = uiState.isSavingLocationName,
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.isGarageMember) {
                            item {
                                GaragePhotoCard(
                                    photoUrl = uiState.garagePhotos.firstOrNull().orEmpty(),
                                    canEdit = uiState.isGarageOwner,
                                    isSaving = uiState.isSavingGaragePhoto,
                                    onEdit = {
                                        garagePhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                )
                            }
                        }

                        if (uiState.isGarageOwner) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MotoRed.copy(alpha = 0.1f)),
                                    onClick = { showGarageCatalog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = MotoRed)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "GARAGE SERVICES",
                                                color = MotoRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = buildString {
                                                    append(
                                                        if (uiState.garageSelectedSkills.isEmpty())
                                                            "Set vehicle types and shop prices"
                                                        else
                                                            "${uiState.garageSelectedSkills.size} garage service(s)"
                                                    )
                                                    if (uiState.garageVehicleTypes.isNotEmpty()) {
                                                        append(" · ${uiState.garageVehicleTypes.size} vehicle type(s)")
                                                    }
                                                },
                                                color = MotoInk,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Text("EDIT", color = MotoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            item {
                                GarageInviteCard(
                                    inviteCode = uiState.inviteCode,
                                    onRegenerate = { viewModel.regenerateInviteCode() },
                                )
                            }

                            if (uiState.pendingJoinRequests.isNotEmpty()) {
                                item {
                                    GaragePendingJoinsCard(
                                        pending = uiState.pendingJoinRequests,
                                        onApprove = { viewModel.approveJoinRequest(it) },
                                        onReject = { viewModel.rejectJoinRequest(it) },
                                    )
                                }
                            }
                        }

                        if (uiState.isGarageMember && uiState.garageMemberStatus == "pending") {
                            item {
                                Text(
                                    text = "Waiting for the garage owner to approve your join request.",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            }
                        }

                        if (uiState.isGarageMember && uiState.garageMemberStatus != "pending") {
                            item(key = "garage_jobs_header") {
                                val borderColor by animateColorAsState(
                                    targetValue = if (highlightGarageJobs) MotoRed else Color.Transparent,
                                    label = "garageJobsHighlight",
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "GARAGE JOBS",
                                        color = MotoInk,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Bookings assigned to your garage team",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (uiState.garageJobs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No garage jobs yet",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                items(uiState.garageJobs, key = { "garage_${it.id}" }) { job ->
                                    GarageJobItem(
                                        job = job,
                                        currentUserId = currentUserId,
                                        isGarageOwner = uiState.isGarageOwner,
                                        isGarageMember = uiState.isGarageMember,
                                        onAccept = { viewModel.acceptJob(job.id, currentUserId) },
                                        onAddAdditionalService = { text ->
                                            viewModel.addAdditionalServiceNote(job.id, text)
                                        },
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.new_requests),
                                color = MotoInk,
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
                                    actionText = "VIEW",
                                    onActionClick = { onIncomingRequest(job.id) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.ongoing_jobs),
                                color = MotoInk,
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
private fun GarageVehicleTypeCheck(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
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

@Composable
private fun GarageVehicleTypesSection(
    selectedIds: List<String>,
    onTypeToggled: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
) {
    val options = remember { ke.co.mototap.core.data.VehicleCatalogData.vehicleTypeOptions() }
    val selected = remember(selectedIds) { selectedIds.toSet() }
    val allSelected = options.isNotEmpty() && options.all { it.id in selected }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Vehicles we can service",
            color = MotoInk,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text(
            text = "Select every vehicle type this garage can service. Use Select All if you work on the full catalog.",
            color = Color.LightGray,
            fontSize = 13.sp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectAll(!allSelected) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GarageVehicleTypeCheck(selected = allSelected, onClick = { onSelectAll(!allSelected) })
            Text(
                text = "Select All",
                color = MotoInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }

        options.forEach { option ->
            val isSelected = option.id in selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTypeToggled(option.id) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GarageVehicleTypeCheck(
                    selected = isSelected,
                    onClick = { onTypeToggled(option.id) },
                )
                Text(
                    text = option.name,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun MechanicServiceSelection(
    selectedSkills: List<String>,
    servicePrices: Map<String, Long>,
    vehiclePrices: Map<String, Map<String, Long>> = emptyMap(),
    onSkillToggled: (String) -> Unit,
    onPriceChanged: (String, Long?) -> Unit,
    onVehicleRateChanged: (service: String, make: String, model: String, price: Long?) -> Unit = { _, _, _, _ -> },
    onVehicleRateRemoved: (service: String, make: String, model: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier,
    showPrices: Boolean = true,
    introText: String? = null,
    header: (@Composable () -> Unit)? = null,
) {
    val categories = ke.co.mototap.core.data.SERVICE_CATEGORIES.map { catalogCategory ->
        ServiceCategory(
            title = catalogCategory.name,
            services = catalogCategory.allItems,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (header != null) {
            item { header() }
        }

        if (header != null) {
            item {
                Text(
                    text = "Garage service prices",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        if (header != null) {
            item {
                Text(
                    text = "Garage service prices",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }

        item {
            Text(
                text = introText ?: if (showPrices) {
                    "Choose the services you offer. Pricing is optional — you can set a base price or specific vehicle rates, or leave them blank to list the service without a price."
                } else {
                    "Choose the services you personally deliver. Prices come from your garage — drivers see those rates when booking you."
                },
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(categories) { category ->
            ExpandableCategory(
                category = category,
                selectedSkills = selectedSkills,
                servicePrices = servicePrices,
                vehiclePrices = vehiclePrices,
                onSkillToggled = onSkillToggled,
                onPriceChanged = onPriceChanged,
                onVehicleRateChanged = onVehicleRateChanged,
                onVehicleRateRemoved = onVehicleRateRemoved,
                showPrices = showPrices,
            )
        }
    }
}

data class ServiceCategory(val title: String, val services: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCategory(
    category: ServiceCategory,
    selectedSkills: List<String>,
    servicePrices: Map<String, Long>,
    vehiclePrices: Map<String, Map<String, Long>>,
    onSkillToggled: (String) -> Unit,
    onPriceChanged: (String, Long?) -> Unit,
    onVehicleRateChanged: (service: String, make: String, model: String, price: Long?) -> Unit,
    onVehicleRateRemoved: (service: String, make: String, model: String) -> Unit,
    showPrices: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedInCategory = category.services.count { service ->
        selectedSkills.any { it.equals(service, ignoreCase = true) }
    }
    val pricedInCategory = category.services.count { service ->
        selectedSkills.any { it.equals(service, ignoreCase = true) } &&
            allSelectedServicesPriced(listOf(service), servicePrices, vehiclePrices)
    }

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
                        text = if (!showPrices || pricedInCategory == 0) {
                            "$selectedInCategory selected"
                        } else {
                            "$selectedInCategory selected, $pricedInCategory with prices"
                        },
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
                    val isSelected = selectedSkills.any { it.equals(service, ignoreCase = true) }
                    val towing = isTowingService(service)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onSkillToggled(service) },
                                contentAlignment = Alignment.Center,
                            ) {
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

                            Text(
                                text = service,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSkillToggled(service) },
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        if (isSelected && showPrices) {
                            val perKm = towing
                            val vehicleRates = lookupVehiclePrices(vehiclePrices, service)
                            val flatAmount = lookupFlatPrice(servicePrices, service)
                            if (!perKm) {
                                var draftFlatPrice by remember(service, flatAmount) {
                                    mutableStateOf(flatAmount?.toString().orEmpty())
                                }
                                OutlinedTextField(
                                    value = draftFlatPrice,
                                    onValueChange = { value ->
                                        draftFlatPrice = value.filter { it.isDigit() }
                                        onPriceChanged(service, parsePriceInput(draftFlatPrice))
                                    },
                                    label = { Text("Optional base price (KSh)") },
                                    placeholder = { Text("Leave blank if vehicle rates only", color = Color.Gray) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MotoRed,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedLabelColor = Color.Gray,
                                        unfocusedLabelColor = Color.Gray,
                                        cursorColor = MotoRed,
                                    ),
                                )
                            }
                            VehicleRatesEditor(
                                service = service,
                                rates = vehicleRates,
                                perKm = perKm,
                                onRateChanged = { make, model, price ->
                                    onVehicleRateChanged(service, make, model, price)
                                },
                                onRateRemoved = { make, model ->
                                    onVehicleRateRemoved(service, make, model)
                                },
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleRatesEditor(
    service: String,
    rates: Map<String, Long>,
    perKm: Boolean,
    onRateChanged: (make: String, model: String, price: Long?) -> Unit,
    onRateRemoved: (make: String, model: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val makeOptions = remember { ke.co.mototap.core.data.VehicleCatalogData.makeNames() }
    var draftMake by remember(service) { mutableStateOf("") }
    var draftModel by remember(service) { mutableStateOf("") }
    var draftPrice by remember(service) { mutableStateOf("") }
    val modelOptions = remember(draftMake) {
        ke.co.mototap.core.data.VehicleCatalogData.modelsForMake(draftMake)
    }
    val unitLabel = if (perKm) "Ksh/km" else "Ksh"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = if (perKm) {
                "Optionally set a per-km rate for a make and model. Drivers see the best match for their vehicle."
            } else {
                "Optionally set a price for a make and model. Drivers see the best match for their vehicle."
            },
            color = Color.Gray,
            fontSize = 12.sp,
        )

        rates.entries.sortedBy { it.key.lowercase() }.forEach { (key, amount) ->
            val sep = key.indexOf(':')
            val make = if (sep >= 0) key.substring(0, sep) else key
            val model = if (sep >= 0) key.substring(sep + 1) else ""
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "$make $model — KSh ${ke.co.mototap.core.util.formatKsh(amount)}${if (perKm) "/km" else ""}",
                    color = MotoInk,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onRateRemoved(make, model) }) {
                    Text("Remove", color = MotoRed, fontSize = 12.sp)
                }
            }
        }

        var makeExpanded by remember { mutableStateOf(false) }
        var modelExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = makeExpanded,
            onExpandedChange = { makeExpanded = !makeExpanded },
        ) {
            OutlinedTextField(
                value = draftMake,
                onValueChange = {},
                readOnly = true,
                label = { Text("Make") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makeExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MotoRed,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                ),
            )
            ExposedDropdownMenu(
                expanded = makeExpanded,
                onDismissRequest = { makeExpanded = false },
            ) {
                makeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            draftMake = option
                            draftModel = ""
                            makeExpanded = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = modelExpanded && draftMake.isNotBlank(),
            onExpandedChange = { if (draftMake.isNotBlank()) modelExpanded = !modelExpanded },
        ) {
            OutlinedTextField(
                value = draftModel,
                onValueChange = {},
                readOnly = true,
                enabled = draftMake.isNotBlank(),
                label = { Text("Model") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MotoRed,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = Color.Gray,
                    unfocusedLabelColor = Color.Gray,
                ),
            )
            ExposedDropdownMenu(
                expanded = modelExpanded && draftMake.isNotBlank(),
                onDismissRequest = { modelExpanded = false },
            ) {
                modelOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            draftModel = option
                            modelExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = draftPrice,
            onValueChange = { draftPrice = it.filter { ch -> ch.isDigit() } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(unitLabel, color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.DarkGray,
                cursorColor = MotoRed,
            ),
        )

        OutlinedButton(
            onClick = {
                val price = parsePriceInput(draftPrice)
                if (draftMake.isBlank() || draftModel.isBlank() || price == null) return@OutlinedButton
                onRateChanged(draftMake, draftModel, price)
                draftMake = ""
                draftModel = ""
                draftPrice = ""
            },
            enabled = draftMake.isNotBlank() && draftModel.isNotBlank() && parsePriceInput(draftPrice) != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoRed),
            border = BorderStroke(1.dp, MotoRed),
        ) {
            Text("+ Add vehicle price", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GaragePendingJoinsCard(
    pending: List<ke.co.mototap.core.model.GarageMember>,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "JOIN REQUESTS",
                color = MotoRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            pending.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.displayName.ifBlank { "Mechanic" },
                        color = MotoInk,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onApprove(member.uid) }) {
                        Text("APPROVE", color = Color(0xFF7DDEA5), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { onReject(member.uid) }) {
                        Text("REJECT", color = MotoRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GaragePhotoCard(
    photoUrl: String,
    canEdit: Boolean,
    isSaving: Boolean,
    onEdit: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (canEdit && !isSaving) Modifier.clickable(onClick = onEdit) else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.DarkGray.copy(alpha = 0.5f),
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Garage profile photo",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GARAGE PROFILE PHOTO",
                    color = MotoRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
                Text(
                    text = when {
                        isSaving -> "Uploading…"
                        photoUrl.isNotBlank() -> "Shown to clients on the map"
                        else -> "Add a clear shop photo"
                    },
                    color = MotoInk,
                    fontSize = 14.sp,
                )
            }
            if (canEdit) {
                Text(
                    text = if (photoUrl.isBlank()) "ADD" else "EDIT",
                    color = MotoRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
fun GarageInviteCard(inviteCode: String, onRegenerate: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "GARAGE INVITE CODE",
                color = MotoRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = inviteCode.ifBlank { "—" },
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        if (inviteCode.isNotBlank()) {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("Garage invite code", inviteCode)
                            )
                            android.widget.Toast.makeText(context, "Invite code copied", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = inviteCode.isNotBlank(),
                ) {
                    Text("COPY", color = MotoRed, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onRegenerate) {
                    Text("REFRESH", color = MotoRed, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "Share this code with mechanics so they can join your garage.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NewGarageJobAlertBanner(count: Int, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "newJobPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "newJobAlpha",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.75f + pulse * 0.25f)
            .clickable(onClick = onClick),
        color = MotoRed,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MotoInk,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (count <= 1) {
                    "YOU HAVE A NEW JOB — CLICK HERE TO VIEW IT"
                } else {
                    "YOU HAVE $count NEW JOBS — CLICK HERE TO VIEW THEM"
                },
                color = MotoInk,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun GarageJobItem(
    job: JobRequest,
    currentUserId: String,
    isGarageOwner: Boolean = false,
    isGarageMember: Boolean = false,
    onAccept: () -> Unit,
    onAddAdditionalService: (String) -> Unit = {},
) {
    val assignee = when {
        job.mechanicId.isNullOrBlank() -> "Unassigned"
        job.mechanicId == currentUserId -> "You"
        else -> "Teammate"
    }
    val canAccept = job.mechanicId.isNullOrBlank() &&
        (job.status == JobStatus.REQUESTED || job.status == JobStatus.MATCHING)
    val towing = ke.co.mototap.core.util.isTowingService(job.issueType)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "${job.issueType} — ${job.locationLabel}",
            color = MotoInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Status: ${job.status.name} · Mechanic: $assignee",
            color = Color.Gray,
            fontSize = 12.sp,
        )
        if (job.price > 0) {
            Text(
                text = if (towing && job.description.contains("/km", ignoreCase = true)) {
                    "Estimate: KSh ${ke.co.mototap.core.util.formatKsh(job.price)} (${job.description})"
                } else {
                    "Price: KSh ${ke.co.mototap.core.util.formatKsh(job.price)}"
                },
                color = MotoRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        JobAdditionalServicesSection(
            notes = job.additionalServices,
            canAdd = JobAdditionalServices.canAdd(
                job,
                currentUserId,
                isGarageOwner = isGarageOwner,
                isGarageMember = isGarageMember,
            ),
            onAdd = onAddAdditionalService,
        )
        if (canAccept) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("ACCEPT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RequestItem(label: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = MotoInk, fontSize = 14.sp, maxLines = 1)
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

@Composable
fun ProviderHomeHeader(
    profile: UserProfile?,
    uiState: MechanicUiState,
    onSeeAllJobs: () -> Unit,
    onOpenJob: () -> Unit,
    onNewRequests: () -> Unit = onSeeAllJobs,
    roleLabel: String = "Mechanic / Garage",
) {
    val shopName = profile?.institutionName?.ifBlank { profile.name }?.ifBlank { "Your shop" }
        ?: uiState.locationName.ifBlank { "Your shop" }
    val photo = profile?.garagePhotos?.firstOrNull()?.ifBlank { null }
        ?: profile?.profilePhotoUrl?.ifBlank { null }
        ?: uiState.garagePhotos.firstOrNull()
    val hours = getOpenClosedStatus(profile?.workingHours)
    var acceptingJobs by remember { mutableStateOf(hours.isOpen) }
    val startOfDay = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayEarnings = uiState.completedJobs
        .filter { it.createdAtMillis >= startOfDay }
        .sumOf { it.price }
    val recentJobs = (uiState.ongoingJobs + uiState.completedJobs)
        .sortedByDescending { it.createdAtMillis }
        .take(4)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MotoCardShape,
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F6),
                ) {
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = shopName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = MotoRed,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(shopName, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        if (profile?.status == VerificationStatus.VERIFIED) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = MotoGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(roleLabel, color = MotoMuted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            uiState.locationName.ifBlank { profile?.locationName.orEmpty().ifBlank { "Kenya" } },
                            color = MotoMuted,
                            fontSize = 12.sp,
                        )
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
                onClick = { acceptingJobs = true },
                label = { Text(if (hours.isOpen || acceptingJobs) "Online" else hours.label) },
                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFE8F8EE),
                    labelColor = MotoGreen,
                    leadingIconContentColor = MotoGreen,
                ),
            )
            Text("Ready to receive jobs", color = MotoMuted, fontSize = 13.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProviderStatCard("Today's Earnings", "KSh ${formatKsh(todayEarnings)}", Icons.Default.ShoppingBag, Modifier.weight(1f))
            ProviderStatCard("Jobs Completed", uiState.completedJobs.size.toString(), Icons.Default.Star, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProviderStatCard(
                "New Requests",
                uiState.openJobs.size.toString(),
                Icons.Default.Description,
                Modifier.weight(1f).clickable(onClick = onNewRequests),
            )
            ProviderStatCard("Ongoing Jobs", uiState.ongoingJobs.size.toString(), Icons.Default.Build, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { acceptingJobs = true },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (acceptingJobs) MotoGreen else Color.White,
                    contentColor = if (acceptingJobs) Color.White else MotoInk,
                ),
                border = if (!acceptingJobs) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
            ) { Text("Online", fontWeight = FontWeight.Bold) }
            Button(
                onClick = { acceptingJobs = false },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!acceptingJobs) Color(0xFF111111) else Color.White,
                    contentColor = if (!acceptingJobs) Color.White else MotoInk,
                ),
                border = if (acceptingJobs) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
            ) { Text("Offline", fontWeight = FontWeight.Bold) }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Recent Jobs", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            TextButton(onClick = onSeeAllJobs) {
                Text("See all", color = MotoRed, fontWeight = FontWeight.Bold)
            }
        }

        if (recentJobs.isEmpty()) {
            Text("No recent jobs yet", color = MotoMuted, fontSize = 13.sp)
        } else {
            recentJobs.forEach { job ->
                val done = job.status == JobStatus.COMPLETED || job.status == JobStatus.PAID || job.status == JobStatus.CLOSED
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenJob() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MotoRed)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${job.vehicleMake} ${job.vehicleModel}".trim().ifBlank { job.issueType },
                                color = MotoInk,
                                fontWeight = FontWeight.Bold,
                            )
                            Text("KSh ${formatKsh(job.price)}", color = MotoMuted, fontSize = 13.sp)
                            Text(formatRecentJobTime(job.createdAtMillis), color = MotoMuted, fontSize = 12.sp)
                        }
                        Surface(
                            color = if (done) Color(0xFFE8F8EE) else Color(0xFFFFE8EA),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(
                                if (done) "Completed" else job.status.name.replace('_', ' ').lowercase()
                                    .replaceFirstChar { it.titlecase() },
                                color = if (done) MotoGreen else MotoRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, color = MotoMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Icon(icon, contentDescription = null, tint = MotoGreen, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

private fun formatRecentJobTime(millis: Long): String {
    if (millis <= 0L) return ""
    val now = java.util.Calendar.getInstance()
    val job = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val time = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(millis))
    val sameDay = now.get(java.util.Calendar.YEAR) == job.get(java.util.Calendar.YEAR) &&
        now.get(java.util.Calendar.DAY_OF_YEAR) == job.get(java.util.Calendar.DAY_OF_YEAR)
    return if (sameDay) "Today, $time"
    else java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(millis))
}

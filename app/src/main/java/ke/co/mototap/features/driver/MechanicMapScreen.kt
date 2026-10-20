package ke.co.mototap.features.driver

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.model.VerificationStatus
import ke.co.mototap.core.util.formatDistanceMeters
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.core.util.garageServicesVehicle
import ke.co.mototap.core.util.getMechanicServicePrice
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.MotoCardShape
import ke.co.mototap.ui.theme.MotoGreen
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import kotlinx.coroutines.tasks.await

private enum class ProviderSort { NEAREST, LOWEST_PRICE, HIGHEST_RATED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicMapScreen(
    service: String,
    viewModel: DriverHomeViewModel,
    isAdmin: Boolean = false,
    onMechanicDetailsClick: (UserProfile) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var sort by remember { mutableStateOf(ProviderSort.NEAREST) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(setOf<String>()) }

    val filteredMechanics = remember(
        uiState.availableMechanics,
        service,
        uiState.activeVehicleMake,
        uiState.activeVehicleModel,
        query,
    ) {
        val trimmedService = service.trim().lowercase()
        val q = query.trim().lowercase()
        uiState.availableMechanics.filter { mechanic ->
            val hasService = mechanic.availableServices.any { it.trim().lowercase() == trimmedService } ||
                mechanic.skills.any { it.trim().lowercase() == trimmedService }
            val containsService = mechanic.availableServices.any { it.trim().lowercase().contains(trimmedService) } ||
                mechanic.skills.any { it.trim().lowercase().contains(trimmedService) }
            val matchesQuery = q.isBlank() ||
                mechanic.name.contains(q, ignoreCase = true) ||
                mechanic.locationName.contains(q, ignoreCase = true)
            (hasService || containsService) &&
                matchesQuery &&
                garageServicesVehicle(
                    mechanic.garageVehicleTypes,
                    uiState.activeVehicleMake,
                    uiState.activeVehicleModel,
                )
        }
    }

    val sortedMechanics = remember(
        filteredMechanics,
        sort,
        uiState.userLocation,
        service,
        uiState.activeVehicleMake,
        uiState.activeVehicleModel,
    ) {
        filteredMechanics.sortedWith(
            when (sort) {
                ProviderSort.NEAREST -> compareBy { distanceMeters(uiState.userLocation, it) ?: Float.MAX_VALUE }
                ProviderSort.LOWEST_PRICE -> compareBy {
                    getMechanicServicePrice(it, service, uiState.activeVehicleMake, uiState.activeVehicleModel)
                        ?: Long.MAX_VALUE
                }
                ProviderSort.HIGHEST_RATED -> compareByDescending<UserProfile> { it.rating }
                    .thenByDescending { it.reviewCount }
            },
        )
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

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    viewModel.setUserLocation(LatLng(location.latitude, location.longitude))
                }
            } catch (_: Exception) {
            }
        }
    }

    val locationLabel = uiState.locationInput.ifBlank { "Nairobi, Kenya" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mechanic",
                        color = MotoInk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MotoInk)
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MotoInk)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MotoInk,
                ),
            )
        },
        containerColor = Color.White,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (showSearch) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("Search providers") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "Nearby providers",
                        color = MotoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(locationLabel, color = MotoMuted, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(
                            "${sortedMechanics.size} provider${if (sortedMechanics.size == 1) "" else "s"}",
                            color = MotoMuted,
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ProviderSortChip("Nearest", sort == ProviderSort.NEAREST) { sort = ProviderSort.NEAREST }
                        }
                        item {
                            ProviderSortChip("Lowest Price", sort == ProviderSort.LOWEST_PRICE) { sort = ProviderSort.LOWEST_PRICE }
                        }
                        item {
                            ProviderSortChip("Highest Rated", sort == ProviderSort.HIGHEST_RATED) { sort = ProviderSort.HIGHEST_RATED }
                        }
                    }
                }
                if (sortedMechanics.isEmpty()) {
                    item {
                        Text(
                            "No providers found for this service nearby.",
                            color = MotoMuted,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
                items(sortedMechanics, key = { it.id }) { mechanic ->
                    NearbyProviderCard(
                        mechanic = mechanic,
                        service = service,
                        userLocation = uiState.userLocation,
                        vehicleMake = uiState.activeVehicleMake,
                        vehicleModel = uiState.activeVehicleModel,
                        favorited = mechanic.id in favorites,
                        onToggleFavorite = {
                            favorites = if (mechanic.id in favorites) favorites - mechanic.id else favorites + mechanic.id
                        },
                        onView = { onMechanicDetailsClick(mechanic) },
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ProviderSortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MotoRed,
            selectedLabelColor = Color.White,
            containerColor = Color(0xFFF3F3F5),
            labelColor = MotoInk,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
private fun NearbyProviderCard(
    mechanic: UserProfile,
    service: String,
    userLocation: LatLng?,
    vehicleMake: String,
    vehicleModel: String,
    favorited: Boolean,
    onToggleFavorite: () -> Unit,
    onView: () -> Unit,
) {
    val photoUrl = mechanic.garagePhotos.firstOrNull { it.isNotBlank() }
        ?: mechanic.profilePhotoUrl.takeIf { it.isNotBlank() }
    val meters = distanceMeters(userLocation, mechanic)
    val area = formatShopAreaLabel(mechanic.locationName).ifBlank { mechanic.address }
    val distanceLabel = meters?.let { formatDistanceMeters(it) }.orEmpty()
    val placeLine = when {
        distanceLabel.isNotBlank() && area.isNotBlank() -> "$distanceLabel • $area"
        distanceLabel.isNotBlank() -> distanceLabel
        area.isNotBlank() -> area
        else -> "Location shared"
    }
    val price = getMechanicServicePrice(mechanic, service, vehicleMake, vehicleModel)
    val verified = mechanic.status == VerificationStatus.VERIFIED

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MotoCardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F2)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(Color(0xFFECECEF)),
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "${mechanic.name} garage photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF2B2B30), Color(0xFF111111)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(48.dp))
                    }
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.92f), CircleShape),
                ) {
                    Icon(
                        imageVector = if (favorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (favorited) MotoRed else MotoInk,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            mechanic.name.ifBlank { "Garage" },
                            color = MotoInk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5B400), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mechanic.reviewCount > 0) {
                                    "${formatRating(mechanic.rating)} (${mechanic.reviewCount} reviews)"
                                } else {
                                    "New"
                                },
                                color = MotoMuted,
                                fontSize = 13.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(placeLine, color = MotoMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (verified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MotoGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", color = MotoGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (price != null && price > 0) "From KSh ${formatKsh(price)}" else "Price on request",
                        color = MotoInk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = onView,
                        colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                        shape = MotoButtonShape,
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                    ) {
                        Text("View", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun distanceMeters(userLocation: LatLng?, mechanic: UserProfile): Float? {
    val userLoc = userLocation ?: return null
    val lat = mechanic.latitude ?: return null
    val lng = mechanic.longitude ?: return null
    val results = FloatArray(1)
    Location.distanceBetween(userLoc.latitude, userLoc.longitude, lat, lng, results)
    return results[0]
}

private fun formatRating(rating: Double): String =
    if (rating <= 0.0) "—" else String.format("%.1f", rating)

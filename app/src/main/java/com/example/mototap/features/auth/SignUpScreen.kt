package com.example.mototap.features.auth

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: (String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val signUpStep by viewModel.signUpStep.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success && signUpStep == SignUpStep.BASIC_INFO) {
            viewModel.nextStep()
            viewModel.resetUiState()
        }
    }

    Scaffold(
        topBar = {
            if (signUpStep != SignUpStep.BASIC_INFO) {
                TopAppBar(
                    title = {
                        Text(
                            "Step ${signUpStep.ordinal + 1}: ${when(signUpStep) {
                                SignUpStep.IDENTITY_VERIFICATION -> "Identity"
                                SignUpStep.ADDITIONAL_INFO -> "Details"
                                else -> ""
                            }}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MotoRed)
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (signUpStep) {
                SignUpStep.BASIC_INFO -> BasicInfoStep(viewModel, onNavigateToLogin)
                SignUpStep.IDENTITY_VERIFICATION -> IdentityVerificationStep(viewModel)
                SignUpStep.ADDITIONAL_INFO -> AdditionalInfoStep(viewModel, onSignUpSuccess)
            }
        }
    }
}

@Composable
fun BasicInfoStep(viewModel: AuthViewModel, onNavigateToLogin: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val role by viewModel.role.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.signup_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = stringResource(id = R.string.signup_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.name.value = it },
            label = { Text(stringResource(id = R.string.signup_name_label), color = Color.Gray) },
            singleLine = true,
            isError = name.isNotBlank() && !viewModel.isNameValid(name),
            supportingText = {
                if (name.isNotBlank() && !viewModel.isNameValid(name)) {
                    Text("Please provide at least two names (e.g. John Doe)", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Email", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Password", color = Color.Gray) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { viewModel.phoneNumber.value = it },
            label = { Text("Phone Number", color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Role:",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = role == "driver",
                onClick = { viewModel.role.value = "driver" },
                colors = RadioButtonDefaults.colors(selectedColor = MotoRed, unselectedColor = Color.Gray)
            )
            Text(
                text = "Driver",
                color = Color.White,
                modifier = Modifier.clickable { viewModel.role.value = "driver" }
            )
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(
                selected = role == "mechanic",
                onClick = { viewModel.role.value = "mechanic" },
                colors = RadioButtonDefaults.colors(selectedColor = MotoRed, unselectedColor = Color.Gray)
            )
            Text(
                text = "Mechanic",
                color = Color.White,
                modifier = Modifier.clickable { viewModel.role.value = "mechanic" }
            )
        }

        Button(
            onClick = { viewModel.signUp() },
            enabled = uiState !is AuthUiState.Loading &&
                    viewModel.isNameValid(name) &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    phoneNumber.isNotBlank() &&
                    role.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "NEXT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        TextButton(
            onClick = {
                viewModel.resetState()
                onNavigateToLogin()
            },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.signup_have_account),
                color = MotoRed
            )
        }
    }
}

@Composable
fun IdentityVerificationStep(viewModel: AuthViewModel) {
    val role by viewModel.role.collectAsState()
    val idNumber by viewModel.idNumber.collectAsState()
    val profilePhotoUrl by viewModel.profilePhotoUrl.collectAsState()
    val idPhotoUrl by viewModel.idPhotoUrl.collectAsState()

    val profileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "profile") }
    }
    
    val idLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "id_front") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 2: Identity Verification",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        VerificationItem(
            label = "Selfie / Profile Photo",
            description = "Live camera capture or gallery upload",
            previewUrl = profilePhotoUrl,
            onPickImage = { profileLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val idLabel = if (role == "driver") "Driving License" else "Mechanic Certification"

        OutlinedTextField(
            value = idNumber,
            onValueChange = { viewModel.idNumber.value = it },
            label = { Text("$idLabel Number", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        VerificationItem(
            label = "Front Photo of $idLabel",
            previewUrl = idPhotoUrl,
            onPickImage = { idLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.nextStep() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text("NEXT", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerificationItem(label: String, description: String? = null, previewUrl: String? = null, onPickImage: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
        if (description != null) {
            Text(text = description, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.DarkGray, shape = MaterialTheme.shapes.medium)
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (previewUrl.isNullOrBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("Tap to upload", color = Color.White)
                }
            } else {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdditionalInfoStep(viewModel: AuthViewModel, onSignUpSuccess: (String?) -> Unit) {
    val role by viewModel.role.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 3: Additional Information",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (role == "driver") {
            DriverAdditionalInfo(viewModel)
        } else {
            MechanicAdditionalInfo(viewModel)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.completeProfile { onSignUpSuccess(role) } },
            enabled = uiState !is AuthUiState.Loading && (role != "mechanic" || (latitude != null && longitude != null)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("COMPLETE SIGN UP", fontWeight = FontWeight.Bold)
            }
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
fun DriverAdditionalInfo(viewModel: AuthViewModel) {
    val vehicleType by viewModel.vehicleType.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()
    val numberPlate by viewModel.numberPlate.collectAsState()
    val vehiclePhotoUrl by viewModel.vehiclePhotoUrl.collectAsState()

    val vehicleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "vehicle") }
    }

    OutlinedTextField(
        value = vehicleType,
        onValueChange = { viewModel.vehicleType.value = it },
        label = { Text("Vehicle Type (e.g. Motorcycle, Car)", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = vehicleModel,
        onValueChange = { viewModel.vehicleModel.value = it },
        label = { Text("Vehicle Model", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = numberPlate,
        onValueChange = { viewModel.numberPlate.value = it },
        label = { Text("Number Plate", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
    Spacer(modifier = Modifier.height(16.dp))
    VerificationItem(
        label = "Vehicle Photo",
        previewUrl = vehiclePhotoUrl,
        onPickImage = { vehicleLauncher.launch("image/*") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicAdditionalInfo(viewModel: AuthViewModel) {
    val institutionName by viewModel.institutionName.collectAsState()
    val experienceYears by viewModel.experienceYears.collectAsState()
    val address by viewModel.address.collectAsState()
    val garagePhotos by viewModel.garagePhotos.collectAsState()
    val idPhotoUrl by viewModel.idPhotoUrl.collectAsState()
    val certificatePhotoUrl by viewModel.certificatePhotoUrl.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val availableServices by viewModel.availableServices.collectAsState()

    val certLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "certificate") }
    }
    
    val garageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "garage") }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showMap by remember { mutableStateOf(false) }

    // Automatic Address Reflection when Location changes
    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                val addressLine = addresses?.firstOrNull()?.getAddressLine(0)
                if (addressLine != null) {
                    viewModel.address.value = addressLine
                }
            } catch (e: Exception) {
                // Fallback to coordinates if Geocoder fails
                if (viewModel.address.value.isBlank() || viewModel.address.value.contains(",")) {
                    viewModel.address.value = "$latitude, $longitude"
                }
            }
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
        if (locationPermissionGranted) {
            showMap = true
        }
    }

    if (showMap) {
        val nairobi = LatLng(-1.286389, 36.817223)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                if (latitude != null && longitude != null) LatLng(latitude!!, longitude!!) else nairobi,
                15f
            )
        }

        AlertDialog(
            onDismissRequest = { showMap = false },
            confirmButton = {
                Button(onClick = {
                    if (latitude == null || longitude == null) {
                        viewModel.latitude.value = cameraPositionState.position.target.latitude
                        viewModel.longitude.value = cameraPositionState.position.target.longitude
                    }
                    showMap = false
                }) { Text("Confirm Location") }
            },
            title = { Text("Pin Garage Location") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
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
                            zoomControlsEnabled = true,
                            compassEnabled = true
                        ),
                        onMapClick = { latLng ->
                            viewModel.latitude.value = latLng.latitude
                            viewModel.longitude.value = latLng.longitude
                        }
                    ) {
                        latitude?.let { lat ->
                            longitude?.let { lon ->
                                Marker(state = MarkerState(position = LatLng(lat, lon)))
                            }
                        }
                    }
                }
            }
        )
    }

    OutlinedTextField(
        value = institutionName,
        onValueChange = { viewModel.institutionName.value = it },
        label = { Text("Institution Name", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    VerificationItem(
        label = "Front Photo of Mechanic Certification",
        previewUrl = certificatePhotoUrl,
        onPickImage = { certLauncher.launch("image/*") }
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    Text("Experience", color = Color.White)
    val experienceOptions = listOf("0-1 years", "1-3 years", "3-5 years", "5-10 years", "10+ years")
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        experienceOptions.forEach { option ->
            FilterChip(
                selected = experienceYears == option,
                onClick = { viewModel.experienceYears.value = option },
                label = { Text(option) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = address,
        onValueChange = { viewModel.address.value = it },
        label = { Text("Garage Address", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            if (locationPermissionGranted) {
                showMap = true
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (latitude != null) "Location Pinned" else "Pin Garage Location on Map")
    }

    Spacer(modifier = Modifier.height(16.dp))
    VerificationItem(
        label = "Garage Front Photo",
        previewUrl = garagePhotos.lastOrNull(),
        onPickImage = { garageLauncher.launch("image/*") }
    )
}

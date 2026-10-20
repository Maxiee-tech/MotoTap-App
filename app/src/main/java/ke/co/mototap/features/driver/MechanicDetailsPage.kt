package ke.co.mototap.features.driver

import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import ke.co.mototap.core.model.Review
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.model.VerificationStatus
import ke.co.mototap.core.util.formatDistanceMeters
import ke.co.mototap.core.util.formatKsh
import ke.co.mototap.core.util.formatShopAreaLabel
import ke.co.mototap.core.util.getMechanicServicePrice
import ke.co.mototap.core.util.mechanicServiceInventory
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoGreen
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDetailsPage(
    mechanic: UserProfile,
    selectedService: String = "",
    reviews: List<Review> = emptyList(),
    isAdmin: Boolean = false,
    vehicleMake: String = "",
    vehicleModel: String = "",
    userLocation: LatLng? = null,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onBookService: (serviceName: String, estimatedKm: Double?) -> Unit,
    onRequestService: (serviceName: String) -> Unit,
) {
    val context = LocalContext.current
    val services = mechanicServiceInventory(mechanic)
    val highlightedService = selectedService.trim().takeIf { it.isNotEmpty() }
    val highlightedPrice = highlightedService?.let {
        getMechanicServicePrice(mechanic, it, vehicleMake, vehicleModel)
    }
    val photos = remember(mechanic) {
        (mechanic.garagePhotos + listOf(mechanic.profilePhotoUrl, mechanic.certificatePhotoUrl))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
    val prices = remember(mechanic, services, vehicleMake, vehicleModel) {
        services.mapNotNull { getMechanicServicePrice(mechanic, it, vehicleMake, vehicleModel)?.takeIf { p -> p > 0 } }
    }
    var showAllReviews by remember { mutableStateOf(false) }
    var favorited by remember { mutableStateOf(false) }
    var showTowingKmDialog by remember { mutableStateOf(false) }
    var towingKmText by remember { mutableStateOf("") }
    var towingKmError by remember { mutableStateOf<String?>(null) }

    fun requestBook(serviceName: String) {
        if (ke.co.mototap.core.util.isTowingService(serviceName)) {
            towingKmText = ""
            towingKmError = null
            showTowingKmDialog = true
        } else {
            onBookService(serviceName, null)
        }
    }

    if (showTowingKmDialog) {
        val rate = highlightedPrice ?: 0L
        val km = towingKmText.toDoubleOrNull()
        val estimate = km?.let { ke.co.mototap.core.util.estimateTowingTotal(rate, it) }
        AlertDialog(
            onDismissRequest = { showTowingKmDialog = false },
            title = { Text("Towing distance", color = MotoInk) },
            text = {
                Column {
                    Text(
                        text = if (rate > 0) {
                            "Rate: KSh ${formatKsh(rate)}/km — enter estimated kilometres."
                        } else {
                            "Enter estimated kilometres for this tow."
                        },
                        color = MotoMuted,
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = towingKmText,
                        onValueChange = {
                            towingKmText = it.filter { ch -> ch.isDigit() || ch == '.' }
                            towingKmError = null
                        },
                        label = { Text("Kilometres") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                    )
                    if (estimate != null) {
                        Text(
                            text = "Estimated total: KSh ${formatKsh(estimate)}",
                            color = MotoRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    towingKmError?.let {
                        Text(text = it, color = MotoRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = towingKmText.toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            towingKmError = "Enter a valid distance in km."
                            return@TextButton
                        }
                        if (rate <= 0L) {
                            towingKmError = "Unable to estimate towing total. Check the rate and distance."
                            return@TextButton
                        }
                        showTowingKmDialog = false
                        onBookService(highlightedService ?: "General Maintenance", parsed)
                    }
                ) {
                    Text("Continue", color = MotoRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTowingKmDialog = false }) {
                    Text("Cancel", color = MotoMuted)
                }
            },
            containerColor = Color.White,
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Provider Profile", color = MotoInk, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MotoInk)
                    }
                },
                actions = {
                    IconButton(onClick = { favorited = !favorited }) {
                        Icon(
                            imageVector = if (favorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (favorited) MotoRed else MotoInk,
                        )
                    }
                    IconButton(
                        onClick = {
                            val share = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${mechanic.name}\n${formatShopAreaLabel(mechanic.locationName)}\nFind them on MotoTap",
                                )
                            }
                            context.startActivity(Intent.createChooser(share, "Share provider"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MotoInk)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        containerColor = Color.White,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = {
                        onRequestService(highlightedService ?: services.firstOrNull() ?: "General Maintenance")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                    shape = MotoButtonShape,
                ) {
                    Text("Request Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            ProviderPhotoPager(photos = photos, garageName = mechanic.name)

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = mechanic.name.ifBlank { mechanic.institutionName.ifBlank { "Garage" } },
                    color = MotoInk,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF5B400), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (mechanic.reviewCount > 0) {
                            "${formatRating(mechanic.rating)} (${mechanic.reviewCount} reviews)"
                        } else {
                            "New on MotoTap"
                        },
                        color = MotoMuted,
                        fontSize = 14.sp,
                    )
                }
                if (mechanic.status == VerificationStatus.VERIFIED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MotoGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verified by MotoTap", color = MotoGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MotoRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = providerPlaceLine(mechanic, userLocation),
                        color = MotoMuted,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
                Text("Services offered", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                val displayServices = if (services.isNotEmpty()) services else listOf("General repair")
                displayServices.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { name ->
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(serviceOfferedIcon(name), contentDescription = null, tint = MotoRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, color = MotoInk, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Estimated price range", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = MotoRed, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = priceRangeLabel(highlightedPrice, prices),
                        color = MotoInk,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent reviews", color = MotoInk, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    if (reviews.size > 1) {
                        TextButton(onClick = { showAllReviews = !showAllReviews }) {
                            Text(if (showAllReviews) "Show less" else "See all", color = MotoRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                val visibleReviews = when {
                    reviews.isEmpty() -> emptyList()
                    showAllReviews -> reviews
                    else -> reviews.take(1)
                }
                if (visibleReviews.isEmpty()) {
                    Text("No reviews yet.", color = MotoMuted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    visibleReviews.forEach { review ->
                        ReviewItem(review)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                TextButton(onClick = onChat, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Chat with provider", color = MotoMuted, fontWeight = FontWeight.SemiBold)
                }

                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (mechanic.phone.isNotBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mechanic.phone}")))
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call")
                        }
                        OutlinedButton(
                            onClick = {
                                if (mechanic.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${mechanic.phone}"))
                                    intent.putExtra("sms_body", "Hi, I'm interested in your services")
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProviderPhotoPager(photos: List<String>, garageName: String) {
    val pagerState = rememberPagerState(pageCount = { photos.size.coerceAtLeast(1) })
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(Color(0xFFECECEF)),
    ) {
        if (photos.isNotEmpty()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = photos[page],
                    contentDescription = "$garageName photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF2B2B30), Color(0xFF111111)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
            }
        }
        if (photos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(photos.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 8.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.45f)),
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        val initial = review.driverName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F0F2)),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, color = MotoInk, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.driverName.ifBlank { "Customer" },
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(relativeTime(review.timestampMillis), color = MotoMuted, fontSize = 12.sp)
            }
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= review.rating) Color(0xFFF5B400) else Color(0xFFE5E5E5),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            if (review.comment.isNotBlank()) {
                Text(review.comment, color = MotoMuted, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

private fun providerPlaceLine(mechanic: UserProfile, userLocation: LatLng?): String {
    val area = formatShopAreaLabel(mechanic.locationName).ifBlank { mechanic.address }
    val meters = if (userLocation != null && mechanic.latitude != null && mechanic.longitude != null) {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            mechanic.latitude, mechanic.longitude,
            results,
        )
        results[0]
    } else {
        null
    }
    val distance = meters?.let { formatDistanceMeters(it) }.orEmpty()
    return when {
        distance.isNotBlank() && area.isNotBlank() -> "$distance • $area"
        distance.isNotBlank() -> distance
        area.isNotBlank() -> area
        else -> "Location shared"
    }
}

private fun priceRangeLabel(highlighted: Long?, prices: List<Long>): String {
    val all = (listOfNotNull(highlighted?.takeIf { it > 0 }) + prices).distinct().sorted()
    return when {
        all.isEmpty() -> "On request"
        all.size == 1 -> "KSh ${formatKsh(all.first())}"
        else -> "KSh ${formatKsh(all.first())} – ${formatKsh(all.last())}"
    }
}

private fun serviceOfferedIcon(name: String): ImageVector {
    val key = name.lowercase()
    return when {
        "diagnostic" in key -> Icons.Default.Search
        "engine" in key -> Icons.Default.Settings
        "brake" in key -> Icons.Default.Warning
        "electric" in key -> Icons.Default.FlashOn
        "suspension" in key || "shock" in key -> Icons.Default.Build
        "start" in key || "general" in key -> Icons.Default.DirectionsCar
        else -> Icons.Default.Build
    }
}

private fun formatRating(rating: Double): String =
    if (rating <= 0.0) "—" else String.format("%.1f", rating)

private fun relativeTime(timestampMillis: Long): String {
    val diff = System.currentTimeMillis() - timestampMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "$days day${if (days == 1L) "" else "s"} ago"
        days < 30 -> "${days / 7} week${if (days / 7 == 1L) "" else "s"} ago"
        else -> "${days / 30} month${if (days / 30 == 1L) "" else "s"} ago"
    }
}

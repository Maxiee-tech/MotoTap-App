package com.example.mototap.features.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.mototap.core.model.UserProfile
import com.example.mototap.core.model.Review
import com.example.mototap.ui.theme.MotoRed
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicDetailsPage(
    mechanic: UserProfile,
    reviews: List<Review> = emptyList(),
    isAdmin: Boolean = false,
    onBack: () -> Unit,
    onChat: () -> Unit,
    onBookService: (String) -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mechanic Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MotoRed
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo & Mechanic Name
            if (!mechanic.profilePhotoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mechanic.profilePhotoUrl,
                    contentDescription = "Mechanic Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MotoRed, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MotoRed.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MotoRed,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = mechanic.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Rating Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${mechanic.rating} (${mechanic.reviewCount} reviews)",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            // Institution/Garage Name
            Text(
                text = mechanic.institutionName.ifBlank { "Professional Mechanic" },
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Garage Photos
            if (mechanic.garagePhotos.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    items(mechanic.garagePhotos) { photoUrl ->
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Garage Photo",
                            modifier = Modifier
                                .size(width = 200.dp, height = 120.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Location Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Location",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = mechanic.address.ifBlank { "Location not specified" },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Experience Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Experience & Expertise",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Experience: ${mechanic.experienceYears.ifBlank { "Not specified" }}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (mechanic.brandSpecializations.isNotEmpty()) {
                        Text(
                            text = "Specializes in: ${mechanic.brandSpecializations.joinToString(", ")}",
                            color = MotoRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Available Services Card
            if (mechanic.availableServices.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Available Services",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            mechanic.availableServices.forEach { service ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "• ",
                                            color = MotoRed,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = service,
                                            color = Color.LightGray,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                    
                                    TextButton(
                                        onClick = { onBookService(service) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MotoRed),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("BOOK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                if (mechanic.availableServices.last() != service) {
                                    HorizontalDivider(color = Color.Black.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            // Phone Number Card
            if (isAdmin) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Contact",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = mechanic.phone.ifBlank { "Phone not available" },
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Call Button
                    Button(
                        onClick = {
                            if (mechanic.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${mechanic.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call", fontWeight = FontWeight.Bold)
                    }

                    // Message Button
                    Button(
                        onClick = {
                            if (mechanic.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${mechanic.phone}"))
                                intent.putExtra("sms_body", "Hi, I'm interested in your services")
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Message",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Chat Button
            Button(
                onClick = onChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Chat with Mechanic", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            // Booking Button
            Button(
                onClick = { onBookService("General Maintenance") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("BOOK NOW", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Reviews Section
            if (reviews.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Customer Reviews",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    reviews.forEach { review ->
                        ReviewItem(review)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(review.timestampMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.driverName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = dateString,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= review.rating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}


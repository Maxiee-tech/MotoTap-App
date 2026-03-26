package com.example.mototap.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

data class ServiceItem(val name: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    onCategorySelected: (String) -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MOTOTAP",
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
                onNavigate = { route ->
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                    onServiceClick = { onCategorySelected(it) }
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
                    onServiceClick = { onCategorySelected(it) }
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
                    onServiceClick = { onCategorySelected(it) }
                )
            }
        }
    }
}

@Composable
fun ServiceCategorySection(
    title: String,
    services: List<ServiceItem>,
    onServiceClick: (String) -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp) 
        ) {
            items(services) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onServiceClick(service.name) }
                )
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = service.name,
                tint = MotoRed,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = service.name,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text(stringResource(R.string.home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MotoRed,
                selectedTextColor = MotoRed,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "requests",
            onClick = { onNavigate("requests") },
            icon = { Icon(Icons.Default.List, contentDescription = "Requests") },
            label = { Text(stringResource(R.string.requests)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MotoRed,
                selectedTextColor = MotoRed,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "messages",
            onClick = { onNavigate("messages") },
            icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
            label = { Text(stringResource(R.string.messages)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MotoRed,
                selectedTextColor = MotoRed,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text(stringResource(R.string.profile)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MotoRed,
                selectedTextColor = MotoRed,
                unselectedIconColor = Color.White,
                unselectedTextColor = Color.White,
                indicatorColor = Color.Transparent
            )
        )
    }
}

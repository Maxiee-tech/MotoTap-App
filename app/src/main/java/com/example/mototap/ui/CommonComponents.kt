package com.example.mototap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

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
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Requests") },
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

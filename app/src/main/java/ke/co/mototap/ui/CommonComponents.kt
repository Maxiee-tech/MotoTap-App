package ke.co.mototap.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ke.co.mototap.R
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

enum class BottomNavVariant { Driver, Provider }

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    variant: BottomNavVariant = BottomNavVariant.Driver,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MotoRed,
        selectedTextColor = MotoRed,
        unselectedIconColor = MotoMuted,
        unselectedTextColor = MotoMuted,
        indicatorColor = Color.Transparent,
    )
    val items = when (variant) {
        BottomNavVariant.Driver -> listOf(
            NavItem("home", stringResource(R.string.home), Icons.Default.Home),
            NavItem("requests", stringResource(R.string.requests), Icons.AutoMirrored.Filled.List),
            NavItem("profile", stringResource(R.string.profile), Icons.Default.Person),
        )
        BottomNavVariant.Provider -> listOf(
            NavItem("home", "Dashboard", Icons.Default.Home),
            NavItem("requests", "Jobs", Icons.AutoMirrored.Filled.List),
            NavItem("earnings", "Earnings", Icons.Default.Star),
            NavItem("profile", stringResource(R.string.profile), Icons.Default.Person),
        )
    }
    NavigationBar(
        containerColor = Color.White,
        contentColor = MotoMuted,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = itemColors,
            )
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

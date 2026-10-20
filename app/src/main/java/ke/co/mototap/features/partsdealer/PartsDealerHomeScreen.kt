package ke.co.mototap.features.partsdealer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.features.mechanic.MechanicDashboardViewModel
import ke.co.mototap.features.mechanic.ProviderHomeHeader
import ke.co.mototap.ui.BottomNavVariant
import ke.co.mototap.ui.BottomNavigationBar
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.theme.MotoScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartsDealerHomeScreen(
    profile: UserProfile?,
    viewModel: MechanicDashboardViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToRequests: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToEarnings: () -> Unit = onNavigateToRequests,
    onIncomingRequest: (String) -> Unit = {},
    onOpenJob: () -> Unit = onNavigateToRequests,
    onNavigateToNotifications: () -> Unit = onNavigateToRequests,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { BrandLogo(modifier = Modifier.height(26.dp)) },
                actions = {
                    ke.co.mototap.ui.NotificationBellButton(onClick = onNavigateToNotifications)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = "home",
                variant = BottomNavVariant.Provider,
                onNavigate = { route: String ->
                    when (route) {
                        "requests" -> onNavigateToRequests()
                        "earnings" -> onNavigateToEarnings()
                        "profile" -> onNavigateToProfile()
                    }
                },
            )
        },
        containerColor = MotoScreen,
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            item {
                ProviderHomeHeader(
                    profile = profile,
                    uiState = uiState,
                    roleLabel = "Parts Dealer",
                    onSeeAllJobs = onNavigateToRequests,
                    onOpenJob = onOpenJob,
                    onNewRequests = {
                        uiState.openJobs.firstOrNull()?.id?.let(onIncomingRequest)
                            ?: onNavigateToRequests()
                    },
                )
            }
        }
    }
}

package ke.co.mototap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.core.model.UserProfile
import ke.co.mototap.core.ui.ContactInfo
import ke.co.mototap.core.ui.LegalUrls
import ke.co.mototap.core.ui.openExternalUri
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import kotlinx.coroutines.launch

@Composable
fun WithDashboardMenu(
    profile: UserProfile?,
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (openMenu: () -> Unit) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var notice by remember { mutableStateOf<String?>(null) }

    fun closeThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                ) {
                    Text(
                        text = "MENU",
                        color = MotoRed,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile?.name?.ifBlank { null } ?: "Guest",
                        color = MotoInk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Text(
                        text = profile?.role?.name
                            ?.lowercase()
                            ?.replace('_', ' ')
                            ?.replaceFirstChar { char ->
                                if (char.isLowerCase()) char.titlecase() else char.toString()
                            }
                            ?: "Guest",
                        color = MotoMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Text(
                        text = profile?.email?.ifBlank { null } ?: "Not signed in",
                        color = MotoMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MenuRow("My Profile") { closeThen(onProfile) }
                    MenuRow("Welcome") {
                        closeThen { context.openExternalUri("https://mototap.co.ke/") }
                    }
                    MenuRow("About") {
                        closeThen { context.openExternalUri("https://mototap.co.ke/about") }
                    }
                    MenuRow("Terms & Conditions") {
                        closeThen { context.openExternalUri(LegalUrls.TERMS) }
                    }
                    MenuRow("Privacy Policy") {
                        closeThen { context.openExternalUri(LegalUrls.PRIVACY) }
                    }
                    MenuRow("Contacts") {
                        closeThen { context.openExternalUri(ContactInfo.OFFICE_MAPS) }
                    }
                    MenuRow("Settings") {
                        notice = "Settings will be available soon."
                    }
                    MenuRow("Log Out") { closeThen(onLogout) }
                    MenuRow("Delete Account") { closeThen(onProfile) }
                    notice?.let { message ->
                        Text(
                            text = message,
                            color = MotoRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        },
    ) {
        content {
            scope.launch { drawerState.open() }
        }
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = if (label == "Delete Account") MotoRed else MotoInk,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

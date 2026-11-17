package ke.co.mototap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.core.ui.ContactInfo
import ke.co.mototap.core.ui.openExternalUri
import ke.co.mototap.ui.theme.MotoInk

@Composable
fun AccountHelpNavActions(
    onRequests: () -> Unit,
    onMessages: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    var accountOpen by remember { mutableStateOf(false) }
    var helpOpen by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextButton(
                onClick = {
                    helpOpen = false
                    accountOpen = true
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MotoInk,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Account",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            DropdownMenu(
                expanded = accountOpen,
                onDismissRequest = { accountOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Requests") },
                    onClick = {
                        accountOpen = false
                        onRequests()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Messages") },
                    onClick = {
                        accountOpen = false
                        onMessages()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        accountOpen = false
                        onLogout()
                    },
                )
            }
        }

        Box {
            TextButton(
                onClick = {
                    accountOpen = false
                    helpOpen = true
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = MotoInk,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Help",
                    color = MotoInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            DropdownMenu(
                expanded = helpOpen,
                onDismissRequest = { helpOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("WhatsApp") },
                    onClick = {
                        helpOpen = false
                        context.openExternalUri(ContactInfo.WHATSAPP_1)
                    },
                )
            }
        }
    }
}

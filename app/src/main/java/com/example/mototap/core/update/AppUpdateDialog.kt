package com.example.mototap.core.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.mototap.ui.theme.MotoRed

@Composable
fun AppUpdateDialog(
    info: AppUpdateInfo,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    fun openDownload() {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!info.forceUpdate) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !info.forceUpdate,
            dismissOnClickOutside = !info.forceUpdate,
        ),
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Text(
                text = if (info.forceUpdate) "Update required" else "Update available",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "MotoTap ${info.versionName} is ready to install.",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                )
                if (info.changelog.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = info.changelog,
                        color = Color.Gray,
                        fontSize = 13.sp,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    openDownload()
                    if (!info.forceUpdate) onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                modifier = Modifier.fillMaxWidth(0.45f),
            ) {
                Text("Download", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!info.forceUpdate) {
                TextButton(
                    onClick = {
                        AppUpdateChecker.snooze(context)
                        onDismiss()
                    },
                ) {
                    Text("Later", color = Color.Gray)
                }
            }
        },
    )
}

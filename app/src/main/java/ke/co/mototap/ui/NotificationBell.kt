package ke.co.mototap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.core.notify.AppNotificationCenter
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoRed

@Composable
fun NotificationBellButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notifications by AppNotificationCenter.items.collectAsState()
    val unreadCount = notifications.count { !it.read }
    val swinging = unreadCount > 0
    val swing = rememberInfiniteTransition(label = "notif_bell")
    val angle by swing.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bell_angle",
    )

    IconButton(onClick = onClick, modifier = modifier) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = MotoRed) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 10.sp,
                        )
                    }
                }
            },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MotoInk,
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .graphicsLayer {
                            rotationZ = if (swinging) angle else 0f
                            transformOrigin = TransformOrigin(0.5f, 0.05f)
                        },
                )
            }
        }
    }
}

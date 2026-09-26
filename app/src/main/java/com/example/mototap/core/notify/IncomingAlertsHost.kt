package com.example.mototap.core.notify

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.core.model.JobStatus
import com.example.mototap.core.repository.AuthRepository
import com.example.mototap.core.repository.ChatRepository
import com.example.mototap.core.repository.JobRepository
import com.example.mototap.ui.theme.MotoRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private data class IncomingAlert(
    val title: String,
    val body: String,
    val id: Int,
)

@Composable
fun IncomingAlertsHost(
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    jobRepository: JobRepository,
) {
    val context = LocalContext.current
    val userId by authRepository.currentUserId.collectAsState(initial = null)
    var banner by remember { mutableStateOf<IncomingAlert?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(userId) {
        if (userId.isNullOrBlank()) return@LaunchedEffect
        IncomingNotificationHelper.ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !IncomingNotificationHelper.canPostNotifications(context)
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(userId) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        var primed = false
        val seen = mutableSetOf<String>()
        chatRepository.observeChatSummaries(uid).collectLatest { summaries ->
            val unread = summaries.filter { it.unread }
            if (!primed) {
                unread.forEach {
                    seen += "${it.partnerId}:${it.lastMessage.timestampMillis}"
                }
                primed = true
                return@collectLatest
            }
            unread.forEach { summary ->
                val key = "${summary.partnerId}:${summary.lastMessage.timestampMillis}"
                if (seen.add(key)) {
                    val title = "New message"
                    val body = buildString {
                        append(summary.otherUserName.ifBlank { "MotoTap" })
                        val preview = summary.lastMessage.text.trim()
                        if (preview.isNotEmpty()) {
                            append(" · ")
                            append(preview.take(80))
                        }
                    }
                    val alert = IncomingAlert(title, body, key.hashCode())
                    banner = alert
                    IncomingNotificationHelper.notify(
                        context = context,
                        title = title,
                        body = body,
                        notificationId = alert.id,
                        withSound = true,
                    )
                }
            }
        }
    }

    LaunchedEffect(userId) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val role = authRepository.getUserRole(uid)?.lowercase().orEmpty()
        if (role != "mechanic") return@LaunchedEffect
        var primed = false
        val seen = mutableSetOf<String>()
        jobRepository.observeOpenJobs().collectLatest { jobs ->
            val incoming = jobs.filter { it.status == JobStatus.REQUESTED }
            if (!primed) {
                incoming.forEach { seen += it.id }
                primed = true
                return@collectLatest
            }
            incoming.forEach { job ->
                if (seen.add(job.id)) {
                    val title = "New service request"
                    val body = job.issueType.ifBlank { "A driver requested a service nearby." }
                    val alert = IncomingAlert(title, body, job.id.hashCode())
                    banner = alert
                    IncomingNotificationHelper.notify(
                        context = context,
                        title = title,
                        body = body,
                        notificationId = alert.id,
                        withSound = true,
                    )
                }
            }
        }
    }

    LaunchedEffect(userId) {
        val uid = userId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        val role = authRepository.getUserRole(uid)?.lowercase().orEmpty()
        if (role == "mechanic") return@LaunchedEffect
        var primed = false
        val seen = mutableMapOf<String, JobStatus>()
        jobRepository.observeDriverJobs(uid).collectLatest { jobs ->
            if (!primed) {
                jobs.forEach { seen[it.id] = it.status }
                primed = true
                return@collectLatest
            }
            jobs.forEach { job ->
                val prev = seen[job.id]
                seen[job.id] = job.status
                if (prev == null || prev == job.status) return@forEach
                if (job.status != JobStatus.ASSIGNED && job.status != JobStatus.IN_PROGRESS) {
                    return@forEach
                }
                val title = "Job update"
                val issue = job.issueType.ifBlank { "Your request" }
                val body = if (job.status == JobStatus.ASSIGNED) {
                    "$issue was accepted."
                } else {
                    "$issue is in progress."
                }
                val alert = IncomingAlert(title, body, job.id.hashCode())
                banner = alert
                IncomingNotificationHelper.notify(
                    context = context,
                    title = title,
                    body = body,
                    notificationId = alert.id,
                    withSound = true,
                )
            }
        }
    }

    LaunchedEffect(banner) {
        if (banner == null) return@LaunchedEffect
        delay(6000)
        banner = null
    }

    AnimatedVisibility(
        visible = banner != null,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(12.dp),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        val current = banner
        if (current != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
                    .clickable { banner = null }
                    .padding(16.dp),
            ) {
                Text(
                    text = current.title,
                    color = MotoRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Text(
                    text = current.body,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

package com.example.mototap.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.core.repository.ChatRepository
import com.example.mototap.core.repository.ChatSummary
import com.example.mototap.ui.theme.MotoRed
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatRepository: ChatRepository,
    onChatSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val chatSummaries by chatRepository.observeChatSummaries(currentUserId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MESSAGES",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Unread filter or simple stats could go here if needed
            if (chatSummaries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatSummaries) { summary ->
                        ChatSummaryItem(
                            summary = summary,
                            onClick = { onChatSelected(summary.jobId) }
                        )
                        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSummaryItem(summary: ChatSummary, onClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = sdf.format(Date(summary.lastMessage.timestampMillis))
    val isUnread = !summary.lastMessage.read && summary.lastMessage.senderId != currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = Color.DarkGray.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.otherUserName,
                    color = Color.White,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = timeString,
                    color = if (isUnread) Color(0xFF25D366) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = summary.lastMessage.text,
                    color = if (isUnread) Color.White else Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal
                )
                
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(22.dp)
                            .background(Color(0xFF25D366), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!", // Could be dynamic count if available
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

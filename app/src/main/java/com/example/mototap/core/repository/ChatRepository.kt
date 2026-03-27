package com.example.mototap.core.repository

import com.example.mototap.core.model.ChatMessage
import kotlinx.coroutines.flow.Flow

data class ChatSummary(
    val jobId: String,
    val lastMessage: ChatMessage,
    val otherUserName: String
)

interface ChatRepository {
    fun observeMessages(jobId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(jobId: String, senderId: String, text: String): Result<Unit>
    fun observeChatSummaries(userId: String): Flow<List<ChatSummary>>
}

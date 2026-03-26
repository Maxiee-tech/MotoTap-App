package com.example.mototap.core.repository

import com.example.mototap.core.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(jobId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(jobId: String, senderId: String, text: String): Result<Unit>
}
